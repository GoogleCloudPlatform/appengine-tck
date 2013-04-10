/*
 * Copyright (C) 2013 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.google.appengine.tck.util;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Use the ClientLogin API to log into a Google Account for AppEngine.
 *
 * @author sandersd@google.com
 * @author terryok@google.com
 */
public class GaeAuthClient {

    private HttpClient client;

    public GaeAuthClient(String servletUrl, String username, String password) throws AuthClientException {
        client = new DefaultHttpClient();
        authInit(servletUrl, username, password);
    }

    public HttpClient getClient() {
        return client;
    }

    public HttpResponse getUrl(String url) throws IOException {
        HttpGet get = new HttpGet(url);
        return client.execute(get);
    }

    protected void authInit(String servletUrl, String username, String password) throws AuthClientException {

      try {
          String authToken = getAuthToken(client, username, password);
          String cookieUrl = getCookieUrl(servletUrl, authToken);
          getAuthCookie(client, cookieUrl);
      } catch (URISyntaxException e) {
          throw new AuthClientException("Could not parse servlet URL", e);
      } catch (IOException e) {
          throw new AuthClientException(e);
      }
    }

    protected String getCookieUrl(String servletUrl, String authToken) throws
        URISyntaxException {
        URI servletUri = new URI(servletUrl);
        URI cookieUri = servletUri.resolve("/_ah/login?auth=" + authToken);
        return cookieUri.toString();
    }

    protected String getAuthToken(HttpClient client, String username, String password)
        throws AuthClientException, IOException {
        HttpPost request = getClientLoginRequest(username, password);
        HttpResponse response = client.execute(request);
        return parseClientLoginResponse(response);
    }

    protected void getAuthCookie(HttpClient client, String cookieUrl) throws IOException {
        client.execute(new HttpGet(cookieUrl));
    }

    protected HttpPost getClientLoginRequest(String username, String password)
      throws UnsupportedEncodingException {
    List<NameValuePair> params = new ArrayList<NameValuePair>();
    params.add(new BasicNameValuePair("accountType", "HOSTED_OR_GOOGLE"));
    params.add(new BasicNameValuePair("Email", username));
    params.add(new BasicNameValuePair("Passwd", password));
    params.add(new BasicNameValuePair("service", "ah"));
    params.add(new BasicNameValuePair("source", "Google-gae-tck-1.0.0"));

    HttpPost request = new HttpPost("https://www.google.com/accounts/ClientLogin");
    request.setEntity(new UrlEncodedFormEntity(params));
    return request;
    }

    // Parse the ClientLogin response and return the auth token
    protected String parseClientLoginResponse(HttpResponse response) throws
        AuthClientException, IOException {
        int status = response.getStatusLine().getStatusCode();
        if (status != HttpStatus.SC_OK && status != HttpStatus.SC_FORBIDDEN) {
          throw new AuthClientException("Unexpected ClientLogin HTTP status " + status);
        }

        String body = EntityUtils.toString(response.getEntity());
        Map<String, String> responseMap = parseClientLoginBody(body);

        if (status == HttpStatus.SC_OK) {
          String authToken = responseMap.get("Auth");
          if (authToken == null) {
            throw new AuthClientException("Auth token missing from ClientLogin response");
          }
          return authToken;
        } else {
          String message = "ClientLogin forbidden";
          // Base error code (eg. BadAuthentication)
          String error = responseMap.get("Error");
          if (error != null) {
            message += ": " + error;
          }
          // Additional error code, not usually present (eg. InvalidSecondFactor)
          String info = responseMap.get("Info");
          if (info != null) {
            message += " (" + info + ")";
          }
          throw new AuthClientException(message);
        }
    }

    // The body of the response is lines of key-value pairs
    protected Map<String, String> parseClientLoginBody(String body) {
        Map<String, String> responseMap = new HashMap<String, String>();
        for (String line : body.split("\n")) {
          int idx = line.indexOf("=");
          if (idx > 0) {
            responseMap.put(line.substring(0, idx), line.substring(idx + 1));
          }
        }
        return responseMap;
    }
}