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

package com.linkedin.rookboom.web

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import scala.collection.JavaConverters._
import org.springframework.web.bind.annotation.{RequestParam, RequestMapping}
import com.linkedin.rookboom.layout.LayoutManager
import com.linkedin.rookboom.schedule._
import com.linkedin.rookboom.user.UserManager
import compat.Platform
import com.linkedin.rookboom.schedule.TimeSlot
import scala.Some
import com.linkedin.rookboom.schedule.TimeMask
import com.linkedin.rookboom.layout.Room
import java.util.{Date, TimeZone}
import java.text.SimpleDateFormat
import com.linkedin.rookboom.util.TimeUtils


case class ScheduleRecord(room: Room, schedule: Seq[AvailabilityRecord])

case class AvailabilityRecord(from: Long, to: Long, busy: Boolean, eventId: Int = 0)

@Controller
@RequestMapping(Array("schedule"))
class ScheduleController extends ExceptionResolver {

  @Autowired
  val scheduleManager: ScheduleManager = null

  @Autowired
  val timeMaskManager: TimeMaskManager = null

  @Autowired
  val layoutManager: LayoutManager = null

  @Autowired
  val userManager: UserManager = null

  @RequestMapping
  def getSchedule(@RequestParam(value = "day", defaultValue = "0") fromParam: Long,
                  @RequestParam(value = "timeframe", defaultValue = "Day") timeframe: String,
                  @RequestParam(value = "location", defaultValue = "") location: String,
                  @RequestParam(value = "repPattern", defaultValue = "") repPattern: String,
                  @RequestParam(value = "repInterval", defaultValue = "0") repInterval: Int,
                  @RequestParam(value = "repDays", defaultValue = "") repDay: Array[String],
                  @RequestParam(value = "repWeek", defaultValue = "") repWeek: String,
                  @RequestParam(value = "repAfter", defaultValue = "0") repAfter: Int,
                  @RequestParam(value = "repBy", defaultValue = "0") repBy: Long) = {

    val layout = layoutManager.getLayout(location).get

    val originalSlot = timeSlot(fromParam)
    val repetition = RepetitionHelper.getRepetition(originalSlot, repPattern, repInterval, repDay, repWeek, repAfter, repBy)
    val slot = repetition match {
      case None => originalSlot
      case Some(rep) => rep.toOccurrences(originalSlot.begin, layout.timezone).head
    }

    val roomByEmail = layout.rooms.map(r => (r.email, r)).toMap
    val emails = roomByEmail.keySet
    val mask = timeMaskManager.getMask(slot.begin, Timeframe.withName(timeframe))

    val schedule = loadSchedule(emails, slot, repetition, layout.timezone)

    val fullSchedule = schedule.map { case (email, events) => 
      ScheduleRecord(roomByEmail(email), convertSchedule(email, events, mask))
    }

    val allEvents = schedule.map { case (email, events) =>
      events.map(e => (email, e))
    }.flatten.map(p => (p.hashCode(), p._2)).toMap

    Map("day" -> slot.begin, "schedule" -> fullSchedule, "events" -> allEvents).asJava
  }

  @RequestMapping(Array("rep-info"))
  def getRepetitionInfo(@RequestParam(value = "repFrom", defaultValue = "0") fromParam: Long,
          @RequestParam(value = "location", defaultValue = "") location: String,
          @RequestParam(value = "repPattern", defaultValue = "") repPattern: String,
          @RequestParam(value = "repInterval", defaultValue = "0") repInterval: Int,
          @RequestParam(value = "repDays", defaultValue = "") repDay: Array[String],
          @RequestParam(value = "repWeek", defaultValue = "") repWeek: String,
          @RequestParam(value = "repAfter", defaultValue = "0") repAfter: Int,
          @RequestParam(value = "repBy", defaultValue = "0") repBy: Long) = {

    val layout = layoutManager.getLayout(location).get
    val slot = timeSlot(fromParam)
    val repetition = RepetitionHelper.getRepetition(slot, repPattern, repInterval, repDay, repWeek, repAfter, repBy)

    val result = repetition match {
      case None => Map.empty
      case Some(rep) =>
        rep.toOccurrences(slot.begin, layout.timezone).toList match {
          case some :: tail => Map(
            "description" -> repetitionDescription(rep, layout.timezone),
            "first" -> some.begin
          )
          case _ => Map.empty
        }
    }

    result.asJava
  }

  @Deprecated //use getUsers instead
  @RequestMapping(Array("user"))
  def getUserSchedules(@RequestParam("email") email: String,
              @RequestParam(value = "timeframe", defaultValue = "Day") timeframe: String,
              @RequestParam(value = "day", defaultValue = "0") fromParam: Long,
              @RequestParam(value = "location", defaultValue = "") location: String,
              @RequestParam(value = "repPattern", defaultValue = "") repPattern: String,
              @RequestParam(value = "repInterval", defaultValue = "0") repInterval: Int,
              @RequestParam(value = "repDays", defaultValue = "") repDay: Array[String],
              @RequestParam(value = "repWeek", defaultValue = "") repWeek: String,
              @RequestParam(value = "repAfter", defaultValue = "0") repAfter: Int,
              @RequestParam(value = "repBy", defaultValue = "0") repBy: Long) = {

    val originalSlot = timeSlot(fromParam)
    val repetition = RepetitionHelper.getRepetition(originalSlot, repPattern, repInterval, repDay, repWeek, repAfter, repBy)
    val zone = timeZone(location)
    val slot = repetition match {
      case None => originalSlot
      case Some(rep) => rep.toOccurrences(originalSlot.begin, zone).head
    }
    val mask = timeMaskManager.getMask(slot.begin, Timeframe.withName(timeframe))
    userSchedules(Set(email), slot, mask, repetition, zone).head.asJava
  }

  @RequestMapping(Array("users"))
  def getUsersSchedules(@RequestParam("emails") emails: Array[String],
               @RequestParam(value = "timeframe", defaultValue = "Day") timeframe: String,
               @RequestParam(value = "day", defaultValue = "0") fromParam: Long,
               @RequestParam(value = "location", defaultValue = "") location: String,
               @RequestParam(value = "repPattern", defaultValue = "") repPattern: String,
               @RequestParam(value = "repInterval", defaultValue = "0") repInterval: Int,
               @RequestParam(value = "repDays", defaultValue = "") repDay: Array[String],
               @RequestParam(value = "repWeek", defaultValue = "") repWeek: String,
               @RequestParam(value = "repAfter", defaultValue = "0") repAfter: Int,
               @RequestParam(value = "repBy", defaultValue = "0") repBy: Long) = {
    val originalSlot = timeSlot(fromParam)
    val repetition = RepetitionHelper.getRepetition(originalSlot, repPattern, repInterval, repDay, repWeek, repAfter, repBy)
    val zone = timeZone(location)
    val slot = repetition match {
      case None => originalSlot
      case Some(rep) => rep.toOccurrences(originalSlot.begin, zone).head
    }
    val mask = timeMaskManager.getMask(slot.begin, Timeframe.withName(timeframe))
    Map("day" -> slot.begin, "result" -> userSchedules(emails.toSet, slot, mask, repetition, zone)).asJava
  }

  @RequestMapping(Array("timemask"))
  def getTimeMask(@RequestParam(value = "day", defaultValue = "0") from: Long,
                  @RequestParam(value = "timeframe", defaultValue = "Day") timeframe: String) = {
    val mask = timeMaskManager.getMask(from, Timeframe.withName(timeframe))
    Map("day" -> from, "timeMask" -> mask).asJava
  }


  private def userSchedules(emails: Set[String], slot: TimeSlot, mask: TimeMask, repetition: Option[Repetition], timeZone: TimeZone): List[Map[String, Equals]] = {

    val grouped = emails.groupBy(userManager.getUserByAddress(_).isDefined)

    val usersSchedule = grouped.get(true) match {
      case None => List.empty
      case Some(users) => loadSchedule(users, slot, repetition, timeZone).map{ case (email, events) =>
        val sch = convertSchedule(email, events, mask)
        Map("schedule" -> sch, "user" -> userManager.getUserByAddress(email))
      }.toList
    }

    val groupsSchedule = grouped.get(false) match {
      case None => List.empty
      case Some(groups) => groups.map(email => {
        val sch = convertSchedule(email, Seq(), mask)
        Map("schedule" -> sch, "user" -> userManager.getGroupByAddress(email))
      }).toList
    }

    usersSchedule ::: groupsSchedule
  }

  private def loadSchedule(emails: Set[String], slot: TimeSlot, repetition: Option[Repetition], timeZone: TimeZone): Map[String, Seq[Event]] = {
    repetition match {
      case None => scheduleManager.getSchedule(emails, slot)
      case Some(rep) =>
        val times = rep.toOccurrences(slot.begin, timeZone)
        scheduleManager.getSchedule(emails, times)
    }
  }

  private def convertSchedule(email: String, events: Seq[Event], mask: TimeMask): Seq[AvailabilityRecord] = {
    mask.frames.map { frame =>
      val slot = TimeSlot(frame + 1, frame + mask.interval - 1)
      val eventOption = events.find(_.time.overlaps(slot))
      val id = eventOption.fold(0)(eventId(email, _))
      AvailabilityRecord(slot.begin, slot.end, busy = eventOption.isDefined, id)
    }
  }

  private def eventId(email: String, event: Event): Int = {
    (email, event).hashCode()
  }

  private def timeSlot(fromParam: Long): TimeSlot = {
    val from = fromParam match {
      case 0 => TimeUtils.roundToDayDown(Platform.currentTime)
      case some => some
    }
    val to = from + TimeUtils.day

    TimeSlot(from, to)
  }

  private def repetitionDescription(rep: Repetition, timeZone: TimeZone): String = {

    def formatStart = {
      val timeFormatter = new SimpleDateFormat("MM/dd/yyyy")
      timeFormatter.setTimeZone(timeZone)
      val first = rep.pattern.first(rep.slot.begin, rep.slot.begin, timeZone)
      timeFormatter.format(new Date(first))
    }

    "%s, effective %s, stop %s".format(
      rep.pattern.toPrettyString,
      formatStart,
      rep.end.toPrettyString(timeZone)
    )
  }

  private def timeZone(location: String): TimeZone = layoutManager.getLayout(location).get.timezone
}
