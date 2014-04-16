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

import com.linkedin.rookboom.util.TimeUtils._
import Timeframe._
import scala._


/**
 *
 *
 * @author Sergey Skrobotov, sskrobotov@linkedin.com
 */
class TimeMaskManagerImpl extends TimeMaskManager {

  val defaultInterval: Long = 30 * minute

  val framesByTimeframe = Map(
    Morning -> frames(0 * hour + 30 * minute, 11 * hour, defaultInterval),
    Day -> frames(8 * hour + 30 * minute, 19 * hour, defaultInterval),
    Night -> frames(13 * hour + 30 * minute, 24 * hour, defaultInterval)
  )

  def getMask(day: Long, timeframe: Timeframe): TimeMask = mask(day, framesByTimeframe(timeframe), defaultInterval)

  private def frames(from: Long, to: Long, interval: Long): Seq[Long] = from.to(to, interval)

  private def mask(day: Long, f: Seq[Long], interval: Long): TimeMask = TimeMask(interval, f.map(forward(_, day)))
}