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

package com.linkedin.rookboom.schedule.dao

import com.linkedin.rookboom.schedule.TimeSlot

/**
 * Schedule data access interface that uses a local data storage.
 * @author Dmitriy Yefremov
 */
trait InternalScheduleDao {

  def getEventsById(ids: Set[Long]): Map[Long, InternalEvent]

  def getEvents(mailboxes: Set[String], time: TimeSlot): Seq[InternalEvent]

  def addEvents(events: Seq[InternalEvent])

  def deleteEvents(ids: Set[Long])

  /**
   * Updates appointment ids for existing events.
   * @param ids map of event ids to appointment ids to be set
   */
  def updateAppointmentIds(ids: Map[Long, Long])

  /**
   * Adds the given appointments to the database.
   * If there are existing appointments with the same ext_id, the new ones are ignored.
   * @param appointments appointments to add
   */
  def addAppointments(appointments: Seq[InternalAppointment])

  def getAppointmentsById(ids: Set[Long]): Map[Long, InternalAppointment]

  def getAppointmentsByExtId(extIds: Set[String]): Map[String, InternalAppointment]

}

case class InternalEvent(id: Long, mailbox: String, time: TimeSlot, extId: Option[String] = None, appointment: Option[InternalAppointment] = None)

case class InternalAppointment(id: Long, extId: Option[String], organizer: Option[String])