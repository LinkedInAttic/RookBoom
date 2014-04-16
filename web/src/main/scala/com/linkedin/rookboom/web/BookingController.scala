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

import dust.engine.DustEngineFactory
import org.springframework.stereotype.Controller
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.{RequestMapping, RequestParam}
import com.linkedin.rookboom.schedule._
import com.linkedin.rookboom.layout.LayoutManager
import java.security.Principal
import java.io.StringWriter
import collection.JavaConverters._

/**
 * Serves meetings booking requests.
 * @author Dmitriy Yefremov
 */
@Controller
@RequestMapping(Array("booking"))
class BookingController extends ExceptionResolver {

  @Autowired val layoutManager: LayoutManager = null
  @Autowired val dustEngineFactory: DustEngineFactory = null
  @Autowired val bookingService: BookingService = null
  @Autowired val rookboomUrl: String = null

  @RequestMapping(Array("create"))
  def handleCreate(principal: Principal,
                   @RequestParam("from") from: Long,
                   @RequestParam("to") to: Long,
                   @RequestParam(value = "meetingLocation", defaultValue = "") meetingLocation: String,
                   @RequestParam(value = "location", defaultValue = "") location: String,
                   @RequestParam(value = "subject", defaultValue = "") subject: String,
                   @RequestParam(value = "body", defaultValue = "") body: String,
                   @RequestParam(value = "resource", defaultValue = "") resources: Array[String],
                   @RequestParam(value = "required", defaultValue = "") requiredAttendees: Array[String],
                   @RequestParam(value = "optional", defaultValue = "") optionalAttendees: Array[String],
                   @RequestParam(value = "repPattern", defaultValue = "") repPattern: String,
                   @RequestParam(value = "repInterval", defaultValue = "0") repInterval: Int,
                   @RequestParam(value = "repDays", defaultValue = "") repDay: Array[String],
                   @RequestParam(value = "repWeek", defaultValue = "") repWeek: String,
                   @RequestParam(value = "repAfter", defaultValue = "0") repAfter: Int,
                   @RequestParam(value = "repBy", defaultValue = "0") repBy: Long) = {

    val timeZone = layoutManager.getLayout(location).get.timezone
    val slot = TimeSlot(from, to)
    val repetition = RepetitionHelper.getRepetition(slot, repPattern, repInterval, repDay, repWeek, repAfter, repBy)
    bookingService.book(principal.getName, slot, timeZone, subject, getBody(body), meetingLocation, resources.toSet,
      requiredAttendees.toSet, optionalAttendees.toSet, repetition)
    val room = if (resources.isEmpty) {
      None
    } else {
      layoutManager.getAll.flatMap(_.rooms).find(_.email.equals(resources.head))
    }

    Map(
      "success" -> true,
      "room" -> room
    ).asJava
  }

  @RequestMapping(Array("cancel"))
  def handleCancel(principal: Principal,
                   @RequestParam("id") appointmentId: Long) = {

    bookingService.cancel(appointmentId)

    Map(
      "success" -> true
    ).asJava
  }

  private def getBody(body: String) = {
    val writer = new StringWriter()
    dustEngineFactory.getEngine.render("appointment_body", Map("text" -> body, "url" -> rookboomUrl), writer)
    writer.toString
  }

}
