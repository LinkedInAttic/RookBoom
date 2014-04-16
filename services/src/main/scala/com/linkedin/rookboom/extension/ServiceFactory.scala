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

package com.linkedin.rookboom.extension

import com.linkedin.rookboom.filter.FilterManager
import com.linkedin.rookboom.layout.LayoutManager
import com.linkedin.rookboom.schedule.{TimeMaskManager, BookingService, ScheduleManager}
import com.linkedin.rookboom.user.UserManager

/**
 * An extension point providing implementations of the core services.
 * @author Dmitriy Yefremov
 */
trait ServiceFactory {

  def filterManager: FilterManager

  def timeMaskManager: TimeMaskManager

  def layoutManager: LayoutManager

  def userManager: UserManager

  def scheduleManager: ScheduleManager

  def bookingService: BookingService

}
