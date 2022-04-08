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

import com.cisco.cta.taxii.adapter.httpclient.HttpBodyWriter;
import com.cisco.cta.taxii.adapter.httpclient.HttpHeadersAppender;
import com.cisco.cta.taxii.adapter.persistence.TaxiiStatus;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpRequestFactory;

import java.io.OutputStream;
import java.net.URI;
import java.net.URL;

import static com.cisco.cta.taxii.adapter.httpclient.HasHeaderMatcher.hasAllTaxiiHeaders;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RequestFactoryTest {

    private RequestFactory requestFactory;
    private URL pollEndpoint;
    private HttpHeadersAppender headersAppender;
    private HttpHeaders headers;

    @Mock
    private ClientHttpRequestFactory httpRequestFactory;

    @Mock
    private HttpBodyWriter bodyWriter;

    @Mock
    private ClientHttpRequest request;

    @Mock
    private OutputStream body;

    private TaxiiStatus.Feed feed;

    @Before
    public void setUp() throws Exception {
        pollEndpoint = new URL("http://somehost/service");
        headers = new HttpHeaders();
        headersAppender = new HttpHeadersAppender();
        initMocks();
        requestFactory = new RequestFactory(pollEndpoint, httpRequestFactory, headersAppender, bodyWriter);
        feed = new TaxiiStatus.Feed();
        feed.setName("my-collection");
    }

    private void initMocks() throws Exception {
        when(httpRequestFactory
            .createRequest(
                new URI("http://somehost/service"),
                HttpMethod.POST))
            .thenReturn(request);
        when(request.getHeaders()).thenReturn(headers);
        when(request.getBody()).thenReturn(body);
    }

    @Test
    public void createInitialRequest() throws Exception {
        request = requestFactory.createPollRequest("123", feed);
        assertThat(headers, hasAllTaxiiHeaders());
        verify(bodyWriter).write("123", feed, body);
    }

    @Test
    public void createFulfillmentRequest() throws Exception {
        request = requestFactory.createFulfillmentRequest("123", feed, "1000#2000", 1);
        assertThat(headers, hasAllTaxiiHeaders());
        verify(bodyWriter).write("123", feed, "1000#2000", 1, body);
    }

}
