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

/**
 * Dust templating engine interface.
 * @author Dmitriy Yefremov
 */
trait DustEngine {

  def render(template: String, json: String, writer: Writer)

  def render(template: String, model: Map[String, AnyRef], writer: Writer)

}
