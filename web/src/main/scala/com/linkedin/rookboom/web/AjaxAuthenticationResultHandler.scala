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

import javax.servlet.http.{HttpServletResponse, HttpServletRequest}
import org.springframework.security.web.authentication.{AuthenticationFailureHandler, SavedRequestAwareAuthenticationSuccessHandler}
import org.springframework.security.core.{AuthenticationException, Authentication}

/**
 * @author Sergey Skrobotov, s.skrobotov@gmail.com
 */
class AjaxAuthenticationResultHandler extends SavedRequestAwareAuthenticationSuccessHandler
with AuthenticationFailureHandler {

  override def onAuthenticationSuccess(request: HttpServletRequest,
                                       response: HttpServletResponse,
                                       authentication: Authentication) {
    //super.onAuthenticationSuccess(request, response, authentication)

    request.getParameter("ajax") match {
      case "true" => {
        clearAuthenticationAttributes(request)
        response.sendRedirect("/?format=json")
      }
      case _ => super.onAuthenticationSuccess(request, response, authentication)
    }
  }

  def onAuthenticationFailure(request: HttpServletRequest,
                              response: HttpServletResponse,
                              exception: AuthenticationException) {
    request.getParameter("ajax") match {
      case "true" => {
        clearAuthenticationAttributes(request)
        response.sendRedirect("/?format=json")
      }
      case _ => {}
    }
  }
}