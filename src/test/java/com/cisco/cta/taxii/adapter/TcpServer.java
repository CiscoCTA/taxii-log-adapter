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
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.Charset;

import org.springframework.util.StreamUtils;

class TcpServer extends Thread implements AutoCloseable {

    private static int port = 19000;
    private static final Charset CHARSET = Charset.forName("UTF-8");

    private final ServerSocket serverSocket;
    private volatile String message;
    private volatile int connectionCount = 0;
    private volatile int errorCount = 0;
    private volatile boolean closed = false;

    public TcpServer() throws IOException {
        serverSocket = new ServerSocket(port++);
    }

    @Override
    public void run() {
        while(!closed) {
            try (
                Socket socket = serverSocket.accept();
                InputStream in = socket.getInputStream()
            ){
                message = StreamUtils.copyToString(in, CHARSET);
                connectionCount++;
            } catch (SocketException e) {
                // closing server - do nothing
            } catch (Exception e) {
                errorCount++;
                e.printStackTrace();
            }
        }
    }

    public int getPort() {
        return serverSocket.getLocalPort();
    }

    public String getMessage() {
        return message;
    }

    public int getErrorCount() {
        return errorCount;
    }

    public int getConnectionCount() {
        return connectionCount;
    }

    @Override
    public void close() throws Exception {
        closed = true;
        serverSocket.close();
        join();
    }

}
