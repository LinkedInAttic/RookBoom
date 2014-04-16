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

import com.linkedin.rookboom.util.UserVisibleException
import java.util.TimeZone


/**
 * This service encapsulates meeting creation/modification algorithms.
 * @author Dmitriy Yefremov
 */
trait BookingService {

  def book(organizer: String,
           time: TimeSlot,
           timeZone: TimeZone,
           subject: String,
           body: String,
           location: String,
           resources: Set[String],
           required: Set[String],
           optional: Set[String],
           repetition: Option[Repetition])

  def cancel(id: Long)

}

class BookingException(message: String, cause: Throwable = null) extends UserVisibleException(message, cause)

trait BookingEventListener {

  def onBook(time: TimeSlot, resources: Set[String], attendees: Set[String])

  def onCancel(time: TimeSlot, resources: Set[String], attendees: Set[String])

}