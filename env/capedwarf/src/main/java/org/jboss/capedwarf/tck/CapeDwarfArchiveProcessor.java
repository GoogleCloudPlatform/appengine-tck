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

package org.jboss.capedwarf.tck;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.Properties;
import java.util.logging.Logger;

import com.google.appengine.tck.arquillian.EnvApplicationArchiveProcessor;
import com.google.appengine.tck.event.TestLifecycle;
import com.google.appengine.tck.util.Utils;
import org.jboss.shrinkwrap.api.Node;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class CapeDwarfArchiveProcessor extends EnvApplicationArchiveProcessor {
    private static final Logger log = Logger.getLogger(CapeDwarfArchiveProcessor.class.getName());

    private static final String CAPEDWARF_WEB =
        "<capedwarf-web-app>" +
            "    <admin>admin@capedwarf.org</admin>" +
            "    <xmpp>" +
            "        <host>talk.l.google.com</host>" +
            "        <port>5222</port>" +
            "        <username>%s</username>" +
            "        <password>%s</password>" +
            "    </xmpp>" +
            "    <mail>" +
            "        <property name=\"mail.transport.protocol\">smtp</property>" +
            "        <property name=\"mail.smtp.auth\">true</property>" +
            "        <property name=\"mail.smtp.starttls.enable\">true</property>" +
            "        <property name=\"mail.smtp.host\">smtp.gmail.com</property>" +
            "        <property name=\"mail.smtp.port\">587</property>" +
            "        <property name=\"mail.smtp.user\">%s</property>" +
            "        <property name=\"mail.smtp.password\">%s</property>" +
            "    </mail>\n" +
            "    <inbound-mail>" +
            "        <protocol>%s</protocol>" +
            "        <host>%s</host>" +
            "        <user>%s</user>" +
            "        <password>%s</password>" +
            "        <folder>%s</folder>" +
            "        <pollingInterval>5000</pollingInterval>" +
            "    </inbound-mail>" +
            "</capedwarf-web-app>";

    private static final String COMPATIBILITY_PROPERTIES = "capedwarf-compatibility.properties";
    private static final Properties COMPATIBILITY;

    static {
        COMPATIBILITY = new Properties();
        // Ignore WebSockets until PhantomJS supports it properly
        COMPATIBILITY.setProperty("disable.websockets.channel", "true");
    }

    @SuppressWarnings("unchecked")
    protected void handleWebArchiveInternal(WebArchive war) {
        addService(war, TestLifecycle.class,
            CapeDwarfExecutionLifecycle.class,
            CapeDwarfImageLifecycle.class,
            CapeDwarfInstanceLifecycle.class,
            CapeDwarfMergeLifecycle.class,
            CapeDwarfPropertyLifecycle.class,
            CapeDwarfServicesLifecycle.class,
            CapeDwarfTestContextEnhancer.class
        );

        war.addClass(CapedwarfEmailAddressFormatter.class);

        addCompatibility(war, COMPATIBILITY);

        String xmppUsername = System.getProperty("capedwarf.xmpp.username", "capedwarftest@gmail.com");
        String xmppPassword = System.getProperty("capedwarf.xmpp.password", "MISSING_PASSWORD");

        log.info(String.format("XMPP: %s / %s", xmppUsername, xmppPassword));

        String mailUser = System.getProperty("capedwarf.mail.username", "capedwarftest@gmail.com");
        String mailPassword = System.getProperty("capedwarf.mail.password", "MISSING_PASSWORD");

        String inboundMailProtocol = System.getProperty("capedwarf.inbound.mail.protocol", "imaps");
        String inboundMailHost = System.getProperty("capedwarf.inbound.mail.host", "imap.gmail.com");
        String inboundMailUsername = System.getProperty("capedwarf.inbound.mail.username", "capedwarftest@gmail.com");
        String inboundMailPassword = System.getProperty("capedwarf.inbound.mail.password", "MISSING_PASSWORD");
        String inboundMailFolder = System.getProperty("capedwarf.inbound.mail.folder", "INBOX");

        String cdw = String.format(CAPEDWARF_WEB,
            xmppUsername, xmppPassword,
            mailUser, mailPassword,
            inboundMailProtocol, inboundMailHost, inboundMailUsername, inboundMailPassword, inboundMailFolder);
        war.addAsWebInfResource(new StringAsset(cdw), "capedwarf-web.xml");
    }

    static void addCompatibility(WebArchive war, Properties extra) {
        final Properties properties = new Properties();

        final Node node = war.get("WEB-INF/classes/" + COMPATIBILITY_PROPERTIES);
        if (node != null) {
            InputStream is = node.getAsset().openStream();
            try {
                properties.load(is);
            } catch (IOException e) {
                throw new RuntimeException("Cannot read compatibility properties.", e);
            } finally {
                Utils.safeClose(is);
            }
            war.delete(node.getPath());
        }

        for (String key : extra.stringPropertyNames()) {
            if (properties.containsKey(key) == false) {
                properties.setProperty(key, extra.getProperty(key));
            }
        }

        final StringWriter writer = new StringWriter();
        try {
            properties.store(writer, "CapeDwarf testing!");
        } catch (IOException e) {
            throw new RuntimeException("Cannot write compatibility properties.", e);
        } finally {
            Utils.safeClose(writer);
        }
        final StringAsset asset = new StringAsset(writer.toString());
        war.addAsResource(asset, COMPATIBILITY_PROPERTIES);
    }
}
