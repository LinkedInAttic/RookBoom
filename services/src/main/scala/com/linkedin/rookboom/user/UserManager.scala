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


/**
 * This service provides information about users and groups.
 *
 * @author Dmitriy Yefremov
 */
trait UserManager {

  def getUsers: Seq[User]

  def getUserByAddress(address: String): Option[User]

  def getUserByAccount(account: String): Option[User]

  def getUserByName(name: String): Option[User]

  def getGroups: Seq[Group]

  def getGroupByAddress(address: String): Option[Group]
}

/**
 * Represents information about a single user.
 * @param displayName name to display
 * @param address e-mail address
 * @param account account name
 * @param photo URL of user's photo
 * @param location user's location code
 * @param url user's profile URL
 */
case class User(displayName: String, address: String, account: String, photo: Option[String] = None, location: Option[String] = None, url: Option[String] = None)

/**
 * Represents a mail list.
 * @param displayName name to display
 * @param address e-mail address
 */
case class Group(displayName: String, address: String)
