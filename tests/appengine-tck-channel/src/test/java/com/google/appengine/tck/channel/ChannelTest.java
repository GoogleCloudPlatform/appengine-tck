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

package com.google.appengine.tck.channel;

import static org.junit.Assert.assertEquals;

import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.net.URL;

/**
 * Test that messages can be sent from the browser to the server,
 * and sent from the server to the browser.
 *
 * Requires <a href="http://phantomjs.org/download.html">phantomjs</a> Specify the absolute path
 * of the binary phantomjs with -Dtck.phantomjs.path
 *
 * For reference:
 *   1) Initially tried HtmlUnit, but it could not receive messages from the server.
 *   2) Tried using WebDriver implicit wait times with the following:
 *          driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
 *      but made the client unable to send or receive messages, thus we use sync()/sleep().
 *
 * @author <a href="mailto:terryok@google.com">Terry Okamoto</a>
 */
@RunWith(Arquillian.class)
public class ChannelTest extends ChannelTestBase {

    private WebDriver driver = null;

    @Before
    public void setUp() {
        initDriver();
    }

    private void initDriver() {
        if (driver != null) {
            return;
        }

        String phantomjsPath = System.getProperty("tck.phantomjs.path");
        DesiredCapabilities caps = DesiredCapabilities.phantomjs();
        caps.setCapability(PhantomJSDriverService.PHANTOMJS_EXECUTABLE_PATH_PROPERTY, phantomjsPath);
        driver = new PhantomJSDriver(caps);
    }

    @After
    public void tearDown() throws Exception {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    @RunAsClient
    @InSequence(10)
    public void testSimpleMessage(@ArquillianResource URL url) throws Exception {
        // 1. Create our test with a unique channel id.
        String channelId = "" + System.currentTimeMillis();
        driver.get(url + "channelPage.jsp?test-channel-id=" + channelId);

        // 2. Verify that the server received our channel id and is using it for this tests.
        WebElement channel = driver.findElement(By.id("channel-id"));
        assertEquals(channelId, channel.getText());

        // 3. The browser waits for the channel to be opened.  There is an implicit timeout of 30 seconds
        //    if it is not found.
        sync(5000L);
        WebElement status = driver.findElement(By.id("opened-" + channelId));

        // 4. Send a message via channel to the server.
        WebElement sendButton = driver.findElement(By.id("send-message-button"));
        sendButton.click();

        // 4. Check that we attempted to send a message from the browser to server.
        sync();
        WebElement lastMsg = driver.findElement(By.id("last-sent-message-" + channelId));

        // 5. Now verify that the browser got the ACK from the server.
        sync(10000L);
        String receivedMsgId = "last-received-message-" + channelId;
        WebElement lastReceived = driver.findElement(By.id(receivedMsgId));
        String expectedMsg = "echo-from-server:msg:" + channelId;
        assertEquals(expectedMsg, lastReceived.getText());
    }
}
