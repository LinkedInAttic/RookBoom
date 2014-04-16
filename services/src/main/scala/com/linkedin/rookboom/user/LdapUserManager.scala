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

import javax.naming.directory.{SearchControls, Attributes}
import org.springframework.ldap.core.LdapTemplate
import scala.collection.JavaConverters._
import com.linkedin.rookboom.util.LdapAttributesMapper

/**
 * A user manager implementation that gets information from a LDAP server.
 *
 * @author Dmitriy Yefremov
 */
class LdapUserManager(val ldapTemplate: LdapTemplate,
                      val usersBase: String,
                      val usersFilter: String,
                      val groupsBase: String,
                      val groupsFilter: String) extends AbstractUserManager {

  val userMapper = new LdapUserMapper()

  val groupMapper = new LdapGroupMapper()

  override protected def loadUsers: Seq[User] = {
    val users = ldapTemplate.search(usersBase, usersFilter, SearchControls.SUBTREE_SCOPE, userMapper.attributes.toArray, userMapper)
    users.asScala.toSeq.collect {
      case Some(user: User) => user
    }
  }

  override protected def loadGroups: Seq[Group] = {
    val groups = ldapTemplate.search(groupsBase, groupsFilter, SearchControls.SUBTREE_SCOPE, groupMapper.attributes.toArray, groupMapper)
    groups.asScala.toSeq.collect {
      case Some(group: Group) => group
    }
  }

}

class LdapUserMapper extends LdapAttributesMapper[Option[User]] {

  override val attributes = Set("samaccountname", "mail", "displayName", "physicaldeliveryofficename")

  override def mapFromAttributes(attributes: Attributes) = {
    for {
      account <- getAttribute(attributes, "samaccountname")
      address <- getAttribute(attributes, "mail")
      displayName <- getAttribute(attributes, "displayName")
      location = getAttribute(attributes, "physicaldeliveryofficename")
    } yield {
      User(displayName, address, account, None, location)
    }
  }

}

class LdapGroupMapper extends LdapAttributesMapper[Option[Group]] {

  override val attributes = Set("mail", "displayName")

  override def mapFromAttributes(attributes: Attributes) = {
    for {
      address <- getAttribute(attributes, "mail")
      displayName <- getAttribute(attributes, "displayName")
    } yield {
      Group(displayName, address)
    }
  }

}
