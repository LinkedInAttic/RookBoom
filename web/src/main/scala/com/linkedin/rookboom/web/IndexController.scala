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

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.servlet.ModelAndView
import java.security.Principal
import collection.JavaConversions._
import org.springframework.beans.factory.annotation.Autowired
import com.linkedin.rookboom.user.{UserManager, User}
import com.linkedin.rookboom.layout.LayoutManager
import com.linkedin.rookboom.filter.FilterManager
import java.util.TimeZone
import scala.collection.mutable

/**
 * @author Sergey Skrobotov, s.skrobotov@gmail.com
 */
@Controller
@RequestMapping(Array(""))
class IndexController extends ExceptionResolver {

  @Autowired val userManager: UserManager = null
  @Autowired val layoutManager: LayoutManager = null
  @Autowired val filterManager: FilterManager = null
  @Autowired val data: mutable.Map[String, AnyRef] = null

  case class UserRecord(user: Option[User]) {
    val loggedIn = user.isDefined
  }

  @RequestMapping
  def handle(principal: Principal) = {
    new ModelAndView(
      "index",
      mapAsJavaMap(Map(
        "principal" -> user(principal),
        "filters" -> filterManager.getFilters,
        "locations" -> layoutManager.getAll.map(l => LocationRecord(l.id, l.name, timezoneOffset(l.timezone))),
        "data" -> data
      ))
    )
  }

  @RequestMapping(Array("ie"))
  def ie() = new ModelAndView("ie")

  def user(principal: Principal) = principal match {
    case null => UserRecord(None)
    case p => UserRecord(userManager.getUserByAccount(p.getName))
  }

  private def timezoneOffset(timezone: TimeZone) = timezone.getOffset(System.currentTimeMillis) / (60 * 1000)
}

case class LocationRecord(id: String, name: String, timezoneOffset: Int)