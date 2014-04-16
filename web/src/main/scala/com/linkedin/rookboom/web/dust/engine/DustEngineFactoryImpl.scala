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


import java.io.InputStreamReader
import collection.mutable.Set
import com.linkedin.rookboom.web.dust.loader.TemplateLoader
import compat.Platform
import com.linkedin.rookboom.util.Logging


/**
 * @author Sergey Skrobotov, sskrobotov@linkedin.com
 */
class DustEngineFactoryImpl(val resources: Array[String],
                            val templateLoader: TemplateLoader,
                            val cacheTemplates: Boolean) extends DustEngineFactory with Logging {

  private val ALL_OPTIMIZATIONS = 9

  @volatile
  private var lastUpdated = 0L

  private val templates = Set[String]()

  private val sharedScope = DustUtils.executeInContext(context => {
    context.setOptimizationLevel(ALL_OPTIMIZATIONS)

    val scope = context.initStandardObjects
    val classLoader = this.getClass.getClassLoader

    resources.foreach(filename => {
      val resource = classLoader.getResource(filename)
      context.evaluateReader(scope, new InputStreamReader(resource.openStream, "UTF-8"), filename, 0, null)
    })

    scope.put("log", scope, log)
    scope
  })

  private val threadLocalDustEngine = new ThreadLocal[DustEngineImpl] {
    override def initialValue = new DustEngineImpl(sharedScope)
  }

  override def getEngine = {
    if (!cacheTemplates) {
      reloadTemplates()
    }
    threadLocalDustEngine.get
  }

  override def hasTemplate(name: String) = {
    if (!cacheTemplates) {
      reloadTemplates()
    }
    templates.contains(name)
  }

  def reloadTemplates() {
    log.info("Reloading templates modified since {}", lastUpdated)
    val templatesData = templateLoader.loadTemplates(lastUpdated)
    templatesData.foreach(t => {
      DustUtils.evaluate(sharedScope, "DustTools.compileAndLoad", ("rawSource", t._2), ("name", t._1))
      templates.add(t._1)
    })
    log.info("There are {} templates loaded", templatesData.size)
    lastUpdated = Platform.currentTime
  }

}