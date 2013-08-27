/*
 * Copyright 2013 Google Inc. All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.appengine.tck.sockets;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

/**
 * @author <a href="mailto:terryok@google.com">Terry Okamoto</a>
 */
@RunWith(Arquillian.class)
public class SocketsTest extends SocketsTestBase {

    private static final String WHOIS = "whois.internic.net";

    @Test
    public void testOutboundSocket() throws Exception {
        Socket socket = new Socket(WHOIS, 43);

        try {
            socket.setSoTimeout(10000);
            assertInternicResponse(socket);
        } finally {
            socket.close();
        }
    }

    @Test
    public void testSetOptionSOLinger() throws Exception {
        Socket socket = new Socket(WHOIS, 43);

        try {
            socket.setSoTimeout(10000);
            int lingerBefore = socket.getSoLinger();
            socket.setSoLinger(true, 1);
            assertInternicResponse(socket);

            int lingerAfter = socket.getSoLinger();
            assertEquals(lingerBefore, lingerAfter);
        } finally {
            socket.close();
        }
    }

    @Test
    public void testOptionKeepAlive() throws Exception {
        Socket socket = new Socket(WHOIS, 43);

        try {
            socket.setSoTimeout(10000);
            boolean keepAliveBefore = socket.getKeepAlive();
            socket.setKeepAlive(true);
            assertInternicResponse(socket);

            boolean keepAliveAfter = socket.getKeepAlive();
            assertEquals(keepAliveBefore, keepAliveAfter);
        } finally {
            socket.close();
        }
    }

    @Test
    public void testOptionSendBufferSize() throws Exception {
        Socket socket = new Socket(WHOIS, 43);

        try {
            socket.setSoTimeout(10000);
            int sendBufferSizeBefore = socket.getSendBufferSize();
            socket.setSendBufferSize(42);
            assertInternicResponse(socket);

            int sendBufferSizeAfter = socket.getSendBufferSize();
            assertEquals(sendBufferSizeBefore, sendBufferSizeAfter);
        } finally {
            socket.close();
        }
    }

    @Test
    public void testOptionReceiveBufferSize() throws Exception {
        Socket socket = new Socket(WHOIS, 43);

        try {
            socket.setSoTimeout(10000);
            int receiveBufferSizeBefore = socket.getReceiveBufferSize();
            socket.setReceiveBufferSize(42);
            assertInternicResponse(socket);

            int receiveBufferSizeAfter = socket.getReceiveBufferSize();
            assertEquals(receiveBufferSizeBefore, receiveBufferSizeAfter);
        } finally {
            socket.close();
        }
    }

    private void assertInternicResponse(Socket socket) throws Exception {
        OutputStreamWriter out = new OutputStreamWriter(socket.getOutputStream(), "8859_1");
        out.write("=google.com\r\n");
        out.flush();

        InputStreamReader in = new InputStreamReader(socket.getInputStream(), "8859_1");
        String whoisResponse = toStream(in);

        if (!whoisResponse.contains("NS1.GOOGLE.COM")) {
            fail("Expected to find NS1.GOOGLE.COM in WHOIS response:" +
                whoisResponse);
        }
    }

    private String toStream(InputStreamReader in) throws IOException {
        StringBuilder sb = new StringBuilder();
        for (int c = 0; (c = in.read()) != -1; ) {
            sb.append(String.valueOf((char) c));
        }
        return sb.toString();
    }
}
