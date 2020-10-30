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


import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.cisco.cta.taxii.adapter.NetworkAppender.Protocol;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;

public class NetworkAppenderTest {
    
    private NetworkAppender appender;

    @Before
    public void setUp() throws Exception {
        appender = new NetworkAppender();
        appender.setHost("localhost");
        appender.setContext(new LoggerContext());
    }

    @After
    public void tearDown() throws Exception {
        appender.stop();
    }

    @Test
    public void start() throws Exception {
        appender.start();
        assertTrue(appender.isStarted());
    }

    @Test(timeout=5000)
    public void sendViaUdp() throws Exception {
        try (DatagramServer datagramServer = new DatagramServer()) {
            datagramServer.start();
            appender.setProtocol(Protocol.UDP);
            appender.setPort(datagramServer.getPort());
            appender.start();
            ILoggingEvent event = mock(ILoggingEvent.class);
            when(event.getFormattedMessage()).thenReturn("Hello message!");
            appender.doAppend(event);
            while(datagramServer.getDatagramCount() == 0) {
                Thread.sleep(10);
            }
            assertThat(datagramServer.getErrorCount(), is(0));
            assertThat(datagramServer.getMessage(), is("Hello message!\n"));
        }
    }

    @Test(timeout=5000)
    public void sendViaTcp() throws Exception {
        try (TcpServer tcpServer = new TcpServer()) {
            tcpServer.start();
            appender.setProtocol(Protocol.TCP);
            appender.setPort(tcpServer.getPort());
            appender.start();
            ILoggingEvent event = mock(ILoggingEvent.class);
            when(event.getFormattedMessage()).thenReturn("Hello message!");
            appender.doAppend(event);
            while(tcpServer.getConnectionCount() == 0) {
                Thread.sleep(10);
            }
            assertThat(tcpServer.getErrorCount(), is(0));
            assertThat(tcpServer.getMessage(), is("Hello message!\n"));
        }
    }
}
