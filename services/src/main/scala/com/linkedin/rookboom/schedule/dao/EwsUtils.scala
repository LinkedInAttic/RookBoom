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

package com.linkedin.rookboom.schedule.dao

import com.microsoft.exchange.types._
import scala.collection.JavaConverters._
import javax.xml.bind.DatatypeConverter

/**
 * Common functions to work with EWS.
 * @author Dmitriy Yefremov
 */
object EwsUtils {

  /**
   * Specifies the clean global ObjectID.
   * See http://msdn.microsoft.com/en-us/library/cc839502.aspx
   */
  val CleanGlobalObjectIdProperty = new PathToExtendedFieldType()
    .withDistinguishedPropertySetId(DistinguishedPropertySetType.MEETING)
    .withPropertyId(0x23)
    .withPropertyType(MapiPropertyTypeType.BINARY)

  val CleanGlobalObjectIdElement = new ObjectFactory().createExtendedFieldURI(CleanGlobalObjectIdProperty)

  def getExtendedProperty(item: ItemType, propertyPath: PathToExtendedFieldType): Option[String] = {
    val property = item.getExtendedProperty.asScala.find(_.getExtendedFieldURI == propertyPath)
    property.map(_.getValue)
  }

  def hexToBase64(s: String) = {
    val data = DatatypeConverter.parseHexBinary(s)
    DatatypeConverter.printBase64Binary(data)
  }
}
