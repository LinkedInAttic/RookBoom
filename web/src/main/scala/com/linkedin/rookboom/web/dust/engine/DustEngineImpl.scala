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

package com.linkedin.rookboom.web.dust.engine


import java.io.Writer
import org.mozilla.javascript.Scriptable
import com.linkedin.rookboom.util.JsonUtils


/**
 * @author Sergey Skrobotov, sskrobotov@linkedin.com
 */
class DustEngineImpl(val sharedScope: Scriptable) extends DustEngine {

  private val scope = DustUtils.executeInContext(context => {
    val s = context.newObject(sharedScope)
    s.setPrototype(sharedScope)
    s.setParentScope(null)
    s
  })

  override def render(template: String, json: String, writer: Writer) {
    DustUtils.evaluate(scope,
      "DustTools.render",
      ("name", template),
      ("json", json),
      ("writer", writer)
    )
  }

  override def render(template: String, model: Map[String, AnyRef], writer: Writer) {
    render(template, JsonUtils.serialize(model), writer)
  }
}