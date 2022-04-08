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


import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class TcpOutputStreamTest {

    private TcpOutputStream out;
    private TcpServer tcpServer;

    @Before
    public void setUp() throws Exception {
        tcpServer = new TcpServer();
        tcpServer.start();
        out = new TcpOutputStream("localhost", tcpServer.getPort());
    }

    @After
    public void tearDown() throws Exception {
        tcpServer.close();
    }

    @Test(timeout = 5000)
    public void sendMessage() throws Exception {
        try (OutputStreamWriter writer = new OutputStreamWriter(out, StandardCharsets.UTF_8)) {
            writer.append("Hello TCP!");
        }
        while (tcpServer.getConnectionCount() == 0) {
            Thread.sleep(10);
        }
        assertThat(tcpServer.getErrorCount(), is(0));
        assertThat(tcpServer.getMessage(), is("Hello TCP!"));
    }

    @Test//(timeout=5000)
    public void sendTwoMessages() throws Exception {
        try (OutputStreamWriter writer = new OutputStreamWriter(out, StandardCharsets.UTF_8)) {
            writer.append("Alpha");
            writer.flush();
            writer.append("Beta");
        }
        while (tcpServer.getConnectionCount() < 2) {
            Thread.sleep(10);
        }
        assertThat(tcpServer.getErrorCount(), is(0));
        assertThat(tcpServer.getMessage(), is("Beta"));
    }

}
