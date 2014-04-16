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
import DayOfWeek._
import WeekOfMonth._
import java.text.{ParseException, SimpleDateFormat}
import com.linkedin.rookboom.util.TimeUtils.utc
import org.testng.Assert
import org.testng.annotations.Test

class RepetitionTest {

  private implicit def toTime(timeStr: String): Long = {

    def parse(pattern: String, time: String) = {
      val dateFormat = new SimpleDateFormat(pattern)
      dateFormat.setTimeZone(utc)
      dateFormat.parse(time).getTime
    }

    try {
      parse("yyyy-MM-dd HH:mm", timeStr)
    } catch {
      case e: ParseException => parse("yyyy-MM-dd", timeStr)
    }
  }

  def assertEquals(expected: Long, actual: Long) {
    Assert.assertEquals(expected, actual)
  }

  def assertEquals(expected: AnyRef, actual: AnyRef) {
    Assert.assertEquals(expected, actual)
  }

  @Test(expectedExceptions = Array(classOf[IllegalArgumentException]))
  def testEndAfterConstructor() {
    After(0)
  }

  @Test
  def testEndAfter() {
    val end = After(2)
    assertFalse(end.isReached(Seq()))
    assertFalse(end.isReached(Seq(1)))
    assertTrue(end.isReached(Seq(1, 2)))
    assertTrue(end.isReached(Seq(1, 2, 3)))
  }

  @Test(expectedExceptions = Array(classOf[IllegalArgumentException]))
  def testEndByConstructor() {
    By(0)
  }

  @Test
  def testEndBy() {
    val end = By(10)
    assertFalse(end.isReached(Seq()))
    assertFalse(end.isReached(Seq(1)))
    assertFalse(end.isReached(Seq(1, 5)))
    assertTrue(end.isReached(Seq(1, 5, 10)))
    assertTrue(end.isReached(Seq(1, 5, 10, 20)))
  }

  @Test(expectedExceptions = Array(classOf[IllegalArgumentException]))
  def testDailyPatternConstructor() {
    Daily(0)
  }

  @Test
  def testDailyPatternFirst() {
    assertEquals("2012-07-19 12:00", Daily(1).first("2012-07-15 12:00", "2012-07-19 11:00", utc))
    assertEquals("2012-07-19 12:00", Daily(1).first("2012-07-19 12:00", "2012-07-19 11:00", utc))
    assertEquals("2012-07-20 12:00", Daily(1).first("2012-07-19 12:00", "2012-07-19 13:00", utc))
    assertEquals("2012-07-20 12:00", Daily(10).first("2012-07-19 12:00", "2012-07-19 13:00", utc))
  }

  @Test
  def testDailyPatternNext() {
    assertEquals("2012-07-20", Daily(1).next("2012-07-19", utc))
    assertEquals("2012-08-01", Daily(1).next("2012-07-31", utc))
    assertEquals("2013-01-02", Daily(2).next("2012-12-31", utc))
    assertEquals("2012-03-10", Daily(10).next("2012-02-29", utc))
  }

  @Test(expectedExceptions = Array(classOf[IllegalArgumentException]))
  def testWeeklyPatternConstructorInterval() {
    Weekly(0, Set(Monday))
  }

  @Test(expectedExceptions = Array(classOf[IllegalArgumentException]))
  def testWeeklyPatternConstructorDays() {
    Weekly(1, Set())
  }

  @Test
  def testWeeklyPatternFirst() {
    assertEquals("2012-07-20 12:00", Weekly(1, Set(Friday)).first("2012-07-20 12:00", "2012-07-20 11:00", utc))
    assertEquals("2012-07-27 12:00", Weekly(1, Set(Friday)).first("2012-07-20 12:00", "2012-07-20 13:00", utc))
    assertEquals("2012-07-16 12:00", Weekly(1, Set(Monday, Friday)).first("2012-07-15 12:00", "2012-07-15 13:00", utc))
    assertEquals("2012-07-23", Weekly(10, Set(Monday)).first("2012-07-19", "2012-07-19", utc))
  }

  @Test
  def testWeeklyPatternNext() {
    // same day
    assertEquals("2012-07-27 11:00", Weekly(1, Set(Friday)).next("2012-07-20 11:00", utc))
    // same week
    assertEquals("2012-07-20 11:00", Weekly(1, Set(Friday)).next("2012-07-15 11:00", utc))
    assertEquals("2012-07-16 11:00", Weekly(1, Set(Monday, Friday)).next("2012-07-15 11:00", utc))
    // next week
    assertEquals("2012-07-23 11:00", Weekly(1, Set(Monday)).next("2012-07-19 11:00", utc))
    assertEquals("2012-07-26 11:00", Weekly(1, Set(Thursday)).next("2012-07-19 11:00", utc))
    // interval
    assertEquals("2012-07-23 11:00", Weekly(1, Set(Monday)).next("2012-07-19 11:00", utc))
  }

  @Test(expectedExceptions = Array(classOf[IllegalArgumentException]))
  def testMonthlyPatternConstructorInterval() {
    Monthly(0, Monday, First)
  }

  @Test
  def testMonthlyPatternFirst() {
    assertEquals("2012-07-02 12:00", Monthly(1, Monday, First).first("2012-07-01 12:00", "2012-07-01 00:00", utc))
    assertEquals("2012-07-02 12:00", Monthly(1, Monday, First).first("2012-07-02 12:00", "2012-07-02 12:00", utc))
    assertEquals("2012-08-06 12:00", Monthly(1, Monday, First).first("2012-07-02 12:00", "2012-07-02 13:00", utc))
    assertEquals("2012-07-27 12:00", Monthly(1, Friday, Last).first("2012-07-01 12:00", "2012-07-01 00:00", utc))
    assertEquals("2012-07-27 12:00", Monthly(10, Friday, Last).first("2012-07-01 12:00", "2012-07-01 00:00", utc))
  }

  @Test
  def testMonthlyPatternNext() {
    assertEquals("2012-08-06 12:00", Monthly(1, Monday, First).next("2012-07-02 12:00", utc))
    assertEquals("2012-09-03 12:00", Monthly(2, Monday, First).next("2012-07-02 12:00", utc))
  }

  @Test
  def testRepetition() {
    val r = Repetition(TimeSlot("2012-07-01 12:00", "2012-07-01 13:00"), Monthly(2, Friday, Last), After(4))
    val expected = Seq(
      TimeSlot("2012-07-27 12:00", "2012-07-27 13:00"),
      TimeSlot("2012-09-28 12:00", "2012-09-28 13:00"),
      TimeSlot("2012-11-30 12:00", "2012-11-30 13:00"),
      TimeSlot("2013-01-25 12:00", "2013-01-25 13:00"))
    assertEquals(expected, r.toOccurrences("2012-07-01 12:00", utc))
  }

  @Test()
  def testWeeklyPrettyPrint() {
    assertEquals("on Monday every week", Weekly(1, Set(Monday)).toPrettyString)
    assertEquals("on Monday, Tuesday every other week", Weekly(2, Set(Monday, Tuesday)).toPrettyString)
    assertEquals("on Monday, Tuesday, Wednesday every 3 weeks", Weekly(3, Set(Monday, Tuesday, Wednesday)).toPrettyString)
    assertEquals("on weekdays every week", Weekly(1, Set(Monday, Tuesday, Wednesday, Thursday, Friday)).toPrettyString)
    assertEquals("on weekend days every week", Weekly(1, Set(Saturday, Sunday)).toPrettyString)
  }

  @Test()
  def testDailyPrettyPrint() {
    assertEquals("every day", Daily(1).toPrettyString)
    assertEquals("every other day", Daily(2).toPrettyString)
    assertEquals("every 3 days", Daily(3).toPrettyString)
  }

  @Test()
  def testMonthlyPrettyPrint() {
    assertEquals("on the first Monday of every month", Monthly(1, Monday, First).toPrettyString)
    assertEquals("on the second Tuesday of every other month", Monthly(2, Tuesday, Second).toPrettyString)
    assertEquals("on the last Wednesday of every 3 months", Monthly(3, Wednesday, Last).toPrettyString)
  }

  @Test()
  def testAfterPrettyPrint() {
    assertEquals("after 1 occurrence", After(1).toPrettyString(utc))
    assertEquals("after 2 occurrences", After(2).toPrettyString(utc))
  }

  @Test()
  def testByPrettyPrint() {
    assertEquals("by 07/27/2012", By("2012-07-27 12:00").toPrettyString(utc))
  }

  @Test()
  def testRepetitionPrettyPrint() {
    val r = Repetition(TimeSlot("2012-07-01 12:00", "2012-07-01 13:00"), Weekly(2, Set(Friday)), After(4))
    val expected = "on Friday every other week from 12:00 PM to 1:00 PM, effective 07/06/2012, stop after 4 occurrences"
    assertEquals(expected, r.toPrettyString("2012-07-01 12:00", utc))
  }

}