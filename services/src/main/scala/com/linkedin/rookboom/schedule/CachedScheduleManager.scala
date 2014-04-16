
/*
 * (c) Copyright 2014 LinkedIn Corp. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.linkedin.rookboom.schedule

import com.linkedin.rookboom.util.Logging
import com.linkedin.rookboom.util.TimeUtils._
import net.sf.ehcache.{Element, CacheManager}

/**
 * This class works as a caching proxy for a real schedule manager implementation.
 * One day schedule for a resource is used as a caching unit.
 *
 * @author Dmitriy Yefremov
 */
class CachedScheduleManager(val delegate: ScheduleManager,
                            val cacheManager: CacheManager) extends AbstractScheduleManager
with Logging
with BookingEventListener {

  private val cache = cacheManager.getCache("schedule")
  require(cache != null, "Cache doesn't exist")

  override def getSchedule(mailboxes: Set[String], time: TimeSlot): Map[String, Seq[Event]] = {
    val days = toDays(time.begin, time.end)
    // load everything possible from cache
    val cacheResults = mailboxes.map(mailbox => mailbox -> days.map(getDayFromCache(mailbox, _)))
    val cachedSchedule = cacheResults.filter(_._2.forall(_.isDefined)).map(x => x._1 -> x._2.flatten.flatten).toMap
    // load the rest from the delegate
    val mailboxesToReload = mailboxes -- cachedSchedule.keySet
    val reloadedSchedule = reloadSchedule(mailboxesToReload, days)
    // save the loaded piece to cache
    reloadedSchedule.foreach(x => days.foreach(d => putDayToCache(x._1, d, applyTimeWindow(x._2, d, forward(d, day)))))
    // the result
    cachedSchedule ++ reloadedSchedule
  }

  def reloadSchedule(mailboxes: Set[String], days: Seq[Long]): Map[String, Seq[Event]] = {
    if (mailboxes.isEmpty) {
      Map.empty[String, Seq[Event]]
    } else {
      log.info("Reloading days '{}' for resources '{}'", days, mailboxes)
      delegate.getSchedule(mailboxes, TimeSlot(days.head, forward(days.last, day)))
    }
  }

  private def toDays(from: Long, to: Long): Seq[Long] = {
    roundToDayDown(from).until(to, day)
  }

  private def putDayToCache(mailbox: String, day: Long, schedule: Seq[Event]) {
    cache.put(new Element(Key(mailbox, day), schedule))
  }

  private def getDayFromCache(mailbox: String, day: Long): Option[Seq[Event]] = {
    cache.get(Key(mailbox, day)) match {
      case null => None
      case element => Some(element.getObjectValue.asInstanceOf[Seq[Event]])
    }
  }

  private def applyTimeWindow(events: Seq[Event], from: Long, to: Long) = events.filter(x => x.time.end > from && x.time.begin < to)

  private def onEvent(time: TimeSlot, resources: Set[String], attendees: Set[String]) {
    val dayStart = roundToDayDown(time.begin)
    val toInvalidate = attendees ++ resources
    toInvalidate.foreach(mailbox => cache.remove(Key(mailbox, dayStart)))
    log.info("Invalidated cache on day {} for: {}", dayStart, toInvalidate)
  }

  override def onBook(time: TimeSlot, resources: Set[String], attendees: Set[String]) {
    onEvent(time, resources, attendees)
  }

  override def onCancel(time: TimeSlot, resources: Set[String], attendees: Set[String]) {
    onEvent(time, resources, attendees)
  }

}

/**
 * Represents a cache key for a single day.
 */
private case class Key(mailbox: String, dayStart: Long)


