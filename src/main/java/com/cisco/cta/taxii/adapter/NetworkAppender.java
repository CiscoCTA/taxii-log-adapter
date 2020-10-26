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

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.core.net.SyslogOutputStream;

/**
 * Sends events to a target host via TCP or UDP.
 * To configure the connection add this appender to your Logback configuration.
 */
public class NetworkAppender extends AppenderBase<ILoggingEvent> {

    /**
     * Network protocol enumeration.
     */
    public enum Protocol{TCP, UDP}
    
    private String host;
    private int port;
    private Protocol protocol = Protocol.TCP;
    private Writer out;

    /**
     * @param host Destination host name.
     */
    public void setHost(String host) {
        this.host = host;
    }

    /**
     * @param port Destination port number.
     */
    public void setPort(int port) {
        this.port = port;
    }

    /**
     * @param protocol Transport protocol.
     */
    public void setProtocol(Protocol protocol) {
        this.protocol = protocol;
    }

    @Override
    protected void append(ILoggingEvent eventObject) {
        try {
            while (!isStarted()) {
                addWarn("Not yet started.");
                Thread.sleep(1000);
            }
            out.write(eventObject.getFormattedMessage());
            out.write("\n");
            out.flush();
        } catch (Exception e) {
            addError("Error while sending datagram to " + host + ':' + port, e);
        }
    }

    /**
     * Logback lifecycle method.
     */
    @Override
    public void start() {
        try {
          out = new OutputStreamWriter(createOutputStream(), StandardCharsets.UTF_8);
        } catch (Exception e) {
          addError("Error while creating connection", e);
        }
        super.start();
      }

    private OutputStream createOutputStream() throws IOException {
        switch (protocol) {
        case TCP:
            return new TcpOutputStream(host, port);
        case UDP:
            return new SyslogOutputStream(host, port);
        default:
            throw new IOException("Unsupported protocol: " + protocol);
        }
    }

    /**
     * Logback lifecycle method.
     */
    @Override
    public void stop() {
        try {
            out.close();
        } catch (IOException e) {
            addError("Error closing output writer", e);
        }
        super.stop();
    }
}
