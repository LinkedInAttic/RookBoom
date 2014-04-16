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


import org.testng.AssertJUnit._
import com.linkedin.rookboom.util.NullSafe.anyToOption
import com.linkedin.rookboom.schedule.dao.{InternalAppointment, InternalEvent, EwsEvent}
import EwsScheduleManager._
import org.testng.annotations.Test

class EwsScheduleManagerTest {

  @Test
  def testIsSame() {
    val int = InternalEvent(0, "mailbox", TimeSlot(1, 2), "id")
    val ext = EwsEvent("id", "mailbox", TimeSlot(1, 2))
    assertTrue(isSame(int, ext))
  }

  @Test
  def testIsSameUnresolved() {
    val int = InternalEvent(0, "mailbox", TimeSlot(1, 2))
    val ext = EwsEvent(None, "mailbox", TimeSlot(1, 2))
    assertTrue(isSame(int, ext))
  }

  @Test
  def testIsSameUnresolvedInternal() {
    val int = InternalEvent(0, "mailbox", TimeSlot(1, 2))
    val ext = EwsEvent("id", "mailbox", TimeSlot(1, 2))
    assertFalse(isSame(int, ext))
  }

  @Test
  def testIsSameUnresolvedExternal() {
    val int = InternalEvent(0, "mailbox", TimeSlot(1, 2), "id")
    val ext = EwsEvent(None, "mailbox", TimeSlot(1, 2))
    assertTrue(isSame(int, ext))
  }

  @Test
  def testIsSameDifferentTime() {
    val int = InternalEvent(0, "mailbox", TimeSlot(1, 3), "id")
    val ext = EwsEvent("id", "mailbox", TimeSlot(1, 2))
    assertFalse(isSame(int, ext))
  }

  @Test
  def testIsSameDifferentMailbox() {
    val int = InternalEvent(0, "another-mailbox", TimeSlot(1, 2), "id")
    val ext = EwsEvent("id", "mailbox", TimeSlot(1, 2))
    assertFalse(isSame(int, ext))
  }

  @Test
  def testFindCreated() {
    val stored = Seq(
      InternalEvent(0, "mailbox1", TimeSlot(0, 1), "id1"),
      InternalEvent(0, "mailbox2", TimeSlot(0, 2), "id2")
    )

    val current = Seq(
      EwsEvent("id2", "mailbox2", TimeSlot(0, 2)),
      EwsEvent("id3", "mailbox3", TimeSlot(0, 3))
    )

    val created = findCreated(stored, current)
    assertEquals(1, created.size)
    assertEquals(current(1), created(0))
  }

  @Test
  def testFindDeleted() {
    val stored = Seq(
      InternalEvent(0, "mailbox1", TimeSlot(0, 1), "id1"),
      InternalEvent(0, "mailbox2", TimeSlot(0, 2), "id2")
    )

    val current = Seq(
      EwsEvent("id2", "mailbox2", TimeSlot(0, 2)),
      EwsEvent("id3", "mailbox3", TimeSlot(0, 3))
    )

    val deleted = findDeleted(stored, current)
    assertEquals(1, deleted.size)
    assertEquals(stored(0), deleted(0))
  }

  @Test
  def testFindDeletedWithAppointment() {
    val stored = Seq(
      InternalEvent(1, "mailbox1", TimeSlot(0, 1), "id1"),
      InternalEvent(2, "mailbox1", TimeSlot(0, 2), "id2", InternalAppointment(1, "extId1", "organizer"))
    )

    val current = Seq(
      EwsEvent("id1", "mailbox1", TimeSlot(0, 1)),
      EwsEvent("id2", "mailbox1", TimeSlot(0, 2))
    )

    val deleted = findDeleted(stored, current)
    assertTrue(deleted.isEmpty)
  }

}
