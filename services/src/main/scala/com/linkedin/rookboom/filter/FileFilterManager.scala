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

package com.linkedin.rookboom.filter

import com.linkedin.rookboom.util.{JsonUtils, Logging, Reloadable}
import java.io.File
import compat.Platform

/**
 * File based implementation of the FilterManger interface.
 * @author Dmitriy Yefremov
 */
class FileFilterManager(val file: File) extends FilterManager with Logging with Reloadable {

  require(file.isFile, "File '" + file.getAbsolutePath + "' doesn't exist")

  @volatile
  private var filters: Seq[Filter] = Seq.empty

  @volatile
  private var lastReloadTime: Long = 0

  override def getFilters = filters

  override def reload() {
    if (file.lastModified() > lastReloadTime) {
      log.info("Reloading filters...")
      filters = JsonUtils.deserialize[Seq[Filter]](file)
      lastReloadTime = Platform.currentTime
    }
  }
}
