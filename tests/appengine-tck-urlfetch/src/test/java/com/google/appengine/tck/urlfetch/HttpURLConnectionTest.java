package com.google.appengine.tck.urlfetch;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import org.jboss.arquillian.junit.Arquillian;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

/**
 * HttpURLConnection tests
 */
@RunWith(Arquillian.class)
public class HttpURLConnectionTest extends URLFetchTestBase {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void fetchExistingPage() throws Exception {
        String content = fetchUrl("http://www.google.org/", 200);
    }

    @Test
    public void fetchNonExistentPage() throws Exception {
        String content = fetchUrl("http://www.google.com/404", 404);
    }

    @Test
    public void fetchNonExistentSite() throws Exception {
        thrown.expect(IOException.class);
        String content = fetchUrl("http://i.do.not.exist/", 503);
    }

    protected String fetchUrl(String url, int expectedResponseCode) throws IOException {
        URLConnection conn = new URL(url).openConnection();
        Assert.assertTrue("URLConenction is not HttpURLConnection: " + conn, conn instanceof HttpURLConnection);
        HttpURLConnection connection = (HttpURLConnection) conn;
        connection.connect();
        try {
            int responseCode = connection.getResponseCode();
            assertEquals(url, expectedResponseCode, responseCode);
            return getContent(conn);
        } finally {
            connection.disconnect();
        }
    }

    private String getContent(URLConnection connection) throws IOException {
        InputStream stream = connection.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        try {
            String content = "";
            String line;
            while ((line = reader.readLine()) != null) {
                content += line;
            }
            return content;
        } finally {
            reader.close();
        }
    }
}