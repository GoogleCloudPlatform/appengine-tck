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

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author <a href="mailto:terryok@google.com">Terry Okamoto</a>
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
@RunWith(Arquillian.class)
public class SocketsTest extends SocketsTestBase {

    @Deployment
    protected static WebArchive getDeployment() {
        return getDefaultDeployment();
    }

    @Before
    public void setUp() {
        initGoogleDnsSet();
    }

    @Test
    public void testOutboundSocket() throws Exception {
        try (Socket socket = createSocket()) {
            socket.setSoTimeout(10000);

            assertInternicResponse(socket);
        }
    }

    @Test
    public void testSetOptionSOLinger() throws Exception {
        try (Socket socket = createSocket()) {
            socket.setSoTimeout(10000);
            int lingerBefore = socket.getSoLinger();
            socket.setSoLinger(true, 1);

            assertInternicResponse(socket);

            int lingerAfter = socket.getSoLinger();
            assertEquals(lingerBefore, lingerAfter);
        }
    }

    @Test
    public void testOptionKeepAlive() throws Exception {
        try (Socket socket = createSocket()) {
            socket.setSoTimeout(10000);
            boolean keepAliveBefore = socket.getKeepAlive();
            socket.setKeepAlive(true);

            assertInternicResponse(socket);

            boolean keepAliveAfter = socket.getKeepAlive();
            assertEquals(keepAliveBefore, keepAliveAfter);
        }
    }

    @Test
    public void testOptionSendBufferSize() throws Exception {
        try (Socket socket = createSocket()) {
            socket.setSoTimeout(10000);
            int sendBufferSizeBefore = socket.getSendBufferSize();
            socket.setSendBufferSize(42);

            assertInternicResponse(socket);

            int sendBufferSizeAfter = socket.getSendBufferSize();
            // either it's the same - as it's been completely ignored, or at least the "set" was ignored
            assertTrue(sendBufferSizeBefore == sendBufferSizeAfter || sendBufferSizeAfter != 42);
        }
    }

    @Test
    public void testOptionReceiveBufferSize() throws Exception {
        try (Socket socket = createSocket()) {
            socket.setSoTimeout(10000);
            int receiveBufferSizeBefore = socket.getReceiveBufferSize();
            socket.setReceiveBufferSize(42);

            assertInternicResponse(socket);

            int receiveBufferSizeAfter = socket.getReceiveBufferSize();
            // either it's the same - as it's been completely ignored, or at least the "set" was ignored
            assertTrue(receiveBufferSizeBefore == receiveBufferSizeAfter || receiveBufferSizeAfter != 42);
        }
    }

    @Test
    public void testOptionReuseAddress() throws Exception {
        try (Socket socket = new Socket();
             Socket socket2 = new Socket()) {

            socket.setReuseAddress(true);
            socket.connect(new InetSocketAddress(WHOIS, 43));
            assertInternicResponse(socket);
            socket.close();  // Close it explicitly so socket2 can reuse it.

            socket2.setReuseAddress(true);

            socket2.connect(new InetSocketAddress(WHOIS, 43));
            assertInternicResponse(socket2);
        }
    }

    @Test
    public void testOptionTcpNoDelay() throws Exception {
        try (Socket socket = new Socket()) {
            boolean noDelayBefore = socket.getTcpNoDelay();
            socket.connect(new InetSocketAddress(WHOIS, 43));

            assertInternicResponse(socket);

            boolean noDelayAfter = socket.getTcpNoDelay();
            assertEquals(noDelayBefore, noDelayAfter);
        }
    }

    @Test
    public void testOptionOobInline() throws Exception {
        try (Socket socket = createSocket()) {
            socket.setSoTimeout(10000);
            int receiveBufferSizeBefore = socket.getReceiveBufferSize();
            socket.setOOBInline(true);
            socket.setReceiveBufferSize(42);

            assertInternicResponse(socket);

            int receiveBufferSizeAfter = socket.getReceiveBufferSize();
            // either it's the same - as it's been completely ignored, or at least the "set" was ignored
            assertTrue(receiveBufferSizeBefore == receiveBufferSizeAfter || receiveBufferSizeAfter != 42);
        }
    }

    @Test
    public void testOptionTosTrafficClass() throws Exception {
        Socket socket = new Socket();
        socket.setSoTimeout(10000);
        try {
            socket.setTrafficClass(255);
            socket.connect(new InetSocketAddress(WHOIS, 43));
            assertInternicResponse(socket);
            assertEquals(255, socket.getTrafficClass());
        } catch (SocketException se) {
            // GAE does not support setTrafficClass()
        } finally {
            socket.close();
        }

    }

    @Test
    public void testOptionSetPerformancePreferences() throws Exception {
        try (Socket socket = new Socket()) {
            socket.setPerformancePreferences(2, 1, 3);  // GAE ignores this.
            socket.connect(new InetSocketAddress(WHOIS, 43));
            assertInternicResponse(socket);
        }
    }

    @Test
    public void testGetLocalAddress() throws Exception {
        try (Socket socket = createSocket()) {
            assertInternicResponse(socket);

            InetAddress localInet = socket.getLocalAddress();

            Set<String> addresses = getLocalHostAddressFromNetworkInterface();
            if (addresses.isEmpty()) {  // GAE returns null for queries to NetworkInterface.getNetworkInterfaces()
                assertEquals("127.0.0.1", localInet.getHostAddress());
                assertEquals("localhost", localInet.getHostName());
                assertEquals(false, localInet.isSiteLocalAddress());
                assertEquals(true, localInet.isLoopbackAddress());
            } else {
                String errMsg = String.format("%s not contained in %s", localInet.getHostAddress(),
                    convertSetToString(addresses));
                assertTrue(errMsg, addresses.contains(localInet.getHostAddress()));
                assertEquals(true, localInet.isSiteLocalAddress());
            }

            // assertTrue(localInet.is*())  returns null pointer when value is true. Something with
            // java.net.Socket package since get null pointer outside of this framework.
            assertEquals(false, localInet.isAnyLocalAddress());
            assertEquals(false, localInet.isLinkLocalAddress());
            assertEquals(false, localInet.isMCGlobal());
            assertEquals(false, localInet.isMCNodeLocal());
            assertEquals(false, localInet.isMCOrgLocal());
            assertEquals(false, localInet.isMulticastAddress());
        }
    }

    @Test
    public void testIsReachable() throws Exception {
        try (Socket socket = createSocket()) {
            InetAddress inet = socket.getInetAddress();
            assertEquals(true, inet.isReachable(5000));
        }
    }

    @Test
    public void testGetRemoteSocketAddress() throws Exception {
        try (Socket socket = createSocket()) {
            SocketAddress address = socket.getRemoteSocketAddress();
            assertTrue(address.toString().startsWith(WHOIS) && address.toString().endsWith(":43"));
        }
    }

    @Test
    public void testConnect() throws Exception {
        Socket socket = new Socket();
        socket.connect(new InetSocketAddress(WHOIS, 43));
        assertInternicResponse(socket);
    }

    @Test
    public void testGetInetAddress() throws Exception {
        Socket socket = new Socket();
        socket.connect(new InetSocketAddress(GOOGLE_DNS, 53));
        InetAddress inet = socket.getInetAddress();
        assertGoogleIpAddress(inet.getCanonicalHostName());
        assertGoogleIpAddress(inet.getHostAddress());
        assertFalse(inet.isReachable(3000));
        assertEquals(GOOGLE_DNS, inet.getHostName());
    }

    protected Socket createSocket() throws IOException {
        return new Socket(WHOIS, 43);
    }

}
