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

import org.springframework.context.{ApplicationContextAware, ApplicationContext}
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.scheduling.support.CronTrigger
import scala.util.control.NonFatal

/**
 * Spring based ServiceContext implementation.
 * @author Dmitriy Yefremov
 */
class SpringContext extends ServiceContext with ApplicationContextAware {

  private var applicationContext: ApplicationContext = _

  private var beanFactory: ConfigurableBeanFactory = _

  private val scheduler = new ThreadPoolTaskScheduler()
  scheduler.setPoolSize(2)
  scheduler.initialize()

  def setApplicationContext(context: ApplicationContext) {
    applicationContext = context
    beanFactory = context.getAutowireCapableBeanFactory match {
      case factory: ConfigurableBeanFactory => factory
    }
  }

  def getDependency[T](clazz: Class[T]): T = {
    try {
      applicationContext.getBean(clazz)
    } catch {
      case NonFatal(e) => throw new IllegalArgumentException(s"Can't find a dependency of type '$clazz'", e)
    }
  }

  def getProperty(key: String): String = {
    try {
      beanFactory.resolveEmbeddedValue(s"$${$key}")
    } catch {
      case NonFatal(e) => throw new IllegalArgumentException(s"Can't resolve '$key'", e)
    }
  }

  def schedule(cron: String, task: => Unit, init: Boolean = false) {
    if (init) {
      task
    }
    val runnable = new Runnable {
      def run() {
        task
      }
    }
    val trigger = new CronTrigger(cron)
    scheduler.schedule(runnable, trigger)
  }

}
