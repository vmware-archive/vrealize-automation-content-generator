/*
 * Copyright 2021-2021 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.vmware.devops.client;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Map;
import java.util.stream.Collectors;

import javax.net.ssl.SSLContext;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.Getter;

import com.vmware.devops.SerializationUtils;
import com.vmware.devops.Utils;
import com.vmware.devops.client.cloudassembly.CloudAssemblyClient;
import com.vmware.devops.client.codestream.CodestreamClient;
import com.vmware.devops.client.servicebroker.ServiceBrokerClient;

public class Client {
    public static final HttpClient HTTP_CLIENT = initializeHttpClient();

    private static final String LOGIN_ENDPOINT = "/csp/gateway/am/api/login?access_token";
    private static final String AUTHORIZE_ENDPOINT = "csp/gateway/am/api/auth/api-tokens/authorize";

    public static final String ACCEPT_HEADER = "Accept";
    public static final String CONTENT_TYPE_HEADER = "Content-Type";
    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String CONTENT_TYPE_APPLICATION_JSON = "application/json";
    public static final String CONTENT_TYPE_X_WWW_FORM_URLENCODED = "application/x-www-form-urlencoded";

    @Getter
    private CodestreamClient codestream;

    @Getter
    private CloudAssemblyClient cloudAssembly;

    @Getter
    private ServiceBrokerClient serviceBroker;

    @Getter
    private String refreshToken;

    private String accessToken;
    private long accessTokenExpirationTime;

    public Client(String loginInstance, String instance, String refreshToken)
            throws IOException, InterruptedException, URISyntaxException {
        this.refreshToken = refreshToken;
        initializeClients(instance, getAccessToken(loginInstance, refreshToken));
    }

    public Client(String loginInstance, String instance, String username, String password)
            throws IOException, URISyntaxException, InterruptedException {
        HttpRequest request = HttpRequest
                .newBuilder(new URL(new URL(loginInstance), LOGIN_ENDPOINT).toURI())
                .POST(BodyPublishers
                        .ofString(SerializationUtils.toJson(Map.of(
                                        "username", username,
                                        "password", password
                                )
                        ))
                )
                .header(ACCEPT_HEADER, CONTENT_TYPE_APPLICATION_JSON)
                .header(CONTENT_TYPE_HEADER, CONTENT_TYPE_APPLICATION_JSON)
                .build();

        HttpResponse<String> data = HTTP_CLIENT.send(request, BodyHandlers.ofString());
        if (data.statusCode() != 200) {
            throw new IllegalStateException(
                    String.format("Failed to login. Status code: %s Body: %s", data.statusCode(),
                            data.body()));
        }

        AuthorizationResponse authorizationResponse = SerializationUtils
                .fromJson(data.body(), new AuthorizationResponse());

        refreshToken = authorizationResponse.getRefreshToken();
        String accessToken = authorizationResponse.accessToken;

        if (accessToken == null) {
            accessToken = getAccessToken(loginInstance, refreshToken);
        }

        initializeClients(instance, accessToken);
    }

    public synchronized String getAccessToken(String loginInstance, String refreshToken)
            throws IOException, InterruptedException, URISyntaxException {
        if (accessToken == null || System.currentTimeMillis() > accessTokenExpirationTime
                - 5 * 60 * 1000) { // Renew token 5 minutes before expiration
            HttpRequest request = HttpRequest
                    .newBuilder(new URL(new URL(loginInstance), AUTHORIZE_ENDPOINT).toURI())
                    .POST(BodyPublishers
                            .ofString(getUrlEncoddedBody(Map.of("refresh_token", refreshToken))))
                    .header(ACCEPT_HEADER, CONTENT_TYPE_APPLICATION_JSON)
                    .header(CONTENT_TYPE_HEADER, CONTENT_TYPE_X_WWW_FORM_URLENCODED)
                    .build();

            HttpResponse<String> data = HTTP_CLIENT.send(request, BodyHandlers.ofString());
            if (data.statusCode() != 200) {
                throw new IllegalStateException(
                        String.format("Failed to login. Status code: %s Body: %s",
                                data.statusCode(),
                                data.body()));
            }

            AuthorizationResponse authorizationResponse = SerializationUtils
                    .fromJson(data.body(), new AuthorizationResponse());
            accessToken = authorizationResponse.accessToken;
            accessTokenExpirationTime =
                    System.currentTimeMillis() + authorizationResponse.expiresIn * 1000L;
        }
        return accessToken;
    }

    public void initializeClients(String instance, String accessToken) {
        codestream = new CodestreamClient(instance, accessToken);
        cloudAssembly = new CloudAssemblyClient(instance, accessToken);
        serviceBroker = new ServiceBrokerClient(instance, accessToken);
    }

    public static String getUrlEncoddedBody(Map<String, String> parameters) {
        return parameters.keySet().stream()
                .map(key -> key + "=" + URLEncoder
                        .encode(parameters.get(key), StandardCharsets.UTF_8))
                .collect(Collectors.joining("&"));
    }

    @Data
    public static class AuthorizationResponse {
        @JsonProperty("access_token")
        private String accessToken;

        @JsonProperty("refresh_token")
        private String refreshToken;

        @JsonProperty("expires_in")
        private int expiresIn;
    }

    public static String getAuthorizationHeaderValue(String accessToken) {
        return "Bearer " + accessToken;
    }

    private static final HttpClient initializeHttpClient() {
        try {
            //TODO: This needs to be hardened
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, Utils.trustAllCertsTrustManager(), new SecureRandom());

            return HttpClient.newBuilder()
                    .version(HttpClient.Version.HTTP_1_1)
                    .sslContext(sslContext)
                    .build();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
