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

/**
 * An abstract class containing code shared between different ScheduleManager implementations.
 * @author Dmitriy Yefremov
 */
abstract class AbstractScheduleManager extends ScheduleManager {

  override def getSchedule(mailboxes: Set[String], times: Seq[TimeSlot]): Map[String, Seq[Event]] = {
    val schedules = times.map(time => {
      val schedule = getSchedule(mailboxes, time)
      offsetSchedule(schedule, -time.begin)
    })
    val emptySchedule = mailboxes.map(mailbox => (mailbox, Seq.empty[Event])).toMap
    val combinedSchedules = schedules.foldLeft(emptySchedule)(combineSchedules)
    offsetSchedule(combinedSchedules, times.head.begin)
  }

  private def offsetSchedule(schedule: Map[String, Seq[Event]], begin: Long): Map[String, Seq[Event]] = {
    schedule.map {
      case (mailbox, events) => (mailbox, events.map(offsetEvent(_, begin)))
    }
  }

  private def offsetEvent(event: Event, begin: Long): Event = {
    val time = event.time
    val newTime = TimeSlot(time.begin + begin, time.end + begin)
    fakeEvent(newTime)
  }

  private def fakeEvent(time: TimeSlot) = Event(0, time)

  private def combineSchedules(s1: Map[String, Seq[Event]], s2: Map[String, Seq[Event]]): Map[String, Seq[Event]] = {
    s1.map {
      case (mailbox, events) => (mailbox, combineSchedules(events, s2(mailbox)))
    }
  }

  private def combineSchedules(e1: Seq[Event], e2: Seq[Event]): Seq[Event] = {
    val events = (e1 ++ e2).sortBy(_.time.begin).toList
    if (events.isEmpty) {
      Seq.empty[Event]
    } else {
      events.tail.foldLeft(List(events.head))((acc, curr) => {
        val prev = acc.head
        if (prev.time.includes(curr.time)) {
          acc
        } else if (prev.time.overlaps(curr.time)) {
          val overlap = fakeEvent(prev.time.combine(curr.time))
          overlap +: acc.tail
        } else {
          // the current element is after the previous
          curr +: acc
        }
      }).reverse
    }
  }

}
