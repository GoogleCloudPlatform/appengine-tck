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

package com.google.appengine.tck.appidentity;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.PublicKey;
import java.security.Signature;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import com.google.appengine.api.appidentity.AppIdentityService;
import com.google.appengine.api.appidentity.AppIdentityServiceFactory;
import com.google.appengine.api.appidentity.AppIdentityServiceFailureException;
import com.google.appengine.api.appidentity.PublicCertificate;
import com.google.appengine.tck.event.Property;
import com.google.apphosting.api.ApiProxy;
import org.apache.commons.validator.routines.EmailValidator;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author <a href="mailto:terryok@google.com">Terry Okamoto</a>
 */
@RunWith(Arquillian.class)
public class AppIdentityServiceTest extends AppIdentityTestBase {

    private AppIdentityService appIdentity;

    @Deployment
    public static WebArchive getDeployment() {
        return getDefaultDeployment();
    }

    @Before
    public void setUp() {
        appIdentity = AppIdentityServiceFactory.getAppIdentityService();
    }

    @Test
    public void testGetAccessTokenWithScope() {
        Date beforeRequest = new Date();
        List<String> scopes = new ArrayList<>();
        scopes.add("https://www.googleapis.com/auth/urlshortener");
        AppIdentityService.GetAccessTokenResult tokenResult = appIdentity.getAccessToken(scopes);

        Assert.assertNotNull(tokenResult);
        Assert.assertTrue("Token should not be blank.", !tokenResult.getAccessToken().isEmpty());

        String errMsg = "Expiration time should at least be after request time. " +
            dateDebugStr("Before-Request", beforeRequest) + ", " + dateDebugStr("Expiration-Time=", tokenResult.getExpirationTime());
        Assert.assertTrue(errMsg, beforeRequest.getTime() < tokenResult.getExpirationTime().getTime());
        log.info("AccessToken: " + tokenResult.getAccessToken() +
            " Expiration: " + tokenResult.getExpirationTime());

        // Retrieve it again, should be same since it grabs it from a cache.
        AppIdentityService.GetAccessTokenResult tokenResult2 = appIdentity.getAccessToken(scopes);

        Assert.assertEquals(tokenResult.getAccessToken(), tokenResult2.getAccessToken());
    }

    @Test
    public void testGetAccessTokenUncached() {
        Date beforeRequest = new Date();
        List<String> scopes = new ArrayList<>();
        scopes.add("https://www.googleapis.com/auth/urlshortener");

        // Although we do not verify whether the result came from the cache or not,
        // the token should at least be valid.
        AppIdentityService.GetAccessTokenResult tokenResult = appIdentity.getAccessTokenUncached(scopes);

        Assert.assertNotNull(tokenResult);
        Assert.assertTrue("Token should not be blank.", !tokenResult.getAccessToken().isEmpty());

        String errMsg = "Expiration time should at least be after request time. " +
            dateDebugStr("Before-Request", beforeRequest) + ", " + dateDebugStr("Expiration-Time=", tokenResult.getExpirationTime());
        Assert.assertTrue(errMsg, beforeRequest.getTime() < tokenResult.getExpirationTime().getTime());
        log.info("AccessToken: " + tokenResult.getAccessToken() +
            " Expiration: " + tokenResult.getExpirationTime());
    }

    /*
     * Retrieve the default Google Cloud Storage bucket name.
     */
    @Test
    public void testGetDefaultGcsBucketName() {
        ApiProxy.Environment env = ApiProxy.getCurrentEnvironment();
        String expectedBucketName;
        Property property = property("testGetDefaultGcsBucketName");
        if (property.exists()) {
            expectedBucketName = property.getPropertyValue();
        } else {
            expectedBucketName = (String) env.getAttributes().get("com.google.appengine.runtime.default_version_hostname");
        }

        try {
            String bucketName = appIdentity.getDefaultGcsBucketName();
            Assert.assertEquals(expectedBucketName, bucketName);
        } catch (AppIdentityServiceFailureException aisfe) {
            //TODO: This means that there is no default bucket setup for this project.  Have a better way to verify this.
        }
    }

    @Test
    public void testGetServiceAccountName() {
        String serviceAccountName = appIdentity.getServiceAccountName();
        String errMsg = serviceAccountName + " is not valid.";
        if (execute("testGetServiceAccountName")) {
            Assert.assertTrue(errMsg, EmailValidator.getInstance().isValid(serviceAccountName));
        } else {
            Assert.assertTrue(!serviceAccountName.isEmpty());
        }
    }

    @Test
    public void testParseFullAppId() {
        // [(partition)~][(domain):](display-app-id)
        ApiProxy.Environment env = ApiProxy.getCurrentEnvironment();
        String hostname = (String) env.getAttributes().get("com.google.appengine.runtime.default_version_hostname");
        AppIdentityService.ParsedAppId parsed = appIdentity.parseFullAppId(hostname);

        Assert.assertEquals(createParsed(parsed), appEngineServer, parsed.getDomain());
        Assert.assertEquals(appIdproperty, parsed.getId());
        Assert.assertTrue(parsed.getPartition().equals("s~") || parsed.getPartition().equals("e~"));
    }

    @Test
    public void testGetPublicCertificates() throws Exception {
        Collection<PublicCertificate> certs = appIdentity.getPublicCertificatesForApp();
        Assert.assertTrue("No certificates returned.", !certs.isEmpty());

        for (PublicCertificate publicCert : certs) {
            Assert.assertTrue("No name for certificate.", !publicCert.getCertificateName().trim().isEmpty());

            String pemFormat = publicCert.getX509CertificateInPemFormat();
            String errMsg = "getX509CertificateInPemFormat():" + pemFormat;
            Assert.assertTrue(errMsg, pemFormat.startsWith("-----BEGIN CERTIFICATE-----"));
            Assert.assertTrue(errMsg, pemFormat.trim().endsWith("-----END CERTIFICATE-----"));

            InputStream stream = new ByteArrayInputStream(publicCert.getX509CertificateInPemFormat().getBytes("UTF-8"));
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            Certificate cert = cf.generateCertificate(stream);

            PublicKey pk = cert.getPublicKey();
            Assert.assertNotNull(pk.getEncoded());
        }
    }

    @Test
    /**
     * Verify that all certificates returned will validate signForApp().  Any invalid signature or
     * exception will cause the test to fail.
     */
    public void testSignForApp() throws Exception {
        Collection<PublicCertificate> certs = appIdentity.getPublicCertificatesForApp();
        byte[] blob = "abcdefg".getBytes();
        AppIdentityService.SigningResult result = appIdentity.signForApp(blob);
        byte[] signedBlob = result.getSignature();
        boolean res = verifySignatureWithAllCertsForApp(blob, signedBlob, certs);

        // assertTrue(res) returns null, so using assertEquals()
        Assert.assertEquals("signature.verify() returned false. See logs.", true, res);
        Assert.assertTrue(!result.getKeyName().isEmpty());
    }

    /*
     * Try all certificates with the signed blob.  Any cert throws an exception is a test Error.
     * Any cert is valid with no exceptions, then pass.
     *
     * Debugging this can be a pain, so being extra verbose and explicit with logging.
     */
    private boolean verifySignatureWithAllCertsForApp(byte[] blob, byte[] signedBlob,
                                                      Collection<PublicCertificate> certsForApp) {

        if (certsForApp.isEmpty()) {
            throw new IllegalStateException("No certificates to validate.  Must have at least 1.");
        }
        int currentCertNum = 0;
        int totalValid = 0;
        int totalInvalid = 0;
        List<Exception> allExceptions = new ArrayList<>();

        for (PublicCertificate publicCert : certsForApp) {
            Signature signature;
            Certificate cert = null;
            currentCertNum++;

            log.info("Processing certNum:" + currentCertNum);
            try {
                byte[] certBytes = publicCert.getX509CertificateInPemFormat().getBytes("UTF-8");
                InputStream stream = new ByteArrayInputStream(certBytes);
                signature = Signature.getInstance("SHA256withRSA");  // Make this configurable?
                CertificateFactory cf = CertificateFactory.getInstance("X.509");
                cert = cf.generateCertificate(stream);
                log.info(cert.toString());

                PublicKey pk = cert.getPublicKey();
                signature.initVerify(pk);
                signature.update(blob);
                boolean isValidSignature = signature.verify(signedBlob);

                if (isValidSignature) {
                    totalValid++;
                } else {
                    totalInvalid++;
                }
                log.info("certNum:" + currentCertNum + ": is valid:" + isValidSignature);

                // These can be thrown:
                // UnsupportedEncodingException, NoSuchAlgorithmException, CertificateException,
                // SignatureException, InvalidKeyException
            } catch (Exception e) {
                Exception logException = createExceptionForLog(e, currentCertNum, cert);
                allExceptions.add(logException);
                log.info(e.toString());
            }
        }
        String summary = "totalCerts:" + certsForApp.size() + ": totalValid:" + totalValid +
                " totalInvalid:" + totalInvalid + " totalExceptions:" + allExceptions.size();
        log.info(summary);

        // At least one certificate caused an exception so make test Error.
        if (allExceptions.size() > 0) {
            throw new IllegalStateException(summary + "\n\n" + exceptionListToString(allExceptions));
        }

        // At least one signature was valid and no exceptions thrown.
        return (totalValid > 0);
    }

    private String exceptionListToString(List<Exception> exceptionList) {
        StringBuilder sb = new StringBuilder();
        for (Exception e : exceptionList) {
            sb.append(e.toString());
            sb.append("\n\n");
        }
        return sb.toString();
    }

    private Exception createExceptionForLog(Exception e, int certNum, Certificate cert) {
        return new Exception(e + ": certNum:" + certNum + " : " + cert.toString());
    }

    private String createParsed(AppIdentityService.ParsedAppId parsed) {
        return parsed.getDomain() + " : " + parsed.getId() + " : " + parsed.getPartition();
    }

    private String dateDebugStr(String comment, Date date) {
        return comment + "=" + date.toString() + ", " + date.getTime() + ")";
    }
}
