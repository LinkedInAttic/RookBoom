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

import javax.sql.DataSource
import org.springframework.jdbc.core.namedparam.{SqlParameterSource, MapSqlParameterSource, NamedParameterJdbcTemplate}
import org.springframework.jdbc.core.RowMapper
import java.sql.ResultSet
import scala.collection.JavaConversions._
import com.linkedin.rookboom.schedule.TimeSlot
import com.linkedin.rookboom.util.NullSafe.anyToOption

/**
 * An implementation of the ScheduleDao class that uses JDBC to get data from an SQL database.
 * @author Dmitriy Yefremov
 */
class InternalScheduleDaoImpl(val dataSource: DataSource) extends InternalScheduleDao {

  private val jdbcTemplate = new NamedParameterJdbcTemplate(dataSource)

  private val eventRowMapper = new RowMapper[InternalEvent] {
    def mapRow(rs: ResultSet, rowNum: Int) = {
      // event properties
      val eventId = rs.getLong("event_id")
      val mailbox = rs.getString("mailbox")
      val start = rs.getLong("start")
      val end = rs.getLong("end")
      val eventExtId = rs.getString("event_ext_id")
      // appointment properties
      val appId = rs.getLong("app_id")
      val appointment = if (rs.wasNull()) {
        None
      } else {
        val appExtId = rs.getString("app_ext_id")
        val organizer = rs.getString("organizer")
        Some(InternalAppointment(appId, appExtId, organizer))
      }
      InternalEvent(eventId, mailbox, TimeSlot(start, end), eventExtId, appointment)
    }
  }

  private val appointmentRowMapper = new RowMapper[InternalAppointment] {
    def mapRow(rs: ResultSet, rowNum: Int) = {
      val appId = rs.getLong("id")
      val appExtId = rs.getString("ext_id")
      val organizer = rs.getString("organizer")
      InternalAppointment(appId, appExtId, organizer)
    }
  }

  override def getEventsById(ids: Set[Long]): Map[Long, InternalEvent] = {
    if (ids.isEmpty) {
      return Map.empty
    }
    val sql = "SELECT e.id event_id, mailbox, start, end, e.ext_id event_ext_id, a.id app_id, a.ext_id app_ext_id, organizer " +
      "FROM event e LEFT JOIN appointment a ON e.appointment_id = a.id " +
      "WHERE e.id IN (:ids)"
    val params = Map("ids" -> setAsJavaSet(ids))
    val events = jdbcTemplate.query(sql, params, eventRowMapper)
    events.map(e => (e.id, e)).toMap
  }

  override def getEvents(mailboxes: Set[String], time: TimeSlot): Seq[InternalEvent] = {
    if (mailboxes.isEmpty) {
      return Seq.empty
    }
    val sql = "SELECT e.id event_id, mailbox, start, end, e.ext_id event_ext_id, a.id app_id, a.ext_id app_ext_id, organizer " +
      "FROM event e LEFT JOIN appointment a ON e.appointment_id = a.id " +
      "WHERE e.mailbox IN (:mailboxes) AND e.end > :from AND e.start < :to"
    val params = Map(
      "mailboxes" -> setAsJavaSet(mailboxes),
      "from" -> time.begin,
      "to" -> time.end
    )
    jdbcTemplate.query(sql, params, eventRowMapper).toSeq
  }

  override def addEvents(events: Seq[InternalEvent]) {
    if (events.isEmpty) {
      return
    }
    val sql = "INSERT INTO event (mailbox, start, end, ext_id, appointment_id) " +
      "VALUES (:mailbox, :start, :end, :ext_id, :appointment_id)"
    val batchParams = events.map(event => {
      val params = Map(
        "mailbox" -> event.mailbox,
        "start" -> event.time.begin,
        "end" -> event.time.end,
        "ext_id" -> event.extId.getOrElse(null),
        "appointment_id" -> event.appointment.map(_.id).getOrElse(null)
      )
      new MapSqlParameterSource(params)
    })
    jdbcTemplate.batchUpdate(sql, batchParams.toArray[SqlParameterSource])
  }

  override def deleteEvents(ids: Set[Long]) {
    if (ids.isEmpty) {
      return
    }
    val sql = "DELETE FROM event WHERE id IN (:ids)"
    val params = Map("ids" -> setAsJavaSet(ids))
    jdbcTemplate.update(sql, params)
  }

  override def updateAppointmentIds(ids: Map[Long, Long]) {
    if (ids.isEmpty) {
      return
    }
    val sql = "UPDATE event SET appointment_id = :appointment_id WHERE id = :id"
    val batchParams = ids.map {
      case (eventId, appId) => {
        val params = Map(
          "id" -> eventId,
          "appointment_id" -> appId
        )
        new MapSqlParameterSource(params)
      }
    }
    jdbcTemplate.batchUpdate(sql, batchParams.toArray[SqlParameterSource])
  }

  override def addAppointments(appointments: Seq[InternalAppointment]) {
    if (appointments.isEmpty) {
      return
    }
    val sql = "INSERT IGNORE INTO appointment (ext_id, organizer) VALUES (:ext_id, :organizer)"
    val batchParams = appointments.map(app => {
      val params = Map(
        "ext_id" -> app.extId.getOrElse(null),
        "organizer" -> app.organizer.getOrElse(null)
      )
      new MapSqlParameterSource(params)
    })
    jdbcTemplate.batchUpdate(sql, batchParams.toArray[SqlParameterSource])
  }

  override def getAppointmentsById(ids: Set[Long]): Map[Long, InternalAppointment] = {
    if (ids.isEmpty) {
      return Map.empty
    }
    val sql = "SELECT * FROM appointment WHERE id IN (:ids)"
    val params = Map("ids" -> setAsJavaSet(ids))
    val appointments = jdbcTemplate.query(sql, params, appointmentRowMapper)
    appointments.map(a => (a.id, a)).toMap
  }

  override def getAppointmentsByExtId(extIds: Set[String]): Map[String, InternalAppointment] = {
    if (extIds.isEmpty) {
      return Map.empty
    }
    val sql = "SELECT * FROM appointment WHERE ext_id IN (:ext_ids)"
    val params = Map("ext_ids" -> setAsJavaSet(extIds))
    val appointments = jdbcTemplate.query(sql, params, appointmentRowMapper)
    appointments.map(a => (a.extId.get, a)).toMap
  }

}


