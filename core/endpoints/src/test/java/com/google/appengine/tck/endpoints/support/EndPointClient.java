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
import java.nio.charset.Charset;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.config.ConnectionConfig;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;

/**
 * @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class EndPointClient {
    private CloseableHttpClient client;

    private int connectionTimeout = 30 * 1000;
    private String contentCharset = "UTF-8";

    public String doGet(URL url) throws Exception {
        return doRequest(new HttpGet(url.toURI()));
    }

    public String doPost(URL url) throws Exception {
        return doRequest(new HttpPost(url.toURI()));
    }

    public String doPut(URL url) throws Exception {
        return doRequest(new HttpPut(url.toURI()));
    }

    public String doDelete(URL url) throws Exception {
        return doRequest(new HttpDelete(url.toURI()));
    }

    public void shutdown() {
        if (client != null) {
            CloseableHttpClient tmp = client;
            client = null;
            try {
                tmp.close();
            } catch (IOException ignored) {
            }
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
    private synchronized CloseableHttpClient getClient() {
        if (client == null) {
            RequestConfig.Builder requestBuilder = RequestConfig.custom();
            requestBuilder.setConnectTimeout(connectionTimeout);

            ConnectionConfig.Builder connBuilder = ConnectionConfig.custom();
            connBuilder.setCharset(Charset.forName(getContentCharset()));

            // Create and initialize scheme registry
            RegistryBuilder<ConnectionSocketFactory> builder = RegistryBuilder.create();
            builder.register("http", getPlainFactory());
            builder.register("https", getSslFactory());
            Registry<ConnectionSocketFactory> registry = builder.build();

            HttpClientConnectionManager hccm = createClientConnectionManager(registry);

            HttpClientBuilder clientBuilder = HttpClients.custom();
            clientBuilder.setDefaultRequestConfig(requestBuilder.build());
            clientBuilder.setDefaultConnectionConfig(connBuilder.build());
            clientBuilder.setConnectionManager(hccm);

            client = clientBuilder.build();
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
    protected HttpClientConnectionManager createClientConnectionManager(Registry<ConnectionSocketFactory> schemeRegistry) {
        return new PoolingHttpClientConnectionManager(schemeRegistry);
    }

    protected ConnectionSocketFactory getPlainFactory() {
        return PlainConnectionSocketFactory.getSocketFactory();
    }

    /**
     * This ssl socket factory ignores validation.
     *
     * @return ssl socket factory
     */
    protected ConnectionSocketFactory getSslFactory() {
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

            return new SSLConnectionSocketFactory(sc, hostnameVerifier);
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

    public String getContentCharset() {
        return contentCharset;
    }

    public void setContentCharset(String contentCharset) {
        this.contentCharset = contentCharset;
    }
}
