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
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.{RequestParam, RequestMapping}
import collection.JavaConversions._
import com.linkedin.rookboom.user.UserManager
import com.linkedin.rookboom.util.Logging

@Controller
@RequestMapping(Array("users"))
class UsersController extends ExceptionResolver {

  @Autowired val userManager: UserManager = null

  @RequestMapping
  def handle() = userManager.getUsers

  @RequestMapping(Array("/search"))
  def search(@RequestParam(value = "query") query: String,
             @RequestParam(value = "search", defaultValue = "all") search: String) = {

    case class SearchItem(displayName: String, address: String)

    val usersAndGroups = userManager.getUsers.map(u => SearchItem(u.displayName, u.address)).toList :::
      userManager.getGroups.map(g => SearchItem(g.displayName, g.address)).toList

    val result = usersAndGroups.filter(searchItem => {
      val queryLc = query.toLowerCase
      search match {
        case "email" => searchItem.address.toLowerCase.contains(queryLc)
        case "name" => searchItem.displayName.toLowerCase.contains(queryLc)
        case _ => searchItem.displayName.toLowerCase.contains(queryLc) ||
          searchItem.address.toLowerCase.contains(queryLc)
      }
    })

    mapAsJavaMap(Map("users" -> result.asInstanceOf[AnyRef]))
  }
}
