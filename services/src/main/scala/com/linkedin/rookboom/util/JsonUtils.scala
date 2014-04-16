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

import com.fasterxml.jackson.module.scala.DefaultScalaModule
import java.lang.reflect.{Type, ParameterizedType}
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.core.`type`.TypeReference
import java.io.{InputStream, File, OutputStream}
import java.io.StringWriter

/**
 * Helper functions to deal with JSON.
 * @author Dmitriy Yefremov
 */
object JsonUtils {

  val mapper = new ObjectMapper()
  mapper.registerModule(DefaultScalaModule)

  def serialize(value: Any): String = {
    val writer = new StringWriter()
    mapper.writeValue(writer, value)
    writer.toString
  }

  def serialize(value: Any, file: File) {
    mapper.writeValue(file, value)
  }

  def serialize(value: Any, out: OutputStream) {
    mapper.writeValue(out, value)
  }

  def deserialize[T: Manifest](value: String): T = {
    mapper.readValue(value, typeReference[T])
  }

  def deserialize[T: Manifest](in: InputStream): T = {
    mapper.readValue(in, typeReference[T])
  }

  def deserialize[T: Manifest](file: File): T = {
    mapper.readValue(file, typeReference[T])
  }

  private[this] def typeReference[T: Manifest] = new TypeReference[T] {
    override def getType = typeFromManifest(manifest[T])
  }

  private[this] def typeFromManifest(m: Manifest[_]): Type = {
    if (m.typeArguments.isEmpty) {
      m.runtimeClass
    }
    else new ParameterizedType {
      def getRawType = m.runtimeClass

      def getActualTypeArguments = m.typeArguments.map(typeFromManifest).toArray

      def getOwnerType = null
    }
  }
}
