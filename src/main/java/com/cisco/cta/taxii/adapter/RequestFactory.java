/*
   Copyright 2015 Cisco Systems

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/

package com.cisco.cta.taxii.adapter;

import java.net.URI;
import java.net.URL;

import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpRequestFactory;

/**
 * Factory to create complete TAXII requests.
 */
public class RequestFactory {

    private final URI pollEndpoint;
    private final ClientHttpRequestFactory httpRequestFactory;
    private final HttpHeadersAppender httpHeadersAppender;
    private final HttpBodyWriter httpBodyWriter;

    public RequestFactory(
            URL pollEndpoint,
            ClientHttpRequestFactory httpRequestFactory,
            HttpHeadersAppender httpHeadersAppender,
            HttpBodyWriter httpBodyWriter
            ) throws Exception {
        this.pollEndpoint = pollEndpoint.toURI();
        this.httpRequestFactory = httpRequestFactory;
        this.httpHeadersAppender = httpHeadersAppender;
        this.httpBodyWriter = httpBodyWriter;
    }

    /**
     * Create the TAXII request.
     * 
     * @param feed The TAXII feed name.
     * @return TAXII poll request.
     * @throws Exception When any error occurs.
     */
    public ClientHttpRequest create(String feed) throws Exception {
        ClientHttpRequest req = httpRequestFactory.createRequest(pollEndpoint, HttpMethod.POST);
        httpHeadersAppender.appendTo(req.getHeaders());
        httpBodyWriter.write(feed, req.getBody());
        return req;
    }

}
