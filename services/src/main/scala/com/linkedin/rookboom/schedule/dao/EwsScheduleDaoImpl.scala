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

import com.microsoft.exchange.messages._
import com.microsoft.exchange.types._
import com.linkedin.rookboom.util.TimeUtils._

import scala.collection.JavaConversions._
import com.linkedin.rookboom.util.Logging

import com.linkedin.rookboom.util.NullSafe.?
import com.microsoft.exchange.utils.TimeZoneHelper
import EwsUtils._
import com.linkedin.rookboom.schedule.TimeSlot
import scala.Some
import scala.util.Try

class EwsScheduleDaoImpl extends EwsDaoSupport with EwsScheduleDao with Logging {

  // no more then 100 mailboxes per request (defined by the spec)
  private val MaxAvailabilityMailboxes = 100

  // maximum length of availability request time window
  private val MaxAvailabilityDays = 62

  // maximum number of ids to be converted per request
  private val MaxConvertIdItems = 1000

  // maximum number of ids to be converted per request
  private val MaxGetItemIds = 100

  private val DefaultTimeZone = TimeZoneHelper.defaultTimeZone()

  override def getEvents(mailboxes: Set[String], time: TimeSlot): Map[String, Seq[EwsEvent]] = {
    if (mailboxes.isEmpty) {
      Map.empty
    } else {
      val mailboxGroups = mailboxes.toSeq.grouped(MaxAvailabilityMailboxes)
      val times = time.split(MaxAvailabilityDays * day)
      times.flatMap(t =>
        mailboxGroups.flatMap(m =>
          getAvailability(m, t)
        )
      ).toMap
    }
  }

  private def getAvailability(mailboxes: Seq[String], time: TimeSlot): Map[String, Seq[EwsEvent]] = {
    val fbViewOptions = new FreeBusyViewOptionsType(time, null, List("Detailed"))

    val mailboxData = mailboxes.map(new MailboxData(_, MeetingAttendeeType.REQUIRED, true))
    val mailboxDataArray = new ArrayOfMailboxData(mailboxData.toList)

    val request = new GetUserAvailabilityRequestType(DefaultTimeZone, mailboxDataArray, fbViewOptions, null)

    val response = invoke[GetUserAvailabilityResponseType](request)

    val responses = mailboxes.zip(response.getFreeBusyResponseArray.getFreeBusyResponse)

    // partition into successes and failures
    responses.partition(_._2.getResponseMessage.getResponseClass == ResponseClassType.SUCCESS) match {
      case (successful, failed) => {
        // print failures
        failed.foreach(f =>
          log.warn("Error getting availability for '{}': {}", f._1, f._2.getResponseMessage.getMessageText)
        )
        // return only successful
        successful.map(s => (s._1, handleFreeBusyResponse(s._1, s._2))).toMap
      }
    }
  }


  private def handleFreeBusyResponse(mailbox: String, response: FreeBusyResponseType): Seq[EwsEvent] = {
    ?(response.getFreeBusyView.getCalendarEventArray.getCalendarEvent) match {
      case Some(calendarEvents) => {
        // filter out 'free' and 'tentative' events
        val busyEvents = calendarEvents.filter {
          event =>
            LegacyFreeBusyType.BUSY == event.getBusyType || LegacyFreeBusyType.OOF == event.getBusyType
        }
        busyEvents.map {
          event =>
            val id = ?(event.getCalendarEventDetails.getID)
            EwsEvent(id, mailbox, TimeSlot(event.getStartTime, event.getEndTime))
        }
      }
      case None => Seq.empty
    }
  }


  override def convertEventIds(ids: Set[EwsItemId]): Map[EwsItemId, EwsItemId] = {
    if (ids.isEmpty) {
      Map.empty
    } else {
      ids.toSeq.grouped(MaxConvertIdItems).flatMap(convertId).toMap
    }
  }

  private def convertId(ids: Seq[EwsItemId]): Map[EwsItemId, EwsItemId] = {
    val alternateIds = ids.map(id => new AlternateIdType(IdFormatType.HEX_ENTRY_ID, id.id, id.mailbox, null))
    val sourceIds = new NonEmptyArrayOfAlternateIdsType(alternateIds)
    val request = new ConvertIdType(sourceIds, IdFormatType.EWS_ID)

    val response = invoke[ConvertIdResponseType](request)

    val responses = ids.zip(response.getResponseMessages.getConvertIdResponseMessage)

    responses.partition(_._2.getResponseClass == ResponseClassType.SUCCESS) match {
      case (successful, failed) => {
        // print failures
        failed.foreach(f =>
          log.warn("Error converting ids for '{}': {}", f._1, f._2.getMessageText)
        )
        // return only successful
        successful.flatMap(s => handleConvertIdResponse(s._1, s._2).map(id => (s._1, id))).toMap
      }
    }
  }

  private def handleConvertIdResponse(id: EwsItemId, response: ConvertIdResponseMessageType): Option[EwsItemId] = {
    ?(response.getAlternateId).map {
      case convertedId: AlternateIdType => EwsItemId(convertedId.getId, id.mailbox)
    }
  }

  override def getAppointments(ids: Set[EwsItemId]): Map[EwsItemId, EwsAppointment] = {
    if (ids.isEmpty) {
      Map.empty
    } else {
      // order ids by mailbox in order to make it easier for Exchange to fetch
      val ordered = ids.toSeq.sortBy(_.mailbox)
      val grouped = ordered.grouped(MaxGetItemIds)
      grouped.flatMap(group =>
      // don't fail the entire method call if one group has failed
        Try(getItem(group)).recover {
          case e => {
            log.error("Error loading appointments: {}", e.getMessage)
            Map.empty[EwsItemId, EwsAppointment]
          }
        }.get
      ).toMap
    }
  }

  private def getItem(ids: Seq[EwsItemId]): Map[EwsItemId, EwsAppointment] = {
    val itemIds = new NonEmptyArrayOfBaseItemIdsType(ids.map(id => new ItemIdType(id.id, null)))
    // request only the properties we really need (that makes the request much faster)
    val additionalProperties = new NonEmptyArrayOfPathsToElementType().withPath(
      CleanGlobalObjectIdElement,
      TypeFactory.createFieldURI(new PathToUnindexedFieldType(UnindexedFieldURIType.CALENDAR_ORGANIZER))
    )
    val itemShape = new ItemResponseShapeType()
      .withBaseShape(DefaultShapeNamesType.ID_ONLY)
      .withAdditionalProperties(additionalProperties)
    val request = new GetItemType()
      .withItemShape(itemShape)
      .withItemIds(itemIds)

    val response = invoke[GetItemResponseType](request)

    val responses = ids.zip(response.getResponseMessages.getGetItemResponseMessage)

    responses.partition(_._2.getResponseClass == ResponseClassType.SUCCESS) match {
      case (successful, failed) => {
        // print failures grouped by error message
        failed.groupBy(_._2.getMessageText).foreach(f =>
          log.warn("Error loading appointments for {} ids: {}", f._2.size, f._1)
        )
        // return only successful
        successful.flatMap(s => handleGetItemResponse(s._2).map(app => (s._1, app))).toMap
      }
    }
  }

  private def handleGetItemResponse(response: ItemInfoResponseMessageType): Option[EwsAppointment] = {
    ?(response.getItems.getItemOrMessageOrCalendarItem).map(_.toList match {
      case (item: CalendarItemType) :: Nil => {
        val id = getExtendedProperty(item, CleanGlobalObjectIdProperty)
        val owner = ?(item.getOrganizer.getMailbox.getEmailAddress)
        EwsAppointment(id, owner)
      }
      case _ => throw new IllegalStateException("There are multiple items in ItemInfoResponseMessageType response")
    })
  }

  private implicit def timeSlotToDuration(time: TimeSlot): Duration = new Duration(time.begin, time.end)

  private implicit def timeSlotToCalendarView(time: TimeSlot): CalendarViewType = new CalendarViewType(null, time.begin, time.end)

  private implicit def stringToEmailAddress(mailbox: String): EmailAddress = new EmailAddress().withAddress(mailbox)

  private implicit def stringToEmailAddressType(mailbox: String): EmailAddressType = new EmailAddressType().withEmailAddress(mailbox)

}