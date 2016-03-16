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

import java.net.URL;

import com.cisco.cta.taxii.adapter.persistence.TaxiiStatus;

import lombok.AllArgsConstructor;

import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpRequestFactory;

import com.cisco.cta.taxii.adapter.httpclient.HttpBodyWriter;
import com.cisco.cta.taxii.adapter.httpclient.HttpHeadersAppender;

/**
 * Factory to create complete TAXII requests.
 */
@AllArgsConstructor
public class RequestFactory {

    private final URL pollEndpoint;
    private final ClientHttpRequestFactory httpRequestFactory;
    private final HttpHeadersAppender httpHeadersAppender;
    private final HttpBodyWriter httpBodyWriter;


    /**
     * Create the TAXII request.
     * 
     * @param feed The TAXII feed.
     * @return TAXII poll request.
     * @throws Exception When any error occurs.
     */
    public ClientHttpRequest createPollRequest(String messageId, TaxiiStatus.Feed feed) throws Exception {
        ClientHttpRequest req = httpRequestFactory.createRequest(pollEndpoint.toURI(), HttpMethod.POST);
        httpHeadersAppender.appendTo(req.getHeaders());
        httpBodyWriter.write(messageId, feed, req.getBody());
        return req;
    }

    /**
     * Create the TAXII request.
     *
     * @param feed The TAXII feed name.
     * @return TAXII poll request.
     * @throws Exception When any error occurs.
     */
    public ClientHttpRequest createFulfillmentRequest(String messageId, TaxiiStatus.Feed feed, String resultId, Integer resultPartNumber) throws Exception {
        ClientHttpRequest req = httpRequestFactory.createRequest(pollEndpoint.toURI(), HttpMethod.POST);
        httpHeadersAppender.appendTo(req.getHeaders());
        httpBodyWriter.write(messageId, feed, resultId, resultPartNumber, req.getBody());
        return req;
    }

}
