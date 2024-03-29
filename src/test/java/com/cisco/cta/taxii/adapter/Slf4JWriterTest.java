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

import ch.qos.logback.core.Appender;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.io.Writer;

import static com.cisco.cta.taxii.adapter.IsEventContaining.verifyLog;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@RunWith(MockitoJUnitRunner.class)
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
        writer = new Slf4JWriter(logger, statistics);
        ((ch.qos.logback.classic.Logger) LoggerFactory.getLogger(Slf4JWriter.class)).addAppender(mockAppender);
    }

    @Test
    public void doNotWriteUnfinishedLine() throws Exception {
        char[] chars = "PAYLOAD".toCharArray();
        writer.write(chars);
        verifyNoInteractions(logger);
        verifyNoInteractions(statistics);
        writer.close();
    }

    @Test
    public void writeFinishedLine() throws Exception {
        char[] chars = "PAYLOAD\n".toCharArray();
        writer.write(chars);
        verify(logger, only()).info("PAYLOAD");
        assertThat(statistics.getLogs(), is(1L));
        writer.close();
    }

    @Test
    public void ignoreCharactersOutsideBoundaries() throws Exception {
        char[] chars = "beforePAYLOAD\nafter\nend".toCharArray();
        writer.write(chars, 6, 8);
        verify(logger, only()).info("PAYLOAD");
        assertThat(statistics.getLogs(), is(1L));
        writer.close();
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
        verifyNoInteractions(logger);
        verifyNoInteractions(statistics);
        writer.close();
    }

    @Test(expected = OutputValidationException.class)
    public void reactsOnJsonValidationError() throws Exception {
        char[] chars = "PAYLOAD\n".toCharArray();
        MDC.put(OutputValidationException.MDC_KEY, "test error");
        writer.write(chars);
        writer.close();
    }

}
