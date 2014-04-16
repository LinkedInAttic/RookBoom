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

import org.apache.commons.lang.StringUtils


/**
 * This exception is used to pass an error message to the user. So the message must not be empty and must contain meaningful information.
 * @author Dmitriy Yefremov
 */
class UserVisibleException(message: String, cause: Throwable = null) extends RuntimeException(message, cause) {

  require(StringUtils.isNotBlank(message), "Message can't be blank")

}
