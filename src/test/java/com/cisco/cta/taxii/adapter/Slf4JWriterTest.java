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

import static com.cisco.cta.taxii.adapter.IsEventContaining.verifyLog;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.io.Writer;

import com.cisco.cta.taxii.adapter.filter.JsonValidationFilter;
import org.jboss.logging.MDC;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cisco.cta.taxii.adapter.AdapterStatistics;
import com.cisco.cta.taxii.adapter.Slf4JWriter;

import ch.qos.logback.core.Appender;

@SuppressWarnings({"rawtypes", "unchecked"})
public class Slf4JWriterTest {

    @Mock
    private Logger logger;

    private Writer writer;
    
    @Spy
    private AdapterStatistics statistics = new AdapterStatistics();

    @Mock
    private Appender mockAppender;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        writer = new Slf4JWriter(logger, statistics);
        ((ch.qos.logback.classic.Logger) LoggerFactory.getLogger(Slf4JWriter.class)).addAppender(mockAppender);
    }

    @Test
    public void doNotWriteUnfinishedLine() throws Exception {
        char[] chars = "PAYLOAD".toCharArray();
        writer.write(chars);
        verifyZeroInteractions(logger);
        verifyZeroInteractions(statistics);
    }

    @Test
    public void writeFinishedLine() throws Exception {
        char[] chars = "PAYLOAD\n".toCharArray();
        writer.write(chars);
        verify(logger, only()).info("PAYLOAD");
        assertThat(statistics.getLogs(), is(1L));
    }

    @Test
    public void ignoreCharactersOutsideBoundaries() throws Exception {
        char[] chars = "beforePAYLOAD\nafter\nend".toCharArray();
        writer.write(chars, 6, 8);
        verify(logger, only()).info("PAYLOAD");
        assertThat(statistics.getLogs(), is(1L));
    }

    @Test
    public void logErrorOnClosingBeforeFinishedLine() throws Exception {
        char[] chars = "PAYLOAD\nend".toCharArray();
        writer.write(chars);
        writer.close();
        verifyLog(mockAppender, "unfinished");
        assertThat(statistics.getLogs(), is(1L));
    }

    @Test
    public void closingAfterFinishedLineDoesNothing() throws Exception {
        char[] chars = "PAYLOAD\n".toCharArray();
        writer.write(chars);
        writer.close();
        assertThat(statistics.getLogs(), is(1L));
    }

    @Test
    public void flushDoesNothing() throws Exception {
        writer.flush();
        verifyZeroInteractions(logger);
        verifyZeroInteractions(statistics);
    }

    @Test(expected = JsonValidationException.class)
    public void reactsOnJsonValidationError() throws Exception {
        char[] chars = "PAYLOAD\n".toCharArray();
        MDC.put(JsonValidationFilter.JSON_VALIDATION_ERROR, "test error");
        writer.write(chars);
    }

}
