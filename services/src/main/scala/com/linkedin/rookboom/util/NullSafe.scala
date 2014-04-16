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

package com.linkedin.rookboom.util

/**
 * A null safe guard to invoke a chain of methods when any of them may return null.
 * This guard is to be used for interoperability with Java, for Scala please use Option.
 *
 * @author Dmitriy Yefremov
 */
object NullSafe {

  /**
   * Tries to invoke the given piece of code and return the result of the invocation.
   * If the invocation is successful, an existing option (Some) of the result is returned.
   * If there is a NullPointerException thrown within the given function, a non-existing option (None) is returned.
   * Please note that any NullPointerException is interpreted as None no matter where is it thrown from.
   * @param block the code to invoke
   * @tparam A the result type
   * @return an option of the result of the invocation
   */
  def ?[A](block: => A): Option[A] = {
    try {
      Option(block)
    } catch {
      case e: NullPointerException => None
    }
  }

  implicit def anyToOption[A](a: A): Option[A] = Option(a)

}
