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

import com.linkedin.rookboom.schedule.{TrackingStatus, Appointment, Repetition, TimeSlot}
import java.util.TimeZone

/**
 * @author Sergey Skrobotov, sskrobotov@linkedin.com
 */
trait EwsAppointmentDao {

  def create(timeZone: TimeZone,
             time: TimeSlot,
             subject: String,
             body: String,
             required: Set[String],
             optional: Set[String],
             resources: Set[String],
             location: String,
             optRepetition: Option[Repetition]): Appointment

  def read(uid: String): Option[Appointment]

  def update(uid: String,
             timeZone: TimeZone,
             time: Option[TimeSlot],
             subject: Option[String],
             body: Option[String],
             required: Option[Set[String]],
             optional: Option[Set[String]],
             resources: Option[Set[String]],
             location: Option[String]): Appointment

  def delete(uid: String)

  def track(uid: String): Option[Map[String, TrackingStatus.Value]]
}