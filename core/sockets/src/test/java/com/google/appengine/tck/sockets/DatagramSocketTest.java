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

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;

import static org.junit.Assert.assertNotNull;

/**
 * @author <a href="mailto:terryok@google.com">Terry Okamoto</a>
 */
@RunWith(Arquillian.class)
public class DatagramSocketTest extends SocketsTestBase {

    @Deployment
    protected static WebArchive getDeployment() {
        return getDefaultDeployment();
    }

    @Before
    public void setUp() {
        initGoogleDnsSet();
    }

    @Test
    public void testSendDatagramPacket() throws Exception {
        try (DatagramSocket socket = new DatagramSocket()) {

            DatagramPacket packet = createHelloDatagramPacket(GOOGLE_DNS, 53);
            socket.send(packet);

            // Timeout is not set so we can verify that at least socket.send() does not hang.
            assertNotNull(socket);
        }
    }

// Currently some issues with appspot causing timeouts on each test.  Reenable when fixed.
//
//    @Test
//    public void testConnect() throws Exception {
//        DatagramSocket socket = new DatagramSocket();
//        socket.connect(new InetSocketAddress(GOOGLE_DNS, 53));
//
//        DatagramPacket packet = createHelloDatagramPacket(GOOGLE_DNS, 53);
//        socket.send(packet);
//    }
//
//    @Test
//    public void testSoTimeout() throws Exception {
//        try (DatagramSocket socket = new DatagramSocket()) {
//            socket.setSoTimeout(10000);
//
//            DatagramPacket packet = createHelloDatagramPacket(GOOGLE_DNS, 53);
//            socket.send(packet);
//        }
//    }
//
//    @Test
//    public void testOptionSendBufferSize() throws Exception {
//        try (DatagramSocket socket = new DatagramSocket()) {
//            socket.setSoTimeout(10000);
//            int sendBufferSizeBefore = socket.getSendBufferSize();
//            socket.setSendBufferSize(42);
//
//            DatagramPacket packet = createHelloDatagramPacket(GOOGLE_DNS, 53);
//            socket.send(packet);
//
//            int sendBufferSizeAfter = socket.getSendBufferSize();
//            // either it's the same - as it's been completely ignored, or at least the "set" was ignored
//            assertTrue(sendBufferSizeBefore == sendBufferSizeAfter || sendBufferSizeAfter != 42);
//        }
//    }
//
//    @Test
//    public void testOptionReceiveBufferSize() throws Exception {
//        try (DatagramSocket socket = new DatagramSocket()) {
//            socket.setSoTimeout(10000);
//            int receiveBufferSizeBefore = socket.getReceiveBufferSize();
//            socket.setReceiveBufferSize(42);
//
//            DatagramPacket packet = createHelloDatagramPacket(GOOGLE_DNS, 53);
//            socket.send(packet);
//
//            int receiveBufferSizeAfter = socket.getReceiveBufferSize();
//            // either it's the same - as it's been completely ignored, or at least the "set" was ignored
//            assertTrue(receiveBufferSizeBefore == receiveBufferSizeAfter || receiveBufferSizeAfter != 42);
//        }
//    }
//
//    @Test
//    public void testOptionReuseAddress() throws Exception {
//        try (DatagramSocket socket = new DatagramSocket();
//             DatagramSocket socket2 = new DatagramSocket()) {
//
//            socket.setReuseAddress(true);
//
//            DatagramPacket packet = createHelloDatagramPacket(GOOGLE_DNS, 53);
//            socket.send(packet);
//
//
//            socket.close();  // Close it explicitly so socket2 can reuse it.
//
//            socket2.setReuseAddress(true);
//
//            socket2.connect(new InetSocketAddress(GOOGLE_DNS, 53));
//            DatagramPacket packet2 = createHelloDatagramPacket(GOOGLE_DNS, 53);
//            socket2.send(packet2);
//        }
//    }
//
//    @Test
//    public void testOptionTosTrafficClass() throws Exception {
//        DatagramSocket socket = new DatagramSocket();
//        socket.setSoTimeout(10000);
//        try {
//            socket.setTrafficClass(255);
//            DatagramPacket packet = createHelloDatagramPacket(GOOGLE_DNS, 53);
//            socket.send(packet);
//
//            assertEquals(255, socket.getTrafficClass());
//        } catch (SocketException se) {
//            // GAE does not support setTrafficClass()
//        } finally {
//            socket.close();
//        }
//    }
//
//    @Test
//    public void testGetLocalAddress() throws Exception {
//        try (DatagramSocket socket = new DatagramSocket()) {
//
//            InetAddress localInet = socket.getLocalAddress();
//
//            Set<String> addresses = getLocalHostAddressFromNetworkInterface();
//            if (addresses.isEmpty()) {  // GAE returns null for queries to NetworkInterface.getNetworkInterfaces()
//                assertEquals("127.0.0.1", localInet.getHostAddress());
//                assertEquals("localhost", localInet.getHostName());
//                assertEquals(false, localInet.isSiteLocalAddress());
//                assertEquals(true, localInet.isLoopbackAddress());
//            } else {
//                String errMsg = String.format("%s not contained in %s", localInet.getHostAddress(),
//                    convertSetToString(addresses));
//                assertTrue(errMsg, addresses.contains(localInet.getHostAddress()));
//                assertEquals(true, localInet.isSiteLocalAddress());
//            }
//
//            // assertTrue(localInet.is*())  returns null pointer when value is true. Arquillian bug?
//            assertEquals(false, localInet.isAnyLocalAddress());
//            assertEquals(false, localInet.isLinkLocalAddress());
//            assertEquals(false, localInet.isMCGlobal());
//            assertEquals(false, localInet.isMCNodeLocal());
//            assertEquals(false, localInet.isMCOrgLocal());
//            assertEquals(false, localInet.isMulticastAddress());
//        }
//    }
//
//    @Test
//    public void testGetRemoteSocketAddress() throws Exception {
//        try (DatagramSocket socket = new DatagramSocket()) {
//            DatagramPacket packet = createHelloDatagramPacket(GOOGLE_DNS, 53);
//            socket.send(packet);
//            SocketAddress address = socket.getRemoteSocketAddress();
//            assertTrue(address.toString().startsWith(GOOGLE_DNS) && address.toString().endsWith(":53"));
//        }
//    }
//
//    @Test
//    public void testGetInetAddress() throws Exception {
//        DatagramSocket socket = new DatagramSocket();
//        socket.connect(new InetSocketAddress(GOOGLE_DNS, 53));
//        InetAddress inet = socket.getInetAddress();
//        assertGoogleIpAddress(inet.getCanonicalHostName());
//        assertGoogleIpAddress(inet.getHostAddress());
//        assertEquals(GOOGLE_DNS, inet.getHostName());
//    }

    private DatagramPacket createHelloDatagramPacket(String hostName, int port) throws UnknownHostException {
        byte[] buf = "Hello".getBytes();
        InetAddress address = InetAddress.getByName(hostName);
        return new DatagramPacket(buf, buf.length, address, port);
    }


}
