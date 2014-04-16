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


import org.mozilla.javascript.{Scriptable, Context}


/**
 * @author Sergey Skrobotov, sskrobotov@linkedin.com
 */
object DustUtils {

  def executeInContext[T](action: Context => T): T = {
    val dustContext = Context.enter()
    try {
      action(dustContext)
    } finally {
      Context.exit()
    }
  }

  def evaluate(globalScope: Scriptable, function: String, args: (String, AnyRef)*) = {
    executeInContext(context => {
      val compileScope = context.newObject(globalScope)
      compileScope.setParentScope(globalScope)

      val sb = new StringBuilder("(").append(function).append("(")

      args.foreach(arg => {
        compileScope.put(arg._1, compileScope, arg._2)
        sb.append(arg._1).append(",")
      })

      sb.replace(sb.length - 1, sb.length, "))")

      context.evaluateString(compileScope, sb.toString(), function, 0, null).toString
    })
  }
}