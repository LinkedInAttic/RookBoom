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

package com.linkedin.rookboom.layout

import java.util.TimeZone

/**
 * Defines the office layout: buildings, floors, rooms.
 *
 * @author Dmitriy Yefremov
 */
trait LayoutManager {

  /**
   * Returns ids of all available layouts.
   * @return layouts' ids
   */
  def getAvailableLayouts: Seq[String]

  /**
   * Returns a layout with the given id.
   * @param id layout's id
   * @return layout option
   */
  def getLayout(id: String): Option[Layout]

  /**
   * Returns all available layouts,
   * @return all layouts
   */
  def getAll: Seq[Layout]

  /**
   * Returns id of the default layout if the layout manager has one.
   * @return option with the id of the default layout.
   */
  def defaultLayoutId: Option[String]
}

/**
 * Immutable classes that represent conference rooms layout.
 *
 * @author Sergey Skrobotov, sskrobotov@linkedin.com
 */
case class Layout(id: String, name: String, timezone: TimeZone, rooms: Seq[Room])

case class Room(email: String, name: String, layout: String, attributes: Map[String, String] = Map.empty)