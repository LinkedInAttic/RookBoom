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

package com.linkedin.rookboom.web.dust

import com.linkedin.rookboom.web.dust.engine.DustEngineFactory
import org.springframework.web.servlet.view.{AbstractView, AbstractCachingViewResolver}
import javax.servlet.http.{HttpServletResponse, HttpServletRequest}
import scala.collection.JavaConversions._
import org.springframework.validation.BindingResult
import com.linkedin.rookboom.util.Logging

/**
 * @author Sergey Skrobotov, sskrobotov@linkedin.com
 */
class DustTemplateViewResolver(val dustEngineFactory: DustEngineFactory,
                               val cacheViews: Boolean) extends AbstractCachingViewResolver with Logging {

  override def isCache = cacheViews

  override def loadView(viewName: String, locale: java.util.Locale) = {
    dustEngineFactory.hasTemplate(viewName) match {
      case false => null
      case true => new DustTemplateView(viewName)
    }
  }

  private class DustTemplateView(viewName: String) extends AbstractView {

    def renderMergedOutputModel(model: java.util.Map[String, AnyRef],
                                request: HttpServletRequest,
                                response: HttpServletResponse) {
      val filtered = model.toMap.filterNot(_._2.isInstanceOf[BindingResult])
      dustEngineFactory.getEngine.render(viewName, filtered, response.getWriter)
    }

  }

}