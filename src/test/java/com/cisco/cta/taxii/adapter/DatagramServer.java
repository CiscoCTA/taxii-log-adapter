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

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

class DatagramServer extends Thread implements AutoCloseable {

    private final DatagramSocket socket;
    private final DatagramPacket packet = new DatagramPacket(new byte[256], 256);
    private volatile String message;
    private volatile int datagramCount = 0;
    private volatile int errorCount = 0;

    public DatagramServer() throws SocketException {
        socket = new DatagramSocket();
    }

    @Override
    public void run() {
        try {
            socket.receive(packet);
            message = new String(packet.getData(), packet.getOffset(), packet.getLength(), "UTF-8");
            datagramCount++;
        } catch (SocketException e) {
            // closing server - do nothing
        } catch (Exception e) {
            errorCount++;
            e.printStackTrace();
        }
    }
    
    public int getPort() {
        return socket.getLocalPort();
    }

    public String getMessage() {
        return message;
    }

    public int getErrorCount() {
        return errorCount;
    }

    public int getDatagramCount() {
        return datagramCount;
    }

    @Override
    public void close() throws Exception {
        socket.close();
        join();
    }

}
