/*
 * Copyright 2021-2021 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.vmware.devops.client.servicebroker;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.List;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import com.vmware.devops.SerializationUtils;
import com.vmware.devops.Utils;
import com.vmware.devops.client.Client;
import com.vmware.devops.client.servicebroker.stubs.Policy;

public class ServiceBrokerClient {
    private static final String POLICIES_ENDPOINT = "policy/api/policies";

    @Getter
    private String instance;

    @Getter
    private String accessToken;

    public ServiceBrokerClient(String instance, String accessToken) {
        this.instance = instance;
        this.accessToken = accessToken;
    }

    public Policy createPolicy(Policy policy)
            throws IOException, InterruptedException, URISyntaxException {
        HttpRequest request = HttpRequest
                .newBuilder(new URL(new URL(instance), POLICIES_ENDPOINT).toURI())
                .POST(BodyPublishers
                        .ofString(SerializationUtils.toJson(policy)))
                .header(Client.ACCEPT_HEADER, Client.CONTENT_TYPE_APPLICATION_JSON)
                .header(Client.CONTENT_TYPE_HEADER, Client.CONTENT_TYPE_APPLICATION_JSON)
                .header(Client.AUTHORIZATION_HEADER,
                        Client.getAuthorizationHeaderValue(accessToken))
                .build();

        HttpResponse<String> data = Client.HTTP_CLIENT.send(request, BodyHandlers.ofString());
        if (data.statusCode() != 201) {
            throw new IllegalStateException(
                    String.format("Failed to create policy. Status code: %s Body: %s",
                            data.statusCode(),
                            data.body()));
        }

        return SerializationUtils.fromJson(data.body(), new Policy());
    }

    public Policy updatePolicy(Policy policy)
            throws IOException, InterruptedException, URISyntaxException {
        HttpRequest request = HttpRequest
                .newBuilder(
                        new URL(new URL(instance), POLICIES_ENDPOINT)
                                .toURI())
                .POST(BodyPublishers
                        .ofString(SerializationUtils.toJson(policy)))
                .header(Client.ACCEPT_HEADER, Client.CONTENT_TYPE_APPLICATION_JSON)
                .header(Client.CONTENT_TYPE_HEADER, Client.CONTENT_TYPE_APPLICATION_JSON)
                .header(Client.AUTHORIZATION_HEADER,
                        Client.getAuthorizationHeaderValue(accessToken))
                .build();

        HttpResponse<String> data = Client.HTTP_CLIENT.send(request, BodyHandlers.ofString());
        if (data.statusCode() != 201) {
            throw new IllegalStateException(
                    String.format("Failed to update policy. Status code: %s Body: %s",
                            data.statusCode(),
                            data.body()));
        }

        return SerializationUtils.fromJson(data.body(), new Policy());
    }

    public Policy createOrUpdatePolicy(Policy policy)
            throws IOException, InterruptedException, URISyntaxException {
        Policy existing = findPolicyByName(policy.getName());
        if (existing != null) {
            policy.setId(existing.getId());
            return updatePolicy(policy);
        }

        return createPolicy(policy);
    }

    public void deletePolicy(String id)
            throws IOException, InterruptedException, URISyntaxException {
        HttpRequest request = HttpRequest
                .newBuilder(new URL(new URL(instance), POLICIES_ENDPOINT + "/" + id).toURI())
                .DELETE()
                .header(Client.AUTHORIZATION_HEADER,
                        Client.getAuthorizationHeaderValue(accessToken))
                .build();

        HttpResponse<String> data = Client.HTTP_CLIENT.send(request, BodyHandlers.ofString());
        if (data.statusCode() != 204) {
            throw new IllegalStateException(
                    String.format("Failed to delete policy. Status code: %s Body: %s",
                            data.statusCode(),
                            data.body()));
        }
    }

    public Policy findPolicyByName(String name)
            throws IOException, InterruptedException, URISyntaxException {
        String queryParams = "?size=200&search=" + Utils.urlEncode(name);
        HttpRequest request = HttpRequest
                .newBuilder(
                        new URL(new URL(instance),
                                POLICIES_ENDPOINT + queryParams).toURI())
                .GET()
                .header(Client.AUTHORIZATION_HEADER,
                        Client.getAuthorizationHeaderValue(accessToken))
                .header(Client.ACCEPT_HEADER, Client.CONTENT_TYPE_APPLICATION_JSON)
                .build();

        HttpResponse<String> data = Client.HTTP_CLIENT.send(request, BodyHandlers.ofString());
        if (data.statusCode() != 200) {
            throw new IllegalStateException(
                    String.format("Failed to find policy. Status code: %s Body: %s",
                            data.statusCode(),
                            data.body()));
        }

        /*
         * This doesn't handle pagination, but if you have more than 1 page matches here,
         * the name pattern is probably not specific enough
         */
        List<Policy> content = SerializationUtils
                .fromJson(data.body(), new QueryPoliciesResponse()).getContent();
        for (Policy p : content) {
            if (p.getName().equals(name)) {
                return p;
            }
        }

        return null;
    }

    public List<Policy> getAllPolicies()
            throws IOException, InterruptedException, URISyntaxException {
        // TODO current implementation gets the the maximum allowed documents for one request - 200
        // If there are more than 200 documents, proper pagination must be implemented
        String queryParams = "?expandDefinition=true&size=200";
        HttpRequest request = HttpRequest
                .newBuilder(new URL(new URL(instance), POLICIES_ENDPOINT + queryParams).toURI())
                .GET()
                .header(Client.ACCEPT_HEADER, Client.CONTENT_TYPE_APPLICATION_JSON)
                .header(Client.AUTHORIZATION_HEADER,
                        Client.getAuthorizationHeaderValue(accessToken))
                .build();
        HttpResponse<String> data = Client.HTTP_CLIENT.send(request, BodyHandlers.ofString());
        if (data.statusCode() != 200) {
            throw new IllegalStateException(
                    String.format("Failed to fetch all policies. Status code: %s Body: %s",
                            data.statusCode(),
                            data.body()));
        }
        return SerializationUtils
                .fromJson(data.body(), new QueryPoliciesResponse()).getContent();
    }

    @Data
    @NoArgsConstructor
    public static class QueryPoliciesResponse {
        //Extend on demand
        private List<Policy> content;
    }
}
