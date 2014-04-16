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

package com.linkedin.rookboom.web.dust.loader

import java.io.{FileReader, FileFilter, File}
import org.springframework.util.{FileCopyUtils, Assert}
import com.linkedin.rookboom.util.Logging

/**
 * @author Sergey Skrobotov, sskrobotov@linkedin.com
 */
class FileSystemTemplateLoader(val paths: Array[File], val suffix: String) extends TemplateLoader with Logging {

  override def loadTemplates() = loadTemplates(0)

  override def loadTemplates(since: Long) = {
    paths.flatMap(path => {
      Assert.isTrue(path.isDirectory, "Directory '" + path.getAbsolutePath + "' doesn't exist")
      val files = path.listFiles(new FileFilter {
        def accept(file: File) = file.isFile && file.lastModified() > since && file.getName.endsWith(suffix)
      })
      files.map(file => (file.getName.dropRight(suffix.length), readFile(file)))
    }).toMap
  }

  private def readFile(file: File): String = FileCopyUtils.copyToString(new FileReader(file))

}