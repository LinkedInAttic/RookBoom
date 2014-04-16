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
 * DAO for accessing Microsoft Exchange data.
 * @author Dmitriy Yefremov
 */
trait EwsScheduleDao {

  def getEvents(mailboxes: Set[String], time: TimeSlot): Map[String, Seq[EwsEvent]]

  /**
   * Converts the given ids from HEX_ENTRY_ID format into EWS_ID format.
   * @param ids ids to be converted
   * @return map of the given ids to the converted ids
   */
  def convertEventIds(ids: Set[EwsItemId]): Map[EwsItemId, EwsItemId]

  def getAppointments(ids: Set[EwsItemId]): Map[EwsItemId, EwsAppointment]

}

case class EwsItemId(id: String, mailbox: String)

case class EwsEvent(id: Option[String], mailbox: String, time: TimeSlot)

case class EwsAppointment(id: Option[String], organizer: Option[String])
