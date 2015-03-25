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

import static com.cisco.cta.taxii.adapter.HasHeaderMatcher.hasAllTaxiiHeaders;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.OutputStream;
import java.net.URI;
import java.net.URL;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpRequestFactory;

import com.cisco.cta.taxii.adapter.HttpBodyWriter;
import com.cisco.cta.taxii.adapter.HttpHeadersAppender;
import com.cisco.cta.taxii.adapter.RequestFactory;


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

    @Before
    public void setUp() throws Exception {
        pollEndpoint = new URL("http://somehost/service");
        headers = new HttpHeaders();
        headersAppender = new HttpHeadersAppender();
        initMocks();
        requestFactory = new RequestFactory(pollEndpoint, httpRequestFactory, headersAppender, bodyWriter);
    }

    private void initMocks() throws Exception {
        MockitoAnnotations.initMocks(this);
        when(httpRequestFactory
            .createRequest(
                new URI("http://somehost/service"),
                HttpMethod.POST))
            .thenReturn(request);
        when(request.getHeaders()).thenReturn(headers);
        when(request.getBody()).thenReturn(body);
    }

    @Test
    public void createRequest() throws Exception {
        request = requestFactory.create("my-collection");
        assertThat(headers, hasAllTaxiiHeaders());
        verify(bodyWriter).write("my-collection", body);
    }
}
