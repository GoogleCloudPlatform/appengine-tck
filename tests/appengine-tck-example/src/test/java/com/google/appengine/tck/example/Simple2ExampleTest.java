/*
    * copyright (c) 2013 google inc.
    *
    * licensed under the apache license, version 2.0 (the "license"); you may not
    * use this file except in compliance with the license. you may obtain a copy of
    * the license at
    *
    * http://www.apache.org/licenses/license-2.0
    *
    * unless required by applicable law or agreed to in writing, software
    * distributed under the license is distributed on an "as is" basis, without
    * warranties or conditions of any kind, either express or implied. see the
    * license for the specific language governing permissions and limitations under
    * the license.
*/

package com.google.appengine.tck.example;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Example test cases that demonstrate how to run a client side test.
 */
@RunWith(Arquillian.class)
public class Simple2ExampleTest extends ExampleTestBase {

    private DatastoreService service;

    @Before
    public void setUp() {
        service = DatastoreServiceFactory.getDatastoreService();
    }

    /**
     * @param url the url of the container.
     * @throws Exception
     * @RunAsClient runs the test outside of the container.  The common
     * usage is to hit a test servlet with some requests, store the results in
     * datastore, then verify the results in a subsequent test case that has access
     * to the results.  @InSequence tells the runner which order to execute the test
     * cases.
     */
    @Test
    @RunAsClient
    @InSequence(1)
    public void invokeTimestampTest(@ArquillianResource URL url) throws Exception {
        String clientTimeStamp = "" + System.currentTimeMillis();
        URL testUrl = new URL(url, "examplePage.jsp?ts=" + clientTimeStamp);

        HttpURLConnection connection = (HttpURLConnection) testUrl.openConnection();
        connection.setRequestProperty("User-Agent", "TCK Example");
        String response = null;
        try {
            response = readFullyAndClose(connection.getInputStream()).trim();
        } finally {
            connection.disconnect();
        }

        // Verify result in client.
        Assert.assertEquals("Timestamp should be echoed back.", clientTimeStamp, response);

    }

    /**
     * This verifies the result on the server side of the invokeTimestampTest.
     *
     * @throws Exception
     */
    @Test
    @InSequence(2)
    public void testTimestampWrittenTest() throws Exception {
        Query query = new Query("example");

        PreparedQuery preparedQuery = service.prepare(query);

        // A real test would assert on the result here.
        //
        // Assert.assertTrue("Should have a result.",
        //   preparedQuery.countEntities(FetchOptions.Builder.withDefaults()) > 0);
    }

    private String readFullyAndClose(InputStream in) throws IOException {
        try {
            StringBuilder sbuf = new StringBuilder();
            int ch;
            while ((ch = in.read()) != -1) {
                sbuf.append((char) ch);
            }
            return sbuf.toString();
        } finally {
            in.close();
        }
    }
}
