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

import scala._
import collection.mutable
import java.util.{Date, TimeZone, Calendar}
import java.text.SimpleDateFormat

/**
 * This class describe recurrence of an event.
 * @param slot time slot of the recurrence
 * @param pattern describes how is the event repeated over time
 * @param end condition that ends the recurrence
 * @author Dmitriy Yefremov
 */
case class Repetition(slot: TimeSlot, pattern: RepetitionPattern, end: RepetitionEnd) {

  /**
   * Generates a series of occurrences represented by this repetition configuration.
   * @param now current time in millis
   * @param timeZone time zone of this repetition
   * @return occurrences dates
   */
  def toOccurrences(now: Long, timeZone: TimeZone): Seq[TimeSlot] = {
    val buffer = new mutable.ListBuffer[Long]
    var previous = pattern.first(slot.begin, now, timeZone)
    buffer += previous
    while (!end.isReached(buffer)) {
      val next = pattern.next(previous, timeZone)
      buffer += next
      previous = next
    }
    buffer.result().map(toOccurrence(_, slot.length))
  }

  private def toOccurrence(start: Long, length: Long) = TimeSlot(start, start + length)

  /**
   * Converts this repetition into a human readable string.
   * @param now current time in millis
   * @param timeZone time zone of this repetition
   * @return a string representation
   */
  def toPrettyString(now: Long, timeZone: TimeZone): String = {

    def formatTime(time: Long) = {
      val timeFormatter = new SimpleDateFormat("h:mm a")
      timeFormatter.setTimeZone(timeZone)
      timeFormatter.format(new Date(time))
    }

    def formatStart = {
      val timeFormatter = new SimpleDateFormat("MM/dd/yyyy")
      timeFormatter.setTimeZone(timeZone)
      val first = pattern.first(slot.begin, now, timeZone)
      timeFormatter.format(new Date(first))
    }

    "%s from %s to %s, effective %s, stop %s".format(
      pattern.toPrettyString,
      formatTime(slot.begin),
      formatTime(slot.end),
      formatStart,
      end.toPrettyString(timeZone)
    )
  }

}

/**
 * Days of a week.
 */
object DayOfWeek extends Enumeration {
  type DayOfWeek = Value
  val Sunday, Monday, Tuesday, Wednesday, Thursday, Friday, Saturday = Value

  val WeekDays = Set(Monday, Tuesday, Wednesday, Thursday, Friday)

  val WeekendDays = Set(Sunday, Saturday)
}

/**
 * Weeks of a month.
 */
object WeekOfMonth extends Enumeration {
  type WeekOfMonth = Value
  val First, Second, Third, Fourth, Last = Value
}

/**
 * Describes how is an event repeated over time.
 */
sealed trait RepetitionPattern {

  /**
   * Returns the closest possible date to start this series.
   * @param start start date
   * @return the closest possible date
   */
  def first(start: Long, now: Long, timeZone: TimeZone): Long

  /**
   * Returns next occurrence date according to the pattern.
   * @param previous previous occurrence date
   * @return next occurrence date
   */
  def next(previous: Long, timeZone: TimeZone): Long

  /**
   * Converts this pattern into a human readable string.
   * @return a string representation
   */
  def toPrettyString: String

  /**
   * Returns a calendar that is properly configured and with the start time adjusted to the current time.
   * @param start start time
   * @param now current time
   * @param timeZone time zone
   * @return the calendar object
   */
  protected def baseCalendar(start: Long, now: Long, timeZone: TimeZone) = {
    val calendar = Calendar.getInstance(timeZone)
    calendar.setFirstDayOfWeek(Calendar.SUNDAY)
    calendar.setTimeInMillis(start)
    while (calendar.getTimeInMillis < now) {
      calendar.add(Calendar.DAY_OF_YEAR, 1)
    }
    calendar
  }

}

case class Daily(interval: Int) extends RepetitionPattern {

  require(interval > 0, "interval must be positive")

  override def first(start: Long, now: Long, timeZone: TimeZone) = baseCalendar(start, now, timeZone).getTimeInMillis

  override def next(previous: Long, timeZone: TimeZone) = {
    val calendar = baseCalendar(previous, previous, timeZone)
    calendar.add(Calendar.DAY_OF_YEAR, interval)
    calendar.getTimeInMillis
  }

  override def toPrettyString: String = {

    def formatInterval = interval match {
      case 1 => "day"
      case 2 => "other day"
      case _ => interval + " days"
    }

    "every %s".format(formatInterval)
  }
}

case class Weekly(interval: Int, days: Set[DayOfWeek.Value]) extends RepetitionPattern {

  require(interval > 0, "interval must be positive")
  require(!days.isEmpty, "days set can't be empty")

  val sortedDays = days.toSeq.sorted

  override def first(start: Long, now: Long, timeZone: TimeZone) = find(baseCalendar(start, now, timeZone), today = true, offset = 1)

  override def next(previous: Long, timeZone: TimeZone) = find(baseCalendar(previous, previous, timeZone), today = false, offset = interval)

  private def find(calendar: Calendar, today: Boolean, offset: Int) = {
    val day = DayOfWeek(calendar.get(Calendar.DAY_OF_WEEK) - 1)
    val nextDay = sortedDays.find(x => (x > day) || (today && x == day))
    if (nextDay.isDefined) {
      calendar.set(Calendar.DAY_OF_WEEK, nextDay.get.id + 1)
    } else {
      calendar.add(Calendar.WEEK_OF_YEAR, offset)
      calendar.set(Calendar.DAY_OF_WEEK, sortedDays.head.id + 1)
    }
    calendar.getTimeInMillis
  }

  override def toPrettyString: String = {

    def formatInterval = interval match {
      case 1 => "week"
      case 2 => "other week"
      case _ => interval + " weeks"
    }

    def formatDays = days match {
      case DayOfWeek.WeekDays => "weekdays"
      case DayOfWeek.WeekendDays => "weekend days"
      case _ => sortedDays.mkString(", ")
    }

    "on %s every %s".format(formatDays, formatInterval)
  }

}

case class Monthly(interval: Int, day: DayOfWeek.Value, week: WeekOfMonth.Value) extends RepetitionPattern {

  require(interval > 0, "interval must be positive")

  override def first(start: Long, now: Long, timeZone: TimeZone) = find(baseCalendar(start, now, timeZone), today = true, offset = 1)

  override def next(previous: Long, timeZone: TimeZone) = find(baseCalendar(previous, previous, timeZone), today = false, offset = interval)

  private def find(base: Calendar, today: Boolean, offset: Int) = {
    val calendar = base.clone().asInstanceOf[Calendar]
    setDayAndWeek(calendar)
    val compare = calendar.compareTo(base)
    if (compare < 0 || (compare == 0 && !today)) {
      calendar.add(Calendar.MONTH, offset)
      setDayAndWeek(calendar)
    }
    calendar.getTimeInMillis
  }

  private def setDayAndWeek(calendar: Calendar) {
    val dayOfWeek = day.id + 1
    val weekInMonth = week match {
      case WeekOfMonth.Last => -1
      case x => x.id + 1
    }
    calendar.set(Calendar.DAY_OF_WEEK, dayOfWeek)
    calendar.set(Calendar.DAY_OF_WEEK_IN_MONTH, weekInMonth)
  }

  override def toPrettyString: String = {

    def formatInterval = interval match {
      case 1 => "month"
      case 2 => "other month"
      case _ => interval + " months"
    }

    def formatDay = day.toString

    def formatWeek = week.toString.toLowerCase


    "on the %s %s of every %s".format(formatWeek, formatDay, formatInterval)
  }
}

/**
 * Describes a condition to end a recurrent event.
 */
sealed trait RepetitionEnd {

  /**
   * Checks if the end condition is met for the given occurrences.
   * @param occurrences occurrences to check
   * @return true if the end is reached, false otherwise
   */
  def isReached(occurrences: Seq[Long]): Boolean

  /**
   * Converts this pattern into a human readable string.
   * @param timeZone time zone to use
   * @return a string representation
   */
  def toPrettyString(timeZone: TimeZone): String

}

case class After(number: Int) extends RepetitionEnd {

  require(number > 0, "number of occurrences must be positive")

  override def isReached(occurrences: Seq[Long]) = occurrences.size >= number

  override def toPrettyString(timeZone: TimeZone): String = {

    def formatNumber = number match {
      case 1 => "1 occurrence"
      case _ => number + " occurrences"
    }

    "after %s".format(formatNumber)
  }
}

case class By(date: Long) extends RepetitionEnd {

  require(date > 0, "date must be positive")

  override def isReached(occurrences: Seq[Long]) = occurrences.lastOption.exists(_ >= date)

  override def toPrettyString(timeZone: TimeZone): String = {

    def formatDate = {
      val timeFormatter = new SimpleDateFormat("MM/dd/yyyy")
      timeFormatter.setTimeZone(timeZone)
      timeFormatter.format(new Date(date))
    }

    "by %s".format(formatDate)
  }
}
