/*
 * Copyright 2021-2021 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.vmware.devops;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.util.Base64;
import java.util.Properties;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.io.IOUtils;

public class Utils {
    public static final String BEGIN_CERT = "-----BEGIN CERTIFICATE-----";
    public static final String END_CERT = "-----END CERTIFICATE-----";

    public static String readFile(String filePath) throws IOException {
        return IOUtils.toString(getInputStream(filePath), StandardCharsets.UTF_8);
    }

    public static Properties readProperties(String filePath) {
        Properties properties = new Properties();
        try {
            properties.load(getInputStream(filePath));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }

        return properties;
    }

    private static InputStream getInputStream(String filePath) throws FileNotFoundException {
        File file = new File(filePath);
        if (file.isFile() && file.exists()) {
            return new FileInputStream(file);
        }

        return Thread.currentThread().getContextClassLoader()
                .getResourceAsStream(filePath);
    }

    public static String urlEncode(String s) throws UnsupportedEncodingException {
        return URLEncoder
                .encode(s, StandardCharsets.UTF_8.toString()).replace("+", "%20");
    }

    public static String getCertificateFromUrl(String url)
            throws IOException, NoSuchAlgorithmException, KeyManagementException,
            CertificateEncodingException {
        HttpsURLConnection connection = (HttpsURLConnection) new URL(url).openConnection();
        SSLContext sc = SSLContext.getInstance("SSL");
        sc.init(null, trustAllCertsTrustManager(), new java.security.SecureRandom());
        connection.setSSLSocketFactory(sc.getSocketFactory());
        connection.connect();

        String result = "";
        Certificate[] certs = connection.getServerCertificates();
        for (Certificate cert : certs) {
            result += certificateToString(cert);
        }

        return result;
    }

    public static String certificateToString(final Certificate certificate) throws
            CertificateEncodingException {
        final Base64.Encoder encoder = Base64.getMimeEncoder(64, System.lineSeparator().getBytes());

        final byte[] content = certificate.getEncoded();
        final String encoded = new String(encoder.encode(content));
        return BEGIN_CERT + System.lineSeparator() + encoded + System.lineSeparator() + END_CERT
                + System.lineSeparator();
    }

    public static TrustManager[] trustAllCertsTrustManager() {
        return new TrustManager[] {
                new X509TrustManager() {
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }

                    public void checkClientTrusted(
                            java.security.cert.X509Certificate[] certs, String authType) {
                    }

                    public void checkServerTrusted(
                            java.security.cert.X509Certificate[] certs, String authType) {
                    }
                }
        };
    }

    public static void writeFile(File output, byte[] data) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(output)) {
            fos.write(data);
        }
    }

    public static boolean isEmpty(String s) {
        return s == null || s.isEmpty();
    }
}
