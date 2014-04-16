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

import com.linkedin.rookboom.user.User

/**
 * This service is used to get scheduling information.
 *
 * @author Dmitriy Yefremov
 */
trait ScheduleManager {

  /**
   * Returns schedule for the specified set of mailboxes and the given time frame.
   * @param mailboxes set of mailboxes
   * @param time time slot
   * @return a map of mailboxes to their schedule
   */
  def getSchedule(mailboxes: Set[String], time: TimeSlot): Map[String, Seq[Event]]

  /**
   * Returns schedule for the specified set of mailboxes and the given set of time slots.
   * @param mailboxes set of mailboxes
   * @param times set of time slots
   * @return a map of mailboxes to their schedule
   */
  def getSchedule(mailboxes: Set[String], times: Seq[TimeSlot]): Map[String, Seq[Event]]
}

/**
 * Represents a time slot.
 * @param begin start time
 * @param end end time
 */
case class TimeSlot(begin: Long, end: Long) {

  require(end >= begin, "end time must not me earlier then start time")

  def length = end - begin

  def includes(that: TimeSlot): Boolean = {
    this.begin <= that.begin && this.end >= that.end
  }

  def overlaps(that: TimeSlot): Boolean = {
    this.end >= that.begin && this.begin <= that.end
  }

  def combine(that: TimeSlot): TimeSlot = {
    require(overlaps(that), "can only combine overlapping intervals")
    val begin = math.min(this.begin, that.begin)
    val end = math.max(this.end, that.end)
    TimeSlot(begin, end)
  }

  /**
   * Split this time slot into a series of slot with length of no more the the given maximum length.
   * @param maxLength the maximum length of the resulting slots
   * @return resulting slots (may contain only one element)
   */
  def split(maxLength: Long): Seq[TimeSlot] = {

    def split(begin: Long, end: Long, acc: List[TimeSlot]): List[TimeSlot] = {
      if (end - begin <= maxLength) {
        TimeSlot(begin, end) :: acc
      } else {
        val slot = TimeSlot(begin, begin + maxLength)
        split(begin + maxLength + 1, end, slot :: acc)
      }
    }

    require(maxLength > 0, "can only split by positive length")
    split(begin, end, Nil).reverse
  }

}

/**
 * Represents a calendar event.
 */
case class Event(@Deprecated id: Long, time: TimeSlot, appointment: Option[AppointmentInfo] = None)

/**
 * Contains information about an appointment of an event.
 */
case class AppointmentInfo(id: Long, owner: User)
