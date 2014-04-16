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

import org.springframework.web.bind.annotation.ExceptionHandler
import collection.JavaConversions._
import javax.servlet.http.HttpServletResponse
import java.util.{Map => JavaMap}
import com.linkedin.rookboom.util.{UserVisibleException, Logging}
import scala.util.control.NonFatal

/**
 * A convenient base class for controller classes to handle exceptions.
 * @author Dmitriy Yefremov
 */
trait ExceptionResolver extends Logging {

  @ExceptionHandler
  def handleException(exception: Exception, response: HttpServletResponse): JavaMap[_, _] = {
    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR)
    val model = exception match {
      case e: UserVisibleException => Map("error" -> e.getMessage)
      case NonFatal(e) => Map.empty
    }
    log.error("Exception in the controller method", exception)
    mapAsJavaMap(Map("success" -> false) ++ model)
  }
}
