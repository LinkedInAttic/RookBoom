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

import org.springframework.web.servlet.view.json.MappingJackson2JsonView
import javax.servlet.http.{HttpServletResponse, HttpServletRequest}
import com.fasterxml.jackson.databind.util.JSONPObject
import java.util
import scala.beans.BeanProperty
import com.linkedin.rookboom.util.JsonUtils

/**
 * A JSON view for scala JSON serialization that also supports padded JSON.
 * @author Dmitriy Yefremov
 */
class JsonView extends MappingJackson2JsonView {

  import JsonView._

  @BeanProperty
  var withPadding = false

  setObjectMapper(JsonUtils.mapper)

  if (withPadding) {
    setContentType(ContentTypeJavascript)
  } else {
    setContentType(ContentTypeJson)
  }

  override def renderMergedOutputModel(model: util.Map[String, AnyRef],
                                       request: HttpServletRequest,
                                       response: HttpServletResponse) {
    if (withPadding) {
      val callback = getCallbackName(request)
      val value = new JSONPObject(callback, filterModel(model))
      JsonUtils.serialize(value, response.getOutputStream)
    } else {
      super.renderMergedOutputModel(model, request, response)
    }
  }

  private def getCallbackName(request: HttpServletRequest) = {
    request.getParameter(CallbackParam) match {
      case null => DefaultCallback
      case x => x
    }
  }

}

object JsonView {

  /**
   * Content type for simple JSON
   */
  val ContentTypeJson = "application/json"

  /**
   * Content type for padded JSON
   */
  val ContentTypeJavascript = "application/javascript"

  /**
   * The name of the HTTP parameter that sets callback name
   */
  val CallbackParam = "callback"

  /**
   * Default callback function name.
   */
  val DefaultCallback = "callback"

}
