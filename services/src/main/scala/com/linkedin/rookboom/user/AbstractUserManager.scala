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

package com.linkedin.rookboom.user

import com.linkedin.rookboom.util.{Reloadable, Logging}

/**
 * Abstract class containing shared code for typical UserManager implementations.
 * @author Dmitriy Yefremov
 */
abstract class AbstractUserManager extends UserManager with Logging with Reloadable {

  @volatile
  private var userByAddress: Map[String, User] = Map.empty

  @volatile
  private var userByAccount: Map[String, User] = Map.empty

  @volatile
  private var userByName: Map[String, User] = Map.empty

  @volatile
  private var groupByAddress: Map[String, Group] = Map.empty

  override def getUsers = userByAddress.values.toSeq

  override def getUserByAddress(address: String) = userByAddress.get(address)

  override def getUserByAccount(account: String) = userByAccount.get(account)

  def getUserByName(name: String) = userByName.get(name)

  override def getGroups = groupByAddress.values.toSeq

  override def getGroupByAddress(address: String) = groupByAddress.get(address)

  override def reload() {
    log.info("Fetching users...")
    val users = loadUsers
    log.info("There are {} users fetched", users.size)
    userByAddress = users.map(u => u.address -> u).toMap
    userByAccount = users.map(u => u.account -> u).toMap
    userByName = users.map(u => u.displayName -> u).toMap
    log.info("Fetching groups...")
    val groups = loadGroups
    log.info("There are {} groups fetched", groups.size)
    groupByAddress = groups.map(g => g.address -> g).toMap
  }

  protected def loadUsers: Seq[User]

  protected def loadGroups: Seq[Group]
}
