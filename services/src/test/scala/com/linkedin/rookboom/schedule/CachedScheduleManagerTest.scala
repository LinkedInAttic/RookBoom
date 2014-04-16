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

import org.easymock.EasyMock._
import org.testng.AssertJUnit._
import net.sf.ehcache.CacheManager
import com.linkedin.rookboom.util.TimeUtils._
import java.text.SimpleDateFormat
import org.testng.annotations.{AfterMethod, Test, BeforeMethod}

class CachedScheduleManagerTest {

  val resource = "cr-jenga@linkedin.com"

  var cacheManager: CacheManager = _

  var scheduleMangerMock: ScheduleManager = _
  var cachedScheduleManager: CachedScheduleManager = _

  @BeforeMethod
  def init() {

    scheduleMangerMock = createStrictMock(classOf[ScheduleManager])

    cacheManager = CacheManager.newInstance()
    cacheManager.addCache("schedule")

    cacheManager.clearAll()
    cachedScheduleManager = new CachedScheduleManager(scheduleMangerMock, cacheManager)
  }

  @AfterMethod
  def tearDown() {
    cacheManager.shutdown()
  }

  @Test
  def testProxy() {
    val from = "2012-08-23 09:00"
    val to = "2012-08-23 18:00"
    val events = Map(resource -> Seq(Event(1, TimeSlot("2012-08-23 10:00", "2012-08-23 11:00"))))

    expect(scheduleMangerMock.getSchedule(resource, TimeSlot(roundToDayDown(from), roundToDayUp(to)))).andReturn(events)

    replay(scheduleMangerMock)

    val schedule = cachedScheduleManager.getSchedule(resource, TimeSlot(from, to))

    assertEquals(events, schedule)

    verify(scheduleMangerMock)
  }

  @Test
  def testCache() {
    val from = "2012-08-23 09:00"
    val to = "2012-08-23 18:00"
    val events = Map(resource -> Seq(Event(1, TimeSlot("2012-08-23 10:00", "2012-08-23 11:00"))))

    expect(scheduleMangerMock.getSchedule(resource, TimeSlot(roundToDayDown(from), roundToDayUp(to)))).andReturn(events)

    replay(scheduleMangerMock)

    val schedule1 = cachedScheduleManager.getSchedule(resource, TimeSlot(from, to))
    val schedule2 = cachedScheduleManager.getSchedule(resource, TimeSlot(from, to))

    assertEquals(schedule1, schedule2)
    assertEquals(events, schedule1)

    verify(scheduleMangerMock)
  }


  @Test
  def testDayEdge() {
    val from = "2012-08-22 23:00"
    val to = "2012-08-23 01:00"
    val edge = "2012-08-23 00:00"
    val events = Seq(Event(1, TimeSlot(from, edge)), Event(2, TimeSlot(edge, to)))

    expect(scheduleMangerMock.getSchedule(resource, TimeSlot(roundToDayDown(from), roundToDayUp(to)))).andReturn(Map(resource -> events))

    replay(scheduleMangerMock)

    val schedule = cachedScheduleManager.getSchedule(resource, TimeSlot(from, to))

    assertEquals(Map(resource -> events), schedule)

    verify(scheduleMangerMock)
  }

  @Test
  def testReloadMultiple() {
    val from = "2012-08-23 09:00"
    val to = "2012-08-23 18:00"

    val resource1 = "1@linkedin.com"
    val resource2 = "2@linkedin.com"
    val resource3 = "3@linkedin.com"

    val event1 = Event(1, TimeSlot(from, from + hour))
    val event2 = Event(2, TimeSlot(from + hour, from + 2 * hour))
    val event3 = Event(3, TimeSlot(from + 2 * hour, from + 3 * hour))

    expect(
      scheduleMangerMock.getSchedule(resource1, TimeSlot(roundToDayDown(from), roundToDayUp(to)))
    ).andReturn(
      Map(resource1 -> Seq(event1))
    )
    expect(
      scheduleMangerMock.getSchedule(Set(resource2, resource3), TimeSlot(roundToDayDown(from), roundToDayUp(to)))
    ).andReturn(
      Map(resource2 -> Seq(event2), resource3 -> Seq(event3))
    )

    replay(scheduleMangerMock)

    val schedule1 = cachedScheduleManager.getSchedule(resource1, TimeSlot(from, to))
    assertEquals(Map(resource1 -> Seq(event1)), schedule1)

    val schedule2 = cachedScheduleManager.getSchedule(Set(resource1, resource2, resource3), TimeSlot(from, to))
    assertEquals(Map(resource1 -> Seq(event1), resource2 -> Seq(event2), resource3 -> Seq(event3)), schedule2)

    verify(scheduleMangerMock)
  }

  private implicit def toUtcTime(timeStr: String): Long = {
    val dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm")
    dateFormat.setTimeZone(utc)
    dateFormat.parse(timeStr).getTime
  }

  private implicit def toSet(element: String): Set[String] = Set(element)

}
