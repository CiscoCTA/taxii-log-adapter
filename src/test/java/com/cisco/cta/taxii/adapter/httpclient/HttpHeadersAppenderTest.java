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

package com.cisco.cta.taxii.adapter.httpclient;

import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpHeaders;

import static com.cisco.cta.taxii.adapter.httpclient.HasHeaderMatcher.hasAllTaxiiHeaders;
import static com.cisco.cta.taxii.adapter.httpclient.HasHeaderMatcher.hasUserAgentHeader;
import static org.hamcrest.MatcherAssert.assertThat;

public class HttpHeadersAppenderTest {

    private HttpHeadersAppender appender;
    private HttpHeaders headers;

    @Before
    public void setUp() throws Exception {
        appender = new HttpHeadersAppender();
        headers = new HttpHeaders();
    }

    @Test
    public void appendHeaders() throws Exception {
        appender.appendTo(headers);
        assertThat(headers, hasAllTaxiiHeaders());
        assertThat(headers, hasUserAgentHeader());
    }

}
