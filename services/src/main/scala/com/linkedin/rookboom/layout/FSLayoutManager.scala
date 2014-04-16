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

import com.linkedin.rookboom.util.{JsonUtils, Reloadable, Logging}
import java.io.File
import java.io.FilenameFilter


/**
 * File based implementation.
 * @author Sergey Skrobotov, sskrobotov@linkedin.com
 */
class FSLayoutManager(val path: File) extends AbstractLayoutManager with Logging with Reloadable {

  require(path.isDirectory, "Directory '" + path.getAbsolutePath + "' doesn't exist")

  private def loadFromFile(file: File) = {
    val layout = JsonUtils.deserialize[Layout](file)
    // filter out hidden rooms
    val rooms = layout.rooms.filterNot(_.attributes.get("hidden").exists(_.toBoolean))
    Layout(layout.id, layout.name, layout.timezone, rooms)
  }

  override protected def loadLayouts() = {
    val files = path.listFiles(new FilenameFilter() {
      def accept(dir: File, name: String) = name.toLowerCase.endsWith(".json")
    })
    val layouts = files.map(loadFromFile)
    layouts.map(x => (x.id, x)).toMap
  }

}