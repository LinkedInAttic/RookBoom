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

package com.microsoft.exchange;


import com.microsoft.exchange.types.ExchangeVersionType;
import com.microsoft.exchange.types.RequestServerVersion;
import com.microsoft.exchange.utils.NsHelper;
import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.common.logging.Slf4jLogger;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.interceptor.LoggingInInterceptor;
import org.apache.cxf.interceptor.LoggingOutInterceptor;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transport.http.asyncclient.AsyncHTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.NTCredentials;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;
import java.net.URL;
import java.util.Map;


/**
 * A factory to create service ports.
 *
 * @author Dmitriy Yefremov
 */
public class ExchangeWebService {

    public static final RequestServerVersion VERSION = new RequestServerVersion(ExchangeVersionType.EXCHANGE_2010_SP_2, null);

    private static final long DEFAULT_TIMEOUT = 30000;

    private static final QName SERVICE = NsHelper.messagesName("ExchangeWebService");

    private final Service service;

    private final boolean logging;

    public ExchangeWebService() {
        this(false);
    }

    public ExchangeWebService(boolean logging) {
        this.logging = logging;
        URL wsdlUrl = ExchangeWebService.class.getResource("exchange.wsdl");
        service = Service.create(wsdlUrl, SERVICE);

    }

    public ExchangeServicePortType getServicePort() {
        return service.getPort(ExchangeServicePortType.class);
    }

    private void initLogging(Client client) {
        if (logging) {
            LogUtils.setLoggerClass(Slf4jLogger.class);
            client.getInInterceptors().add(new LoggingInInterceptor());
            client.getOutInterceptors().add(new LoggingOutInterceptor());
        }
    }

    public ExchangeServicePortType getNtlmServicePort(String endpointUrl, String userName, String password, String domain) {
        ExchangeServicePortType port = getServicePort();
        Map<String, Object> requestContext = ((BindingProvider) port).getRequestContext();
        requestContext.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, endpointUrl);
        // setup NTLM authentication
        requestContext.put(Credentials.class.getName(), new NTCredentials(userName, password, "", domain));
        // force async conduit
        requestContext.put(AsyncHTTPConduit.USE_ASYNC, Boolean.TRUE);
        // turn off chunking
        Client client = ClientProxy.getClient(port);
        initLogging(client);
        HTTPConduit conduit = (HTTPConduit) client.getConduit();
        HTTPClientPolicy clientPolicy = conduit.getClient();
        clientPolicy.setAllowChunking(false);
        clientPolicy.setAutoRedirect(true);
        clientPolicy.setConnectionTimeout(DEFAULT_TIMEOUT);
        clientPolicy.setReceiveTimeout(DEFAULT_TIMEOUT);
        clientPolicy.setAsyncExecuteTimeout(DEFAULT_TIMEOUT);
        return port;
    }

}
