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

import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.Graphene;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.net.URL;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;

/**
 * Test that messages can be sent from the browser to the server,
 * and sent from the server to the browser.
 *
 * For reference:
 * 1) Initially tried HtmlUnit, but it could not receive messages from the server.
 * 2) Tried using WebDriver implicit wait times with the following:
 * driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
 * but made the client unable to send or receive messages, thus we use sync()/sleep().
 *
 * FIXME Graphene2 should handle timeouts better using requests Guards and Waits
 *
 * @author <a href="mailto:terryok@google.com">Terry Okamoto</a>
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 * @author <a href="mailto:kpiwko@redhat.com">Karel Piwko</a>
 */
@SuppressWarnings("UnusedDeclaration")
@RunWith(Arquillian.class)
public class ChannelTest extends ChannelTestBase {

    @Drone
    private WebDriver driver;

    @Test
    @RunAsClient
    @InSequence(10)
    public void testSimpleMessage(@ArquillianResource URL url) throws Exception {
        // 1. Create our test with a unique channel id.
        final String channelId = String.valueOf(System.currentTimeMillis());
        driver.get(url + "/channelPage.jsp?test-channel-id=" + channelId);

        // 2. Verify that the server received our channel id and is using it for this tests.
        WebElement channel = driver.findElement(By.id("channel-id"));
        assertEquals(channelId, channel.getText());

        // 3. The browser waits for the channel to be opened.  There is an implicit timeout of 30 seconds if it is not found.
        Graphene.waitModel(driver).until().element(By.id("opened-" + channelId)).is().present();

        // 4. Send a message via channel to the server.
        WebElement sendButton = driver.findElement(By.id("send-message-button"));
        sendButton.click();

        // 4. Check that we attempted to send a message from the browser to server.
        Graphene.waitModel(driver).until().element(By.id("last-sent-message-" + channelId)).is().present();

        // 5. Now verify that the browser got the ACK from the server.
        String receivedMsgId = "last-received-message-" + channelId;
        Graphene.waitModel(driver).until().element(By.id(receivedMsgId)).is().present();

        WebElement lastReceived = driver.findElement(By.id(receivedMsgId));
        String expectedMsg = "echo-from-server:msg:" + channelId;
        assertEquals(expectedMsg, lastReceived.getText());
    }

    @Test
    @RunAsClient
    @InSequence(20)
    public void testTimeout(@ArquillianResource URL url) throws Exception {
        // 1. Create our test with a unique channel id.
        final String channelId = "" + System.currentTimeMillis();

        // Set timeout for 1 minute.
        String params = String.format("/channelPage.jsp?test-channel-id=%s&timeout-minutes=%d", channelId, 1);
        driver.get(url + params);

        // 2. Verify that the server received our channel id and is using it for this tests.
        WebElement channel = driver.findElement(By.id("channel-id"));
        assertEquals(channelId, channel.getText());

        // 3. Verify that the channel gets closed after the 1 minute timeout.
        Graphene.waitModel(driver).until().element(By.id("status")).text().equalTo("opened");

        // This should put us over the 1 minute timeout.
        Graphene.waitModel(driver).withTimeout(90, TimeUnit.SECONDS).until().element(By.id("status")).text().equalTo("closed");
    }
}
