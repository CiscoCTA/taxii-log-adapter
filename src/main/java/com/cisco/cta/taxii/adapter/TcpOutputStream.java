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

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

/**
 * Sends data over the TCP protocol.
 * All write methods are writing into a local buffer.
 */
class TcpOutputStream extends OutputStream {

    private final String host;
    private final int port;
    private final ByteArrayOutputStream buf = new ByteArrayOutputStream(1024);

    /**
     * Initialize target host parameters.
     * 
     * @param host Host name or IP address.
     * @param port Port number.
     */
    public TcpOutputStream(String host, int port) {
        this.host = host;
        this.port = port;
    }

    @Override
    public void write(int b) throws IOException {
        buf.write(b);
    }

    /**
     * Make a network connection and send all buffered data.
     * The connection is closed before this method exits.
     */
    @SuppressFBWarnings(
            value = "RCN_REDUNDANT_NULLCHECK_WOULD_HAVE_BEEN_A_NPE",
            justification = "try with resources - false positive if compiled with Java 11"
    )
    @Override
    public void flush() throws IOException {
        try (
            Socket socket = new Socket(host, port);
            OutputStream out = socket.getOutputStream()
        ) {
            out.write(buf.toByteArray());
            out.flush();
            buf.reset();
        }
    }

    /**
     * Flush all remaining buffered data before closing.
     */
    @Override
    public void close() throws IOException {
        flush();
    }

}
