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

import org.springframework.ws.client.core.support.WebServiceGatewaySupport
import javax.xml.bind.JAXBElement
import org.springframework.ws.client.core.WebServiceMessageCallback
import org.springframework.ws.WebServiceMessage
import org.springframework.ws.soap.SoapMessage
import com.microsoft.exchange.types.{RequestServerVersion, ExchangeVersionType}
import com.microsoft.exchange.messages.{ObjectFactory => MessageObjectFactory}
import com.microsoft.exchange.types.{ObjectFactory => TypeObjectFactory}
import java.lang.reflect.Method
import org.springframework.util.ReflectionUtils

/**
 * Convenience class to implement EWS data access objects.
 * @author Dmitriy Yefremov
 */
class EwsDaoSupport extends WebServiceGatewaySupport {

  val ExchangeVersion = ExchangeVersionType.EXCHANGE_2010_SP_2

  val MessageFactory = new MessageObjectFactory

  val TypeFactory = new TypeObjectFactory

  /**
   * Invokes a method on the service.
   * @param body request body object
   * @param headers optional headers to be added to the request
   * @tparam A response type
   * @return response object
   */
  def invoke[A <: AnyRef](body: AnyRef, headers: AnyRef*): A = {
    val requestWrapper = wrap(body, MessageFactory)
    val responseWrapper = getWebServiceTemplate.marshalSendAndReceive(requestWrapper, new HeadersCallBack(headers: _*))
    unwrap(responseWrapper)
  }

  private def wrap[A <: AnyRef](content: A, factory: AnyRef): JAXBElement[A] = {
    val method = findMethod(factory.getClass, content.getClass, classOf[JAXBElement[A]])
    val wrapper = method match {
      case Some(m) => ReflectionUtils.invokeMethod(method.get, factory, content)
      case None => throw new IllegalArgumentException("No wrapper method for type " + content.getClass + " in factory " + factory.getClass)
    }
    wrapper.asInstanceOf[JAXBElement[A]]
  }

  private def findMethod(clazz: Class[_], paramType: Class[_], returnType: Class[_]): Option[Method] = {
    val methods = if (clazz.isInterface) clazz.getMethods else clazz.getDeclaredMethods
    methods.find(method => {
      val returnTypeMatch = method.getReturnType == returnType
      val parameterTypes = method.getParameterTypes
      val parameterTypeMatch = parameterTypes.length == 1 && parameterTypes(0) == paramType
      returnTypeMatch && parameterTypeMatch
    })
  }

  private def unwrap[A <: AnyRef](wrapper: AnyRef): A = {
    wrapper match {
      case element: JAXBElement[_] => {
        element.getValue.asInstanceOf[A]
      }
    }
  }

  private class HeadersCallBack(headers: AnyRef*) extends WebServiceMessageCallback {

    def doWithMessage(message: WebServiceMessage) {
      // add mandatory version header
      addHeader(message, new RequestServerVersion(ExchangeVersion, null))
      // add other headers
      headers.foreach(header => {
        val wrapper = wrap(header, TypeFactory)
        addHeader(message, wrapper)
      })
    }

    def addHeader(message: WebServiceMessage, header: AnyRef) {
      val soapMessage = message.asInstanceOf[SoapMessage]
      val soapHeader = soapMessage.getSoapHeader
      val result = soapHeader.getResult
      getMarshaller.marshal(header, result)
    }

  }

}
