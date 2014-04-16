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

package com.linkedin.rookboom.util

import java.util.{Date, TimeZone, Calendar}


/**
 * @author Sergey Skrobotov, sskrobotov@linkedin.com
 */
object TimeUtils {

  val utc = TimeZone.getTimeZone("UTC")

  val second: Long = 1000
  val minute: Long = 60 * second
  val hour: Long = 60 * minute
  val day: Long = 24 * hour
  val week: Long = 7 * day

  def utcCalendar = {
    val calendar = Calendar.getInstance
    calendar.setTimeZone(utc)
    calendar
  }

  def roundToDayDown(time: Long) = {
    val c = utcCalendar
    c.setTimeInMillis(time)
    c.set(Calendar.MILLISECOND, 0)
    c.set(Calendar.SECOND, 0)
    c.set(Calendar.MINUTE, 0)
    c.set(Calendar.HOUR_OF_DAY, 0)
    c.getTimeInMillis
  }

  def roundToDayUp(time: Long) = forward(roundToDayDown(time), day)

  def forward(time: Long, adjustment: Long) = time + adjustment

  def backward(time: Long, adjustment: Long) = forward(time, -adjustment)

  implicit def longToDate(l: Long): Date = new Date(l)

  implicit def dateToLong(d: Date): Long = d.getTime
}