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

package com.cisco.cta.taxii.adapter.filter;

import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.spi.FilterReply;
import com.cisco.cta.taxii.adapter.OutputValidationException;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.slf4j.MDC;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;

public class JsonValidationFilterTest {
    private JsonValidationFilter jsonValidationFilter = new JsonValidationFilter();

    private String readFirstLine(String resource) throws IOException {
        try(InputStream istream = getClass().getResourceAsStream(resource)) {
            List<String> lines = IOUtils.readLines(istream);
            return lines.get(0);
        }
    }

    @Test
    public void decideValidJson() throws IOException {
        LoggingEvent loggingEvent = new LoggingEvent();
        loggingEvent.setMessage(readFirstLine("/valid-json.json"));
        FilterReply reply = jsonValidationFilter.decide(loggingEvent);
        assertThat(reply, is(FilterReply.ACCEPT));
        assertThat(MDC.get(OutputValidationException.MDC_KEY), nullValue());
    }

    @Test
    public void decideInvalidJson() throws IOException {
        LoggingEvent loggingEvent = new LoggingEvent();
        loggingEvent.setMessage(readFirstLine("/invalid-json.json"));
        try {
            FilterReply reply = jsonValidationFilter.decide(loggingEvent);
            assertThat(reply, is(FilterReply.DENY));
            assertThat(MDC.get(OutputValidationException.MDC_KEY), notNullValue());
        }finally {
            MDC.remove(OutputValidationException.MDC_KEY);
        }
    }

}
