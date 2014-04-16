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

import com.linkedin.rookboom.schedule._
import scala.Some
import scala.Some
import scala.Some
import scala.Some
import scala.Some
import scala.Some
import com.linkedin.rookboom.schedule.Daily
import com.linkedin.rookboom.schedule.After
import com.linkedin.rookboom.schedule.TimeSlot
import com.linkedin.rookboom.schedule.Monthly
import scala.Some
import com.linkedin.rookboom.schedule.Weekly
import com.linkedin.rookboom.schedule.Repetition

/**
 * A set of methods to work with repetition HTTP parameters mapping.
 * @author Dmitriy Yefremov
 */
object RepetitionHelper {

  def getRepetition(timeSlot: TimeSlot,
                    patternName: String,
                    interval: Int,
                    days: Array[String],
                    repWeek: String,
                    after: Int,
                    by: Long): Option[Repetition] = {
    getRepetitionPattern(patternName, interval, days, repWeek).flatMap(pattern =>
      getRepetitionEnd(after, by).flatMap(end =>
        Some(Repetition(timeSlot, pattern, end))
      )
    )
  }

  private def getRepetitionPattern(patternName: String, interval: Int, days: Array[String], repWeek: String): Option[RepetitionPattern] = {
    patternName.toLowerCase match {
      case "daily" => Some(Daily(interval))
      case "weekly" => Some(Weekly(interval, days.map(DayOfWeek.withName(_)).toSet))
      case "monthly" => Some(Monthly(interval, DayOfWeek.withName(days(0)), WeekOfMonth.withName(repWeek)))
      case _ => None
    }
  }

  private def getRepetitionEnd(after: Int, by: Long): Option[RepetitionEnd] = {
    (after, by) match {
      case (x, 0) if x > 0 => Some(After(x))
      case (0, x) if x > 0 => Some(By(x))
      case _ => None
    }
  }

}
