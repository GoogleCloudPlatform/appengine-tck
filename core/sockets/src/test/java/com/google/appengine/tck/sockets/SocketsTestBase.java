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

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

import com.google.appengine.tck.base.TestBase;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Before;

import static org.junit.Assert.assertTrue;

/**
 * @author <a href="mailto:terryok@google.com">Terry Okamoto</a>
 */
public abstract class SocketsTestBase extends TestBase {
    protected static final String WHOIS = "whois.internic.net";
    protected static final String GOOGLE_DNS = "google-public-dns-a.google.com";

    private final Set<String> googleDns = new HashSet<>();

    protected static WebArchive getDefaultDeployment() {
        WebArchive war = getTckDeployment();
        war.addClass(SocketsTestBase.class);
        return war;
    }

    @Before
    public void setUp() {
        initGoogleDnsSet();
    }

    protected static void assertInternicResponse(Socket socket) throws Exception {
        OutputStreamWriter out = new OutputStreamWriter(socket.getOutputStream(), "8859_1");
        out.write("=google.com\r\n");
        out.flush();

        InputStreamReader in = new InputStreamReader(socket.getInputStream(), "8859_1");
        String whoisResponse = toStream(in);

        assertTrue("Expected to find NS1.GOOGLE.COM in WHOIS response:" + whoisResponse, whoisResponse.contains("NS1.GOOGLE.COM"));
    }

    protected static String toStream(InputStreamReader in) throws IOException {
        StringBuilder sb = new StringBuilder();
        for (int c; (c = in.read()) != -1; ) {
            sb.append(String.valueOf((char) c));
        }
        return sb.toString();
    }

    protected void initGoogleDnsSet() {
        googleDns.add(GOOGLE_DNS);
        googleDns.add("8.8.8.8");
        googleDns.add("8.8.4.4");
        googleDns.add("2001:4860:4860::8888");
        googleDns.add("2001:4860:4860::8844");
    }

    protected static Set<String> getLocalHostAddressFromNetworkInterface() throws Exception {
        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
        if (interfaces == null) {
            return Collections.emptySet();
        }

        Set<String> hostAddresses = new HashSet<>();

        while (interfaces.hasMoreElements()) {
            NetworkInterface current = interfaces.nextElement();
            Enumeration<InetAddress> addresses = current.getInetAddresses();
            while (addresses.hasMoreElements()) {
                InetAddress addr = addresses.nextElement();
                hostAddresses.add(addr.getHostAddress());
            }
        }

        return hostAddresses;
    }

    protected void assertGoogleIpAddress(String ipStr) {
        String errMsg = String.format("%s should be one of: %s", ipStr, googleDns);
        assertTrue(errMsg, googleDns.contains(ipStr));
    }
}
