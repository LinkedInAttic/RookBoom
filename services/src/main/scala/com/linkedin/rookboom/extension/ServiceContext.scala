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

package com.linkedin.rookboom.extension

/**
 * Service context abstraction. Manages dependencies, configuration properties etc.
 * @author Dmitriy Yefremov
 */
trait ServiceContext {

  /**
   * Returns an instance of the given type if it exists in the service context. If there is no instance of the
   * required type an IllegalArgumentException is thrown.
   */
  def getDependency[T](clazz: Class[T]): T

  /**
   * Returns the value for the given property key. An IllegalArgumentException is thrown if the property doesn't exist.
   */
  def getProperty(key: String): String

  /**
   * Schedules periodic execution of the given block of code.
   * @param cron cron style expression to trigger execution
   * @param task block of code to be executed
   * @param init if true the task is executed immediately
   */
  def schedule(cron: String, task: => Unit, init: Boolean = false)

}
