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

package com.linkedin.rookboom.layout

import com.linkedin.rookboom.util.{LdapAttributesMapper, Reloadable, Logging}
import javax.naming.directory.{SearchControls, Attributes}
import org.springframework.ldap.core.LdapTemplate
import java.util.TimeZone
import collection.JavaConverters._


/**
 * A LDAP based implementation of the LayoutManager interface.
 * @author Dmitriy Yefremov
 */
class LdapLayoutManager(val ldapTemplate: LdapTemplate,
                        val searchBase: String,
                        val searchFilter: String) extends AbstractLayoutManager with Logging with Reloadable {

  val roomMapper = new LdapRoomMapper

  def loadRooms(): Seq[Room] = {
    log.info("Fetching LDAP conference rooms...")
    val results = ldapTemplate.search(searchBase, searchFilter, SearchControls.SUBTREE_SCOPE, roomMapper.attributes.toArray, roomMapper)
    val rooms = results.asScala.toSeq.collect {
      case Some(room: Room) => room
    }
    log.info("There are {} rooms fetched", rooms.size)
    rooms
  }

  override protected def loadLayouts(): Map[String, Layout] = {
    val allRooms = loadRooms()
    allRooms.groupBy(_.layout).map { case (layoutId, layoutRooms) =>
      layoutId -> toLayout(layoutId, layoutRooms)
    }
  }

  private def toLayout(id: String, rooms: Seq[Room]) = {
    Layout(id, id, TimeZone.getDefault, rooms)
  }

}

class LdapRoomMapper extends LdapAttributesMapper[Option[Room]] {

  val defaultLayout = "default"

  override val attributes = Set("mail", "displayName", "msExchResourceCapacity", "msExchHideFromAddressLists", "physicaldeliveryofficename")

  override def mapFromAttributes(attributes: Attributes): Option[Room] = {
    val hidden = getAttribute(attributes, "msExchHideFromAddressLists", "false").toBoolean
    // if the room is hidden, don't process it any further
    if (hidden) {
      None
    } else {
      getRoom(attributes)
    }
  }

  def getRoom(attributes: Attributes): Option[Room] = {
    for {
      mail <- getAttribute(attributes, "mail")
      displayName <- getAttribute(attributes, "displayName")
      capacity = getAttribute(attributes, "msExchResourceCapacity", "1").toInt
      location = getAttribute(attributes, "physicaldeliveryofficename", defaultLayout)
    } yield {
      val attributes = Map(
        "capacity" -> capacity.toString
      )
      Room(mail, displayName, location, attributes)
    }
  }

}

