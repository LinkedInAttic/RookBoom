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

import com.linkedin.rookboom.util.Logging
import com.linkedin.rookboom.util.NullSafe.?
import com.microsoft.exchange.messages._
import com.microsoft.exchange.types._
import scala.collection.JavaConversions._
import java.{util => ju}
import java.util.{TimeZone, Date}
import com.linkedin.rookboom.schedule._
import com.microsoft.exchange.utils.TimeZoneHelper
import com.linkedin.rookboom.schedule.dao.EwsUtils._
import com.linkedin.rookboom.schedule.TimeSlot
import scala.Some
import com.linkedin.rookboom.schedule.After
import com.linkedin.rookboom.schedule.Monthly
import com.linkedin.rookboom.schedule.Appointment
import com.linkedin.rookboom.schedule.Weekly
import com.linkedin.rookboom.schedule.Daily
import com.linkedin.rookboom.schedule.By
import com.linkedin.rookboom.schedule.Repetition


/**
 * @author Sergey Skrobotov, sskrobotov@linkedin.com
 */
class EwsAppointmentDaoImpl extends EwsDaoSupport with EwsAppointmentDao with Logging {

  val AllPropertiesShape = new ItemResponseShapeType()
    .withBaseShape(DefaultShapeNamesType.ALL_PROPERTIES)
    .withAdditionalProperties(new NonEmptyArrayOfPathsToElementType().withPath(EwsUtils.CleanGlobalObjectIdElement))

  override def create(timeZone: TimeZone,
                      time: TimeSlot,
                      subject: String,
                      body: String,
                      required: Set[String],
                      optional: Set[String],
                      resources: Set[String],
                      location: String,
                      optRepetition: Option[Repetition]): Appointment = {

    val calendarItem = new CalendarItemType()
      .withSubject(subject)
      .withBody(new BodyType(body, BodyTypeType.HTML))
      .withLocation(location)
      .withStart(new Date(time.begin))
      .withEnd(new Date(time.end))

    if (!required.isEmpty) {
      calendarItem.setRequiredAttendees(attendees(required))
    }
    if (!optional.isEmpty) {
      calendarItem.setOptionalAttendees(attendees(optional))
    }
    if (!resources.isEmpty) {
      calendarItem.setResources(attendees(resources))
    }

    // add repetition parameter, if passed
    optRepetition.foreach(rep => addRepetition(calendarItem, rep))

    val request = new CreateItemType()
      .withMessageDisposition(MessageDispositionType.SAVE_ONLY)
      .withSendMeetingInvitations(CalendarItemCreateOrDeleteOperationType.SEND_TO_ALL_AND_SAVE_COPY)
      .withItems(new NonEmptyArrayOfAllItemsType().withItemOrMessageOrCalendarItem(calendarItem))

    val timeZoneContext = new TimeZoneContextType().withTimeZoneDefinition(timeZoneDef(timeZone))

    val response = invoke[CreateItemResponseType](request, timeZoneContext)

    val app = for {
      message <- getMessage(response.getResponseMessages.getCreateItemResponseMessage, "Appointment creation failed")
      item <- getItem[CalendarItemType](message.getItems.getItemOrMessageOrCalendarItem)
      fullItem <- getFullItem(item.getItemId)
    } yield appointmentFromCalendarItem(fullItem)

    app.getOrElse(
      throw new RuntimeException("Appointment creation failed")
    )
  }

  override def read(uid: String): Option[Appointment] = {
    for {
      item <- findByUid(uid)
      fullItem <- getFullItem(item.getItemId)
    } yield appointmentFromCalendarItem(fullItem)
  }

  //TODO the method doesn't support removing attendees/resources (i.e. Some(Set()))
  override def update(uid: String,
                      timeZone: TimeZone,
                      time: Option[TimeSlot],
                      subject: Option[String],
                      body: Option[String],
                      required: Option[Set[String]],
                      optional: Option[Set[String]],
                      resources: Option[Set[String]],
                      location: Option[String]): Appointment = {

    def itemUpdate[A](field: UnindexedFieldURIType,
                      dataOption: Option[A],
                      modifier: (CalendarItemType, A) => CalendarItemType): Option[SetItemFieldType] = {

      dataOption match {
        case None => None
        case Some(s: Set[_]) if s.isEmpty => None
        case Some(data) => {
          val jaxbFieldURI = TypeFactory.createFieldURI(new PathToUnindexedFieldType(field))
          Some(new SetItemFieldType()
            .withCalendarItem(modifier(new CalendarItemType(), data))
            .withPath(jaxbFieldURI))
        }
      }
    }

    val itemId = findByUid(uid).get.getItemId

    val updates = List(
      itemUpdate(UnindexedFieldURIType.ITEM_SUBJECT, subject, (ci: CalendarItemType, d: String) => {
        ci.withSubject(d)
      }),
      itemUpdate(UnindexedFieldURIType.ITEM_BODY, body, (ci: CalendarItemType, d: String) => {
        ci.withBody(new BodyType(d, BodyTypeType.HTML))
      }),
      itemUpdate(UnindexedFieldURIType.CALENDAR_START, time, (ci: CalendarItemType, d: TimeSlot) => {
        ci.withStart(new Date(d.begin))
      }),
      itemUpdate(UnindexedFieldURIType.CALENDAR_END, time, (ci: CalendarItemType, d: TimeSlot) => {
        ci.withEnd(new Date(d.end))
      }),
      itemUpdate(UnindexedFieldURIType.CALENDAR_LOCATION, location, (ci: CalendarItemType, d: String) => {
        ci.withLocation(d)
      }),
      itemUpdate(UnindexedFieldURIType.CALENDAR_REQUIRED_ATTENDEES, required, (ci: CalendarItemType, d: Set[String]) => {
        ci.withRequiredAttendees(attendees(d))
      }),
      itemUpdate(UnindexedFieldURIType.CALENDAR_OPTIONAL_ATTENDEES, optional, (ci: CalendarItemType, d: Set[String]) => {
        ci.withOptionalAttendees(attendees(d))
      }),
      itemUpdate(UnindexedFieldURIType.CALENDAR_RESOURCES, resources, (ci: CalendarItemType, d: Set[String]) => {
        ci.withResources(attendees(d))
      })
    ).flatten

    val itemChange = new ItemChangeType()
      .withItemId(itemId)
      .withUpdates(new NonEmptyArrayOfItemChangeDescriptionsType(seqAsJavaList(updates)))

    val request = new UpdateItemType()
      .withMessageDisposition(MessageDispositionType.SAVE_ONLY)
      .withConflictResolution(ConflictResolutionType.AUTO_RESOLVE)
      .withSendMeetingInvitationsOrCancellations(CalendarItemUpdateOperationType.SEND_TO_ALL_AND_SAVE_COPY)
      .withItemChanges(new NonEmptyArrayOfItemChangesType().withItemChange(itemChange))

    val timeZoneContext = new TimeZoneContextType().withTimeZoneDefinition(timeZoneDef(timeZone))

    val response = invoke[UpdateItemResponseType](request, timeZoneContext)

    getMessage(response.getResponseMessages.getUpdateItemResponseMessage, "Update failed")

    val fullItem = getFullItem(itemId).getOrElse(
      throw new RuntimeException("Can't load the updated appointment")
    )
    appointmentFromCalendarItem(fullItem)
  }

  override def delete(uid: String) {
    val item = findByUid(uid).getOrElse(
      throw new IllegalArgumentException("Appointment " + uid + " doesn't exist")
    )

    val request = new DeleteItemType()
      .withDeleteType(DisposalType.MOVE_TO_DELETED_ITEMS)
      .withSendMeetingCancellations(CalendarItemCreateOrDeleteOperationType.SEND_ONLY_TO_ALL)
      .withItemIds(new NonEmptyArrayOfBaseItemIdsType().withItemIdOrOccurrenceItemIdOrRecurringMasterItemId(item.getItemId))

    val response = invoke[DeleteItemResponseType](request)

    getMessage(response.getResponseMessages.getDeleteItemResponseMessage, "Delete failed")
  }

  override def track(uid: String): Option[Map[String, TrackingStatus.Value]] = {

    def responseToStatus(response: ResponseTypeType): TrackingStatus.Value = response match {
      case ResponseTypeType.UNKNOWN => TrackingStatus.Unknown
      case ResponseTypeType.NO_RESPONSE_RECEIVED => TrackingStatus.Unknown
      case ResponseTypeType.ACCEPT => TrackingStatus.Accept
      case ResponseTypeType.ORGANIZER => TrackingStatus.Accept
      case ResponseTypeType.TENTATIVE => TrackingStatus.Tentative
      case ResponseTypeType.DECLINE => TrackingStatus.Decline
    }

    for {
      item <- findByUid(uid)
      fullItem <- getFullItem(item.getItemId)
    } yield {
      val attendees = Seq(
        ?(fullItem.getRequiredAttendees.getAttendee.toList).getOrElse(List()),
        ?(fullItem.getOptionalAttendees.getAttendee.toList).getOrElse(List()),
        ?(fullItem.getResources.getAttendee.toList).getOrElse(List())
      ).flatten
      attendees.map(attendee => (attendee.getMailbox.getEmailAddress, responseToStatus(attendee.getResponseType))).toMap
    }
  }

  private def addRepetition(ci: CalendarItemType, repetition: Repetition) {

    val recurrence = new RecurrenceType()

    repetition.pattern match {
      case daily: Daily => recurrence.withDailyRecurrence(
        new DailyRecurrencePatternType(daily.interval)
      )
      case weekly: Weekly => {
        val days = seqAsJavaList(weekly.sortedDays.map(d => mapDay(d)))

        val pattern = new WeeklyRecurrencePatternType()
          .withInterval(weekly.interval)
          .withDaysOfWeek(days)

        recurrence.withWeeklyRecurrence(pattern)
      }
      case monthly: Monthly => {
        val pattern = new RelativeMonthlyRecurrencePatternType()
          .withInterval(monthly.interval)
          .withDaysOfWeek(mapDay(monthly.day))
          .withDayOfWeekIndex(mapWeek(monthly.week))
        recurrence.withRelativeMonthlyRecurrence(pattern)
      }
      case _ => throw new IllegalArgumentException("Unsupported repetition pattern: %s".format(repetition.pattern))
    }

    repetition.end match {
      case after: After => recurrence.withNumberedRecurrence(
        new NumberedRecurrenceRangeType(new Date(repetition.slot.begin), after.number)
      )
      case by: By => recurrence.withEndDateRecurrence(
        new EndDateRecurrenceRangeType(new Date(repetition.slot.begin), new Date(by.date))
      )
      case _ => throw new IllegalArgumentException("Unsupported repetition end: %s".format(repetition.end))
    }

    ci.withRecurrence(recurrence)
  }

  /**
   * Searches for an item in the logged in user's calendar.
   * @param uid item uid
   * @return a calendar item with the given uid, please note that this call doesn't load full details of the item
   */
  private def findByUid(uid: String): Option[CalendarItemType] = {
    val page = new IndexedPageViewType()
      .withMaxEntriesReturned(1)
      .withOffset(0)
      .withBasePoint(IndexBasePointType.BEGINNING)

    val query = new IsEqualToType()
      .withFieldURIOrConstant(new FieldURIOrConstantType().withConstant(new ConstantValueType(uid)))
      .withPath(EwsUtils.CleanGlobalObjectIdElement)

    val jaxbWrappedQuery = TypeFactory.createIsEqualTo(query)

    val calendarFolderId = new DistinguishedFolderIdType().withId(DistinguishedFolderIdNameType.CALENDAR)
    val request = new FindItemType()
      .withTraversal(ItemQueryTraversalType.SHALLOW)
      .withItemShape(AllPropertiesShape)
      .withIndexedPageItemView(page)
      .withRestriction(new RestrictionType().withSearchExpression(jaxbWrappedQuery))
      .withParentFolderIds(new NonEmptyArrayOfBaseFolderIdsType().withFolderIdOrDistinguishedFolderId(calendarFolderId))

    val response = invoke[FindItemResponseType](request)

    for {
      message <- getMessage(response.getResponseMessages.getFindItemResponseMessage, "Find failed")
      item <- getItem[CalendarItemType](message.getRootFolder.getItems.getItemOrMessageOrCalendarItem)
    } yield item
  }

  private def getFullItem(itemId: ItemIdType): Option[CalendarItemType] = {
    val request = new GetItemType()
      .withItemShape(AllPropertiesShape)
      .withItemIds(new NonEmptyArrayOfBaseItemIdsType().withItemIdOrOccurrenceItemIdOrRecurringMasterItemId(itemId))

    val response = invoke[GetItemResponseType](request)

    for {
      message <- getMessage(response.getResponseMessages.getGetItemResponseMessage, "Get item failed")
      item <- getItem[CalendarItemType](message.getItems.getItemOrMessageOrCalendarItem)
    } yield item
  }

  private def appointmentFromCalendarItem(ci: CalendarItemType): Appointment = {
    // required
    val owner = ci.getOrganizer.getMailbox.getEmailAddress
    val uid = getExtendedProperty(ci, CleanGlobalObjectIdProperty).get
    val from = ci.getStart.getTime
    val to = ci.getEnd.getTime
    // optional
    val subject: String = ?(ci.getSubject).getOrElse("")
    val body: String = ?(ci.getBody.getValue).getOrElse("")
    val required: Set[String] = ?(ci.getRequiredAttendees.getAttendee.map(_.getMailbox.getEmailAddress).toSet).getOrElse(Set())
    val optional: Set[String] = ?(ci.getOptionalAttendees.getAttendee.map(_.getMailbox.getEmailAddress).toSet).getOrElse(Set())
    val resources: Set[String] = ?(ci.getResources.getAttendee.map(_.getMailbox.getEmailAddress).toSet).getOrElse(Set())
    val location: String = ?(ci.getLocation).getOrElse("")

    Appointment(uid, owner, TimeSlot(from, to), subject, body, required, optional, resources, location)
  }

  private def timeZoneDef(timeZone: TimeZone): TimeZoneDefinitionType = {
    val windowsId = TimeZoneHelper.getWindowsId(timeZone)
    val ids = new NonEmptyArrayOfTimeZoneIdType().withId(windowsId)

    val request = new GetServerTimeZonesType()
      .withReturnFullTimeZoneData(true)
      .withIds(ids)

    val response = invoke[GetServerTimeZonesResponseType](request)

    val result = for {
      message <- getMessage(response.getResponseMessages.getGetServerTimeZonesResponseMessage, "Time zone request failed")
      tzd <- getItem[TimeZoneDefinitionType](message.getTimeZoneDefinitions.getTimeZoneDefinition)
    } yield tzd
    result.getOrElse(
      throw new RuntimeException("No time zone definition for id: " + timeZone.getID)
    )
  }

  private def attendees(emails: Set[String]): NonEmptyArrayOfAttendeesType = {
    val attendees = emails.map(e => attendeeByEmail(e))
    new NonEmptyArrayOfAttendeesType().withAttendee(setAsJavaSet(attendees))
  }

  private def attendeeByEmail(email: String): AttendeeType = {
    new AttendeeType().withMailbox(new EmailAddressType().withEmailAddress(email))
  }

  private def mapDay(day: DayOfWeek.Value): DayOfWeekType = day match {
    case DayOfWeek.Sunday => DayOfWeekType.SUNDAY
    case DayOfWeek.Monday => DayOfWeekType.MONDAY
    case DayOfWeek.Tuesday => DayOfWeekType.TUESDAY
    case DayOfWeek.Wednesday => DayOfWeekType.WEDNESDAY
    case DayOfWeek.Thursday => DayOfWeekType.THURSDAY
    case DayOfWeek.Friday => DayOfWeekType.FRIDAY
    case DayOfWeek.Saturday => DayOfWeekType.SATURDAY
  }

  private def mapWeek(week: WeekOfMonth.Value): DayOfWeekIndexType = week match {
    case WeekOfMonth.First => DayOfWeekIndexType.FIRST
    case WeekOfMonth.Second => DayOfWeekIndexType.SECOND
    case WeekOfMonth.Third => DayOfWeekIndexType.THIRD
    case WeekOfMonth.Fourth => DayOfWeekIndexType.FOURTH
    case WeekOfMonth.Last => DayOfWeekIndexType.LAST
  }

  private def getMessage[A <: ResponseMessageType](messages: ju.List[A], errorMessage: String = "Message received with an error"): Option[A] = {
    messages.toList match {
      case List(singleMessage) => Some(checkResponseClass(singleMessage, errorMessage))
      case List() => None
      case _ => throw new IllegalStateException("Expected one, but multiple messages received")
    }
  }

  private def checkResponseClass[A <: ResponseMessageType](message: A, errorMessage: String): A = {
    message.getResponseClass match {
      case ResponseClassType.SUCCESS => message
      case _ => throw new RuntimeException(errorMessage + ": " + message.getMessageText)
    }
  }

  private def getItem[A <: AnyRef](items: ju.List[_]): Option[A] = {
    items.toList match {
      case List(singleItem) => Some(singleItem.asInstanceOf[A])
      case List() => None
      case list => throw new IllegalStateException("Expected one, but multiple items received")
    }
  }
}