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

package com.microsoft.exchange.utils;

import javax.xml.namespace.QName;

/**
 * Namespace related shared code.
 *
 * @author Dmitriy Yefremov
 */
public class NsHelper {

    /**
     * Types namespace URI
     */
    public static final String TYPES_NS_URI = "http://schemas.microsoft.com/exchange/services/2006/types";

    /**
     * Types namespace URI
     */
    public static final String MESSAGES_NS_URI = "http://schemas.microsoft.com/exchange/services/2006/messages";

    /**
     * Creates a qualified name object with the given name and the Types namespace URI.
     *
     * @param name qualified name local part
     * @return the resulting QName object
     */
    public static QName typesName(String name) {
        return new QName(TYPES_NS_URI, name);
    }

    /**
     * Creates a qualified name object with the given name and the Messages namespace URI.
     *
     * @param name qualified name local part
     * @return the resulting QName object
     */
    public static QName messagesName(String name) {
        return new QName(MESSAGES_NS_URI, name);
    }

}
