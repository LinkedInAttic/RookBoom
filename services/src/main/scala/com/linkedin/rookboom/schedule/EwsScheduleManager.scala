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

import com.linkedin.rookboom.util.{Resolvable, TimeUtils, Reloadable, Logging}
import java.util.concurrent.atomic.AtomicLong
import scala.compat.Platform
import com.linkedin.rookboom.layout.LayoutManager
import com.linkedin.rookboom.user.UserManager
import com.linkedin.rookboom.schedule.dao._
import com.linkedin.rookboom.schedule.dao.EwsEvent
import scala.Some
import com.linkedin.rookboom.user.User
import com.linkedin.rookboom.schedule.dao.InternalEvent
import org.springframework.transaction.PlatformTransactionManager
import com.linkedin.rookboom.util.TxUtils._

/**
 * ScheduleManager implementation based on a microsoft exchange web services api. It uses an internal data store for caching.
 * @author Dmitriy Yefremov
 */
class EwsScheduleManager(val txManager: PlatformTransactionManager,
                         val userManager: UserManager,
                         val layoutManager: LayoutManager,
                         val internalDao: InternalScheduleDao,
                         val ewsDao: EwsScheduleDao,
                         val reloadDays: Int,
                         val resolveDays: Int) extends AbstractScheduleManager
with Logging
with Reloadable
with Resolvable
with BookingEventListener {

  import EwsScheduleManager._

  // TODO how to do it without an extra variable?
  private implicit val implicitTxManager = txManager

  private val availableSlotEnd = new AtomicLong(Platform.currentTime)

  // it is assumed that everything in the past is available
  private def isAvailable(time: TimeSlot): Boolean = time.end <= availableSlotEnd.get()

  override def getSchedule(mailboxes: Set[String], time: TimeSlot): Map[String, Seq[Event]] = {
    readWriteTransaction {
      // different processing for users and resources
      mailboxes.partition(userManager.getUserByAddress(_).isDefined) match {
        case (users, rooms) => {
          val userEvents = getUserSchedule(users, time)
          val roomEvents = getRoomSchedule(rooms, time)
          userEvents ++ roomEvents
        }
      }
    }
  }

  private def getUserSchedule(mailboxes: Set[String], time: TimeSlot): Map[String, Seq[Event]] = {
    ewsDao.getEvents(mailboxes, time).map {
      case (mailbox, events) => (mailbox, events.map(toEvent))
    }
  }

  private def getRoomSchedule(mailboxes: Set[String], time: TimeSlot): Map[String, Seq[Event]] = {
    if (!isAvailable(time)) {
      log.info("{} isn't available locally, fetching from Exchange", time)
      reloadEvents(mailboxes, time)
    }
    // after reloading events everything is available in the database
    val events = internalDao.getEvents(mailboxes, time)
    toSchedule(mailboxes, events)
  }

  private def toEvent(ewsEvent: EwsEvent) = {
    Event(0, ewsEvent.time)
  }

  private def toInternalEvent(ewsEvent: EwsEvent) = {
    InternalEvent(0, ewsEvent.mailbox, ewsEvent.time, ewsEvent.id)
  }

  private def toInternalAppointment(ewsApp: EwsAppointment) = {
    InternalAppointment(0, ewsApp.id, ewsApp.organizer)
  }

  private def toEwsItemId(intEvent: InternalEvent) = {
    EwsItemId(intEvent.extId.get, intEvent.mailbox)
  }

  private def toEvent(intEvent: InternalEvent) = {
    val info = for {
      appointment <- intEvent.appointment
      organizer <- appointment.organizer
    } yield AppointmentInfo(appointment.id, getUser(organizer))
    Event(intEvent.id, intEvent.time, info)
  }

  private def toSchedule(mailboxes: Set[String], events: Seq[InternalEvent]) = {
    val schedule = events.groupBy(_.mailbox).map {
      case (mailbox, intEvents) => (mailbox, intEvents.map(toEvent))
    }
    // add no events mailboxes
    val noEventsSchedule = (mailboxes -- schedule.keySet).map(mailbox => (mailbox, Seq.empty))
    schedule ++ noEventsSchedule
  }

  private def getUser(mailbox: String): User = {
    val user = userManager.getUserByAddress(mailbox)
    user.getOrElse(User(mailbox, mailbox, mailbox))
  }

  private def allRooms: Set[String] = {
    layoutManager.getAll.flatMap(_.rooms).map(_.email).toSet
  }

  private def reloadingSlot(days: Int): TimeSlot = {
    val now = Platform.currentTime
    TimeSlot(now, TimeUtils.forward(Platform.currentTime, TimeUtils.day * days))
  }

  override def reload() {
    readWriteTransaction {
      val time = reloadingSlot(reloadDays)
      val mailboxes = allRooms
      reloadEvents(mailboxes, time)
      availableSlotEnd.set(time.end)
    }
  }

  private def reloadEvents(mailboxes: Set[String], time: TimeSlot) {
    log.info("Reloading events")
    // get existing internal data
    val stored = internalDao.getEvents(mailboxes, time)
    // fetch actual availability
    log.info("Fetching events for {} mailboxes", mailboxes.size)
    val current = ewsDao.getEvents(mailboxes, time).flatMap(_._2).toSeq
    // find newly created appointments
    val created = findCreated(stored, current)
    log.info("Adding {} events", created.size)
    //TODO remove logging
    internalDao.addEvents(created.map(c => {
      val i = toInternalEvent(c)
      log.info("Adding: {}", i)
      i
    }))
    // find deleted appointments
    val deleted = findDeleted(stored, current)
    log.info("Deleting {} events", deleted.size)
    //TODO remove logging
    deleted.foreach {
      d =>
        log.info("Deleting: {}", d)
    }
    internalDao.deleteEvents(deleted.map(_.id).toSet)
    log.info("Reloading completed")
  }

  override def resolve() {
    readWriteTransaction {
      val time = reloadingSlot(resolveDays)
      val mailboxes = allRooms
      resolveAppointments(mailboxes, time)
    }
  }

  private def resolveAppointments(mailboxes: Set[String], time: TimeSlot) {
    log.info("Resolving appointments")
    val storedEvents = internalDao.getEvents(mailboxes, time)
    val unresolvedEvents = storedEvents.filter(e => e.extId.isDefined && e.appointment.isEmpty)
    val idsToConvert = unresolvedEvents.map(toEwsItemId).toSet
    log.info("Converting {} event ids", idsToConvert.size)
    val convertedIds = ewsDao.convertEventIds(idsToConvert)
    val idsToResolve = convertedIds.values.toSet
    log.info("Resolving {} item ids", idsToResolve.size)
    val ewsApps = ewsDao.getAppointments(idsToResolve)
    log.info("Adding {} appointments", ewsApps.size)
    internalDao.addAppointments(ewsApps.values.map(toInternalAppointment).toSeq)
    val intApps = internalDao.getAppointmentsByExtId(ewsApps.flatMap(_._2.id).toSet)
    // map everything back to unresolved events
    val idsToUpdate = unresolvedEvents.flatMap(event =>
      for {
        itemId <- convertedIds.get(toEwsItemId(event))
        ewsApp <- ewsApps.get(itemId)
        extId <- ewsApp.id
        intApp <- intApps.get(extId)
      } yield (event.id, intApp.id)
    ).toMap
    log.info("Updating {} events", idsToUpdate.size)
    internalDao.updateAppointmentIds(idsToUpdate)
    log.info("Resolving completed")
  }

  override def onBook(time: TimeSlot, resources: Set[String], attendees: Set[String]) {
    readWriteTransaction {
      log.info("Meeting created. Reloading schedule for rooms: {}", resources)
      reloadEvents(resources, time)
      resolveAppointments(resources, time)
    }
  }

  override def onCancel(time: TimeSlot, resources: Set[String], attendees: Set[String]) {
    readWriteTransaction {
      log.info("Meeting deleted. Reloading schedule for rooms: {}", resources)
      reloadEvents(resources, time)
    }
  }


}

object EwsScheduleManager {

  /**
   * Returns current events that don't have a corresponding stored event.
   */
  def findCreated(stored: Seq[InternalEvent], current: Seq[EwsEvent]): Seq[EwsEvent] = {
    current.filterNot {
      ext =>
        stored.exists {
          int =>
            isSame(int, ext)
        }
    }
  }

  /**
   * Returns stored events that don't have a corresponding current event.
   */
  def findDeleted(stored: Seq[InternalEvent], current: Seq[EwsEvent]): Seq[InternalEvent] = {
    stored.filterNot {
      int =>
        current.exists {
          ext =>
            isSame(int, ext)
        }
    }
  }

  /**
   * This method is used to match events stored in the database with events just fetched from Exchange.
   * If events are the same, then no change is needed (the sored events remains in the database, the fetched one is ignored).
   * If events are not the same, then the stored event is deleted and the fetched one is saved into the database.
   * @param int the old stored event
   * @param ext the new fetched event
   * @return true if the events are the same, false otherwise
   */
  def isSame(int: InternalEvent, ext: EwsEvent): Boolean = {
    val sameSlot = int.mailbox == ext.mailbox && int.time == ext.time
    val changedProperties = isChanged(int.extId, ext.id)
    sameSlot && !changedProperties
  }

  def isChanged(int: Option[String], ext: Option[String]) = {
    (int, ext) match {
      // both fields are resolved, check if they are equal
      case (Some(i), Some(e)) => i != e
      // none is resolved
      case (None, None) => false
      // internal is resolved - keep internal
      case (Some(_), None) => false
      // external is resolved - update to external
      case (None, Some(_)) => true
    }
  }

}


