/*
 * Copyright 2021-2021 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.vmware.devops.client.cloudassembly.extensibility;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.List;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import org.apache.commons.io.FileUtils;

import com.vmware.devops.SerializationUtils;
import com.vmware.devops.Utils;
import com.vmware.devops.client.Client;
import com.vmware.devops.client.cloudassembly.extensibility.stubs.Action;
import com.vmware.devops.client.cloudassembly.extensibility.stubs.Subscription;

public class ExtensibilityClient {
    public static final String ACTIONS_ENDPOINT = "/abx/api/resources/actions";
    public static final String EXPORT_ACTION_ENDPOINT = ACTIONS_ENDPOINT + "/export";
    public static final String SUBSRIPTIONS_ENDPOINT = "/event-broker/api/subscriptions";

    @Getter
    private String instance;

    @Getter
    private String accessToken;

    public ExtensibilityClient(String instance, String accessToken) {
        this.instance = instance;
        this.accessToken = accessToken;
    }

    public Action createAction(Action action)
            throws IOException, InterruptedException, URISyntaxException {
        HttpRequest request = HttpRequest
                .newBuilder(new URL(new URL(instance), ACTIONS_ENDPOINT).toURI())
                .POST(BodyPublishers
                        .ofString(SerializationUtils.toJson(action)))
                .header(Client.ACCEPT_HEADER, Client.CONTENT_TYPE_APPLICATION_JSON)
                .header(Client.CONTENT_TYPE_HEADER, Client.CONTENT_TYPE_APPLICATION_JSON)
                .header(Client.AUTHORIZATION_HEADER,
                        Client.getAuthorizationHeaderValue(accessToken))
                .build();

        HttpResponse<String> data = Client.HTTP_CLIENT.send(request, BodyHandlers.ofString());
        if (data.statusCode() != 200) {
            throw new IllegalStateException(
                    String.format("Failed to create action. Status code: %s Body: %s",
                            data.statusCode(),
                            data.body()));
        }

        return SerializationUtils.fromJson(data.body(), new Action());
    }

    public void deleteAction(String selfLink)
            throws IOException, InterruptedException, URISyntaxException {
        HttpRequest request = HttpRequest
                .newBuilder(new URL(new URL(instance), selfLink).toURI())
                .DELETE()
                .header(Client.AUTHORIZATION_HEADER,
                        Client.getAuthorizationHeaderValue(accessToken))
                .build();

        HttpResponse<String> data = Client.HTTP_CLIENT.send(request, BodyHandlers.ofString());
        if (data.statusCode() != 200) {
            throw new IllegalStateException(
                    String.format("Failed to delete action. Status code: %s Body: %s",
                            data.statusCode(),
                            data.body()));
        }
    }

    public Action updateAction(Action action)
            throws IOException, InterruptedException, URISyntaxException {
        HttpRequest request = HttpRequest
                .newBuilder(new URL(new URL(instance), action.getSelfLink()).toURI())
                .PUT(BodyPublishers
                        .ofString(SerializationUtils.toJson(action)))
                .header(Client.ACCEPT_HEADER, Client.CONTENT_TYPE_APPLICATION_JSON)
                .header(Client.CONTENT_TYPE_HEADER, Client.CONTENT_TYPE_APPLICATION_JSON)
                .header(Client.AUTHORIZATION_HEADER,
                        Client.getAuthorizationHeaderValue(accessToken))
                .build();

        HttpResponse<String> data = Client.HTTP_CLIENT.send(request, BodyHandlers.ofString());
        if (data.statusCode() != 200) {
            throw new IllegalStateException(
                    String.format("Failed to update action. Status code: %s Body: %s",
                            data.statusCode(),
                            data.body()));
        }

        return SerializationUtils.fromJson(data.body(), new Action());
    }

    public void exportAction(Action action, File outputFile)
            throws URISyntaxException, IOException, InterruptedException {
        ActionExportRequest exportAcrionRequest = ActionExportRequest.builder()
                .actions(List.of(ActionExportRequestEntry.builder()
                        .id(action.getId())
                        .projectId(action.getProjectId())
                        .build()))
                .build();
        HttpRequest request = HttpRequest
                .newBuilder(new URL(new URL(instance), EXPORT_ACTION_ENDPOINT).toURI())
                .POST(BodyPublishers
                        .ofString(SerializationUtils.toJson(exportAcrionRequest)))
                .header(Client.ACCEPT_HEADER, Client.CONTENT_TYPE_APPLICATION_JSON)
                .header(Client.CONTENT_TYPE_HEADER, Client.CONTENT_TYPE_APPLICATION_JSON)
                .header(Client.AUTHORIZATION_HEADER,
                        Client.getAuthorizationHeaderValue(accessToken))
                .build();
        HttpResponse<byte[]> data = Client.HTTP_CLIENT.send(request, BodyHandlers.ofByteArray());
        FileUtils.writeByteArrayToFile(outputFile, data.body());
    }

    public Action createOrUpdateAction(Action action)
            throws IOException, InterruptedException, URISyntaxException {
        Action existing = findActionByName(action.getName());
        if (existing != null) {
            action.setSelfLink(existing.getSelfLink());
            return updateAction(action);
        }

        return createAction(action);
    }

    public Action findActionByName(String name)
            throws IOException, InterruptedException, URISyntaxException {
        HttpRequest request = HttpRequest
                .newBuilder(
                        new URL(new URL(instance), ACTIONS_ENDPOINT + "?$filter=" +
                                Utils.urlEncode(String.format("name eq '%s'", name)))
                                .toURI())
                .GET()
                .header(Client.AUTHORIZATION_HEADER,
                        Client.getAuthorizationHeaderValue(accessToken))
                .header(Client.ACCEPT_HEADER, Client.CONTENT_TYPE_APPLICATION_JSON)
                .build();

        HttpResponse<String> data = Client.HTTP_CLIENT.send(request, BodyHandlers.ofString());
        if (data.statusCode() != 200) {
            throw new IllegalStateException(
                    String.format("Failed to find action. Status code: %s Body: %s",
                            data.statusCode(),
                            data.body()));
        }

        List<Action> content = SerializationUtils
                .fromJson(data.body(), new QueryActionsResponse()).getContent();

        if (content.size() == 1) {
            return content.get(0);
        }

        return null;
    }

    public List<Action> getAllActions() throws URISyntaxException, IOException, InterruptedException {
        // TODO current implementation gets the the maximum allowed documents for one request - 2000
        // If there are more than 2000 documents, proper pagination must be implemented
        String queryParams = "?size=2000";
        HttpRequest request = HttpRequest
                .newBuilder(new URL(new URL(instance), ACTIONS_ENDPOINT + queryParams).toURI())
                .GET()
                .header(Client.AUTHORIZATION_HEADER,
                        Client.getAuthorizationHeaderValue(accessToken))
                .header(Client.ACCEPT_HEADER, Client.CONTENT_TYPE_APPLICATION_JSON)
                .build();

        HttpResponse<String> data = Client.HTTP_CLIENT.send(request, BodyHandlers.ofString());
        if (data.statusCode() != 200) {
            throw new IllegalStateException(
                    String.format("Failed to fetch all actions. Status code: %s Body: %s",
                            data.statusCode(),
                            data.body()));
        }

        return SerializationUtils
                .fromJson(data.body(), new QueryActionsResponse()).getContent();
    }

    public void createSubscription(Subscription subscription)
            throws IOException, InterruptedException, URISyntaxException {
        HttpRequest request = HttpRequest
                .newBuilder(new URL(new URL(instance), SUBSRIPTIONS_ENDPOINT).toURI())
                .POST(BodyPublishers
                        .ofString(SerializationUtils.toJson(subscription)))
                .header(Client.ACCEPT_HEADER, Client.CONTENT_TYPE_APPLICATION_JSON)
                .header(Client.CONTENT_TYPE_HEADER, Client.CONTENT_TYPE_APPLICATION_JSON)
                .header(Client.AUTHORIZATION_HEADER,
                        Client.getAuthorizationHeaderValue(accessToken))
                .build();

        HttpResponse<String> data = Client.HTTP_CLIENT.send(request, BodyHandlers.ofString());
        if (data.statusCode() != 201) {
            throw new IllegalStateException(
                    String.format(
                            "Failed to create or update subscription. Status code: %s Body: %s",
                            data.statusCode(),
                            data.body()));
        }
    }

    public void deleteSubscription(String id)
            throws IOException, InterruptedException, URISyntaxException {
        HttpRequest request = HttpRequest
                .newBuilder(new URL(new URL(instance), SUBSRIPTIONS_ENDPOINT + "/" + id).toURI())
                .DELETE()
                .header(Client.AUTHORIZATION_HEADER,
                        Client.getAuthorizationHeaderValue(accessToken))
                .build();

        HttpResponse<String> data = Client.HTTP_CLIENT.send(request, BodyHandlers.ofString());
        if (data.statusCode() != 204) {
            throw new IllegalStateException(
                    String.format("Failed to delete action. Status code: %s Body: %s",
                            data.statusCode(),
                            data.body()));
        }
    }

    public Subscription findSubscriptionByName(String name)
            throws IOException, InterruptedException, URISyntaxException {
        HttpRequest request = HttpRequest
                .newBuilder(
                        new URL(new URL(instance), SUBSRIPTIONS_ENDPOINT + "?$filter=" +
                                Utils.urlEncode(String.format("name eq '%s'", name)))
                                .toURI())
                .GET()
                .header(Client.AUTHORIZATION_HEADER,
                        Client.getAuthorizationHeaderValue(accessToken))
                .header(Client.ACCEPT_HEADER, Client.CONTENT_TYPE_APPLICATION_JSON)
                .build();

        HttpResponse<String> data = Client.HTTP_CLIENT.send(request, BodyHandlers.ofString());
        if (data.statusCode() != 200) {
            throw new IllegalStateException(
                    String.format("Failed to find action. Status code: %s Body: %s",
                            data.statusCode(),
                            data.body()));
        }

        List<Subscription> content = SerializationUtils
                .fromJson(data.body(), new QuerySubscriptionResponse()).getContent();

        if (content.size() == 1) {
            return content.get(0);
        }

        return null;
    }

    public List<Subscription> getAllSubscriptions()
            throws IOException, InterruptedException, URISyntaxException {
        // TODO current implementation gets the the maximum allowed documents for one request - 2000
        // If there are more than 2000 documents, proper pagination must be implemented
        String queryParams = "?size=2000";
        queryParams += "&$filter=" + Utils.urlEncode("type ne 'SUBSCRIBABLE'");
        HttpRequest request = HttpRequest
                .newBuilder(new URL(new URL(instance), SUBSRIPTIONS_ENDPOINT + queryParams).toURI())
                .GET()
                .header(Client.AUTHORIZATION_HEADER,
                        Client.getAuthorizationHeaderValue(accessToken))
                .header(Client.ACCEPT_HEADER, Client.CONTENT_TYPE_APPLICATION_JSON)
                .build();

        HttpResponse<String> data = Client.HTTP_CLIENT.send(request, BodyHandlers.ofString());
        if (data.statusCode() != 200) {
            throw new IllegalStateException(
                    String.format("Failed to fetch all subscriptions. Status code: %s Body: %s",
                            data.statusCode(),
                            data.body()));
        }

        return SerializationUtils
                .fromJson(data.body(), new QuerySubscriptionResponse()).getContent();
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    private static class ActionExportRequest {
        private List<ActionExportRequestEntry> actions;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    private static class ActionExportRequestEntry {
        private String id;
        private String projectId;
    }

    public void createOrUpdateSubscription(Subscription subscription)
            throws IOException, InterruptedException, URISyntaxException {
        Subscription existing = findSubscriptionByName(subscription.getName());
        if (existing != null) {
            subscription.setId(existing.getId());
            createSubscription(subscription);
            return;
        }

        subscription.setId("vrealize-automation-content-generator-" + UUID.randomUUID());
        createSubscription(subscription);
    }

    @Data
    @NoArgsConstructor
    public static class QueryActionsResponse {
        //Extend on demand
        private List<Action> content;
    }

    @Data
    @NoArgsConstructor
    public static class QuerySubscriptionResponse {
        //Extend on demand
        private List<Subscription> content;
    }
}
