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

import com.linkedin.rookboom.util.{Logging, Reloadable}

/**
 * Abstract class containing shared code for typical LayoutManager implementations.
 * @author Dmitriy Yefremov
 */
abstract class AbstractLayoutManager extends LayoutManager with Logging with Reloadable {

  @volatile
  private var layoutMap: Map[String, Layout] = Map.empty

  override def getAvailableLayouts = layoutMap.keys.toSeq

  override def getLayout(id: String): Option[Layout] = {
    // try the requested layout first
    val l = layoutMap.get(id)
    if (l.nonEmpty) {
      return l
    }
    // if failed -- try the default layout
    val d = defaultLayoutId.flatMap(layoutId => layoutMap.get(layoutId))
    if (d.nonEmpty) {
      return d
    }
    // finally try the first layout
    layoutMap.values.headOption
  }

  override def getAll = layoutMap.values.toSeq

  override def defaultLayoutId: Option[String] = None

  override def reload() {
    log.info("Reloading layouts...")
    layoutMap = loadLayouts()
    log.info("There are {} layouts loaded", layoutMap.size)
  }

  protected def loadLayouts(): Map[String, Layout]

}
