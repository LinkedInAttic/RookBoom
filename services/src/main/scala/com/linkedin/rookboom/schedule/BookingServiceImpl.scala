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

import com.linkedin.rookboom.schedule.dao.{EwsAppointmentDao, InternalScheduleDao}
import com.linkedin.rookboom.util.Logging
import com.linkedin.rookboom.schedule.TrackingStatus._
import com.linkedin.rookboom.util.TimeUtils._
import java.util.TimeZone
import scala.Predef._
import scala.Some
import com.linkedin.rookboom.util.TxUtils._
import org.springframework.transaction.PlatformTransactionManager
import scala.util.control.NonFatal

/**
 * A BookingService implementation that tries to acquire resources first and then adds attendees.
 * @author Dmitriy Yefremov
 */
class BookingServiceImpl(val txManager: PlatformTransactionManager,
                         val appointmentDao: EwsAppointmentDao,
                         val internalDao: InternalScheduleDao,
                         val scheduleManager: ScheduleManager,
                         val listeners: Seq[BookingEventListener],
                         val bookingTimeout: Long,
                         val deletingTimeout: Long) extends BookingService with Logging {

  // TODO how to do it without an extra variable?
  private implicit val implicitTxManager = txManager


  override def book(organizer: String,
                    time: TimeSlot,
                    timeZone: TimeZone,
                    subject: String,
                    body: String,
                    location: String,
                    resources: Set[String],
                    required: Set[String],
                    optional: Set[String],
                    repetition: Option[Repetition]) {
    readWriteTransaction {
      log.info(s"Meeting request received: $organizer, $resources, $time")
      if (resources.isEmpty) {
        doBook(time, timeZone, subject, body, location, repetition, required, optional)
      } else {
        doBook(time, timeZone, subject, body, location, repetition, resources, required, optional)
      }
      log.info("The meeting was created successfully")
      notifyListeners(_.onBook(time, resources, required ++ optional))
    }
  }

  /**
   * Book a meeting with conference room(s)
   */
  private def doBook(time: TimeSlot,
                     timeZone: TimeZone,
                     subject: String,
                     body: String,
                     location: String,
                     repetition: Option[Repetition],
                     resources: Set[String],
                     required: Set[String],
                     optional: Set[String]) {
    // check that the slot is empty
    checkSlotEmpty(resources, time)
    // send booking request
    val appt = failOnError(
      appointmentDao.create(timeZone, time, subject, body, required, optional, resources, location, repetition),
      "Resource booking has failed"
    )
    // track acceptance
    val tracking = failOnError(
      trackAcceptance(appt, resources, bookingTimeout),
      "Can't get tracking information"
    )
    if (tracking.exists(_._2 != Accept)) {
      fail("The room is not confirmed: " + tracking)
    }
    // add attendees if needed
    if (required.nonEmpty || optional.nonEmpty) {
      failOnError(
        appointmentDao.update(appt.uid, timeZone, None, None, None, Some(required), Some(optional), None, None),
        "Failed to add attendees"
      )

    }
  }

  /**
   * Book a meeting with no resources (without a conference room)
   */
  private def doBook(time: TimeSlot,
                     timeZone: TimeZone,
                     subject: String,
                     body: String,
                     location: String,
                     repetition: Option[Repetition],
                     required: Set[String],
                     optional: Set[String]) {
    failOnError(
      appointmentDao.create(timeZone, time, subject, body, required, optional, Set(), location, repetition),
      "Failed to create an appointment"
    )
  }

  private def checkSlotEmpty(resources: Set[String], time: TimeSlot) {
    val schedule = scheduleManager.getSchedule(resources, time)
    val booked = schedule.exists { case (mailbox, events) =>
      events.exists { event =>
        event.time.overlaps(time)
      }
    }
    if (booked) {
      fail("The given slot is already booked")
    }
  }

  private def trackAcceptance(appt: Appointment, resources: Set[String], timeLeft: Long): Map[String, TrackingStatus] = {
    val tracking = appointmentDao.track(appt.uid)
    val filteredTracking = tracking match {
      case Some(t) => t.filter(x => resources.contains(x._1))
      case None => resources.map(_ -> Unknown).toMap
    }
    filteredTracking match {
      case t if !t.exists(_._2 == Unknown) || timeLeft <= 0 => t
      case _ => {
        Thread.sleep(second)
        trackAcceptance(appt, resources, timeLeft - second)
      }
    }
  }

  override def cancel(id: Long) {
    readWriteTransaction {
      log.info("Meeting cancellation received: {}", id)

      val uid = failOnError(
        internalDao.getAppointmentsById(Set(id))(id).extId.get,
        "Can't find the appointment"
      )

      val app = failOnError(
        appointmentDao.read(uid).get,
        "Can't load the appointment"
      )

      failOnError(
        appointmentDao.delete(uid),
        "Failed do delete the appointment"
      )

      val deleted = trackDeleting(uid, deletingTimeout)
      if (!deleted) {
        fail("Meeting deletion timed out")
      }

      log.info("The meeting was cancelled successfully")
      Thread.sleep(2000) //TODO it takes some time for a deleted meeting to be updated in calendars (a better way needed)
      notifyListeners(_.onCancel(app.time, app.resources, app.requiredAttendees ++ app.optionalAttendees))
    }
  }

  private def trackDeleting(uid: String, timeLeft: Long): Boolean = {
    val app = appointmentDao.read(uid)
    app match {
      case None => true
      case Some(_) if timeLeft <= 0 => false
      case _ => {
        Thread.sleep(second)
        trackDeleting(uid, timeLeft - second)
      }
    }

  }

  private def notifyListeners(notification: (BookingEventListener) => Unit) {
    listeners.foreach(listener => {
      try {
        notification(listener)
      } catch {
        case NonFatal(e) => log.warn("Event listener notification exception", e)
      }
    })
  }

  private def failOnError[T](f: => T, msg: String): T = {
    try {
      f
    } catch {
      case NonFatal(e) => fail(msg, e)
    }
  }

  private def fail(msg: String, cause: Throwable = null): Nothing = {
    log.error(msg, cause)
    throw new BookingException(msg, cause)
  }

}
