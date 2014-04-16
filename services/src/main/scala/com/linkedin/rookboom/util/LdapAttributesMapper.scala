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

package com.linkedin.rookboom.util

import javax.naming.directory.Attributes
import org.springframework.ldap.core.AttributesMapper


/**
 * A trait for mapping of LDAP attributes to objects.
 * @author Dmitriy Yefremov
 */
trait LdapAttributesMapper[T <: AnyRef] extends AttributesMapper {

  /**
   * Limit requested attributes to only what is really needed.
   */
  val attributes: Set[String]

  override def mapFromAttributes(attributes: Attributes): T

  def getAttribute(attrs: Attributes, name: String): Option[String] = {
    require(attributes.contains(name), "Attribute isn't requested: " + name)
    attrs.get(name) match {
      case null => None
      case attr => attr.get() match {
        case a: String => Some(a)
        case _ => None
      }
    }
  }

  def getAttribute(attrs: Attributes, name: String, default: String): String = {
    getAttribute(attrs, name).getOrElse(default)
  }

}
