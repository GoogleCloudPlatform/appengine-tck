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

package com.google.appengine.tck.endpoints.support;

import java.io.IOException;
import java.net.URL;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.scheme.SchemeSocketFactory;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.util.EntityUtils;

/**
 * @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class EndPointClient {
    private HttpClient client;

    private int connectionTimeout = 30 * 1000;
    private HttpVersion httpVersion = HttpVersion.HTTP_1_1;
    private String contentCharset = "UTF-8";
    private int port = 80;
    private int sslPort = 443;

    public String doGet(URL url) throws Exception {
        return doRequest(new HttpGet(url.toURI()));
    }

    public String doPost(URL url) throws Exception {
        HttpPost post = new HttpPost(url.toURI());
//        post.setHeader("Content-type", "application/json");
//        post.setEntity(new StringEntity("{\"state\": \"----X----\"}"));
        return doRequest(post);
    }

    public String doPut(URL url) throws Exception {
        return doRequest(new HttpPut(url.toURI()));
    }

    public String doDelete(URL url) throws Exception {
        return doRequest(new HttpDelete(url.toURI()));
    }

    public void shutdown() {
        if (client != null) {
            HttpClient tmp = client;
            client = null;
            tmp.getConnectionManager().shutdown();
        }
    }

    private String doRequest(HttpUriRequest request) throws IOException {
        HttpClient httpClient = getClient();
        HttpResponse response = httpClient.execute(request);
        return EntityUtils.toString(response.getEntity());
    }

    /**
     * Get client.
     *
     * @return the client
     */
    private synchronized HttpClient getClient() {
        if (client == null) {
            HttpParams params = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(params, getConnectionTimeout());
            HttpProtocolParams.setVersion(params, getHttpVersion());
            HttpProtocolParams.setContentCharset(params, getContentCharset());

            // Create and initialize scheme registry
            SchemeRegistry schemeRegistry = new SchemeRegistry();
            schemeRegistry.register(new Scheme("http", getPort(), getPlainFactory()));
            schemeRegistry.register(new Scheme("https", getSslPort(), getSslFactory()));

            ClientConnectionManager ccm = createClientConnectionManager(schemeRegistry);

            client = createClient(ccm, params);
        }

        return client;
    }

    /**
     * Create client connection manager.
     * <p/>
     * Create an HttpClient with the ThreadSafeClientConnManager.
     * This connection manager must be used if more than one thread will
     * be using the HttpClient.
     *
     * @param schemeRegistry the scheme registry
     * @return new client connection manager
     */
    protected ClientConnectionManager createClientConnectionManager(SchemeRegistry schemeRegistry) {
        return new PoolingClientConnectionManager(schemeRegistry);
    }

    /**
     * Create new http client.
     *
     * @param ccm    the client connection manager
     * @param params the http params
     * @return new http client
     */
    protected HttpClient createClient(ClientConnectionManager ccm, HttpParams params) {
        return new DefaultHttpClient(ccm, params);
    }

    protected SchemeSocketFactory getPlainFactory() {
        return PlainSocketFactory.getSocketFactory();
    }

    /**
     * This ssl socket factory ignores validation.
     *
     * @return ssl socket factory
     */
    protected SchemeSocketFactory getSslFactory() {
        try {
            TrustManager trm = new X509TrustManager() {
                public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
                }

                public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
                }

                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
            };

            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, new TrustManager[]{trm}, null);

            X509HostnameVerifier hostnameVerifier = new X509HostnameVerifier() {
                public void verify(String host, SSLSocket ssl) throws IOException {
                }

                public void verify(String host, X509Certificate cert) throws SSLException {
                }

                public void verify(String host, String[] cns, String[] subjectAlts) throws SSLException {
                }

                public boolean verify(String s, SSLSession sslSession) {
                    return true;
                }
            };

            return new SSLSocketFactory(sc, hostnameVerifier);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public int getConnectionTimeout() {
        return connectionTimeout;
    }

    public void setConnectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    public HttpVersion getHttpVersion() {
        return httpVersion;
    }

    public void setHttpVersion(HttpVersion httpVersion) {
        this.httpVersion = httpVersion;
    }

    public String getContentCharset() {
        return contentCharset;
    }

    public void setContentCharset(String contentCharset) {
        this.contentCharset = contentCharset;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getSslPort() {
        return sslPort;
    }

    public void setSslPort(int sslPort) {
        this.sslPort = sslPort;
    }
}
