/*
 * Copyright 2021-2021 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.vmware.devops.client.cloudassembly.design;

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
import com.vmware.devops.client.cloudassembly.design.stubs.Blueprint;

public class DesignClient {

    public static final String BLUEPRINTS_ENDPOINT = "/blueprint/api/blueprints";

    @Getter
    private String instance;

    @Getter
    private String accessToken;

    public DesignClient(String instance, String accessToken) {
        this.instance = instance;
        this.accessToken = accessToken;
    }

    public Blueprint createBlueprint(Blueprint blueprint)
            throws IOException, InterruptedException, URISyntaxException {
        HttpRequest request = HttpRequest
                .newBuilder(new URL(new URL(instance), BLUEPRINTS_ENDPOINT).toURI())
                .POST(BodyPublishers
                        .ofString(SerializationUtils.toJson(blueprint)))
                .header(Client.ACCEPT_HEADER, Client.CONTENT_TYPE_APPLICATION_JSON)
                .header(Client.CONTENT_TYPE_HEADER, Client.CONTENT_TYPE_APPLICATION_JSON)
                .header(Client.AUTHORIZATION_HEADER,
                        Client.getAuthorizationHeaderValue(accessToken))
                .build();

        HttpResponse<String> data = Client.HTTP_CLIENT.send(request, BodyHandlers.ofString());
        if (data.statusCode() != 201) {
            throw new IllegalStateException(
                    String.format("Failed to create blueprint. Status code: %s Body: %s",
                            data.statusCode(),
                            data.body()));
        }

        return SerializationUtils.fromJson(data.body(), new Blueprint());
    }

    public Blueprint getBlueprint(String id)
            throws IOException, InterruptedException, URISyntaxException {
        HttpRequest request = HttpRequest
                .newBuilder(new URL(new URL(instance), BLUEPRINTS_ENDPOINT + "/" + id).toURI())
                .GET()
                .header(Client.ACCEPT_HEADER, Client.CONTENT_TYPE_APPLICATION_JSON)
                .header(Client.AUTHORIZATION_HEADER,
                        Client.getAuthorizationHeaderValue(accessToken))
                .build();

        HttpResponse<String> data = Client.HTTP_CLIENT.send(request, BodyHandlers.ofString());
        if (data.statusCode() != 200) {
            throw new IllegalStateException(
                    String.format("Failed to create blueprint. Status code: %s Body: %s",
                            data.statusCode(),
                            data.body()));
        }

        return SerializationUtils.fromJson(data.body(), new Blueprint());
    }

    public List<Blueprint> getAllBlueprints() throws URISyntaxException, IOException, InterruptedException {
        // TODO current implementation gets the the maximum allowed documents for one request - 2000
        // If there are more than 2000 documents, proper pagination must be implemented
        String queryParams = "?size=2000";
        // expand all fields so the BP content is also included in the response
        queryParams += "&$select=*";
        HttpRequest request = HttpRequest
                .newBuilder(new URL(new URL(instance), BLUEPRINTS_ENDPOINT + queryParams).toURI())
                .GET()
                .header(Client.ACCEPT_HEADER, Client.CONTENT_TYPE_APPLICATION_JSON)
                .header(Client.AUTHORIZATION_HEADER,
                        Client.getAuthorizationHeaderValue(accessToken))
                .build();
        HttpResponse<String> data = Client.HTTP_CLIENT.send(request, BodyHandlers.ofString());
        if (data.statusCode() != 200) {
            throw new IllegalStateException(
                    String.format("Failed to fetch all blueprints. Status code: %s Body: %s",
                            data.statusCode(),
                            data.body()));
        }
        return SerializationUtils
                .fromJson(data.body(), new QueryBlueprintsResponse()).getContent();
    }

    public Blueprint updateBlueprint(Blueprint blueprint)
            throws IOException, InterruptedException, URISyntaxException {
        HttpRequest request = HttpRequest
                .newBuilder(
                        new URL(new URL(instance), BLUEPRINTS_ENDPOINT + "/" + blueprint.getId())
                                .toURI())
                .PUT(BodyPublishers
                        .ofString(SerializationUtils.toJson(blueprint)))
                .header(Client.ACCEPT_HEADER, Client.CONTENT_TYPE_APPLICATION_JSON)
                .header(Client.CONTENT_TYPE_HEADER, Client.CONTENT_TYPE_APPLICATION_JSON)
                .header(Client.AUTHORIZATION_HEADER,
                        Client.getAuthorizationHeaderValue(accessToken))
                .build();

        HttpResponse<String> data = Client.HTTP_CLIENT.send(request, BodyHandlers.ofString());
        if (data.statusCode() != 200) {
            throw new IllegalStateException(
                    String.format("Failed to update blueprint. Status code: %s Body: %s",
                            data.statusCode(),
                            data.body()));
        }

        return SerializationUtils.fromJson(data.body(), new Blueprint());
    }

    public void deleteBlueprint(String id)
            throws IOException, InterruptedException, URISyntaxException {
        HttpRequest request = HttpRequest
                .newBuilder(new URL(new URL(instance), BLUEPRINTS_ENDPOINT + "/" + id).toURI())
                .DELETE()
                .header(Client.AUTHORIZATION_HEADER,
                        Client.getAuthorizationHeaderValue(accessToken))
                .build();

        HttpResponse<String> data = Client.HTTP_CLIENT.send(request, BodyHandlers.ofString());
        if (data.statusCode() != 204) {
            throw new IllegalStateException(
                    String.format("Failed to delete blueprint. Status code: %s Body: %s",
                            data.statusCode(),
                            data.body()));
        }
    }

    public Blueprint findBlueprintByName(String name)
            throws IOException, InterruptedException, URISyntaxException {
        String queryParams = "?search=" + Utils.urlEncode(name);
        // expand all fields so the BP content is also included in the response
        queryParams += "&$select=*";
        HttpRequest request = HttpRequest
                .newBuilder(
                        new URL(new URL(instance),
                                BLUEPRINTS_ENDPOINT + queryParams).toURI())
                .GET()
                .header(Client.AUTHORIZATION_HEADER,
                        Client.getAuthorizationHeaderValue(accessToken))
                .header(Client.ACCEPT_HEADER, Client.CONTENT_TYPE_APPLICATION_JSON)
                .build();

        HttpResponse<String> data = Client.HTTP_CLIENT.send(request, BodyHandlers.ofString());
        if (data.statusCode() != 200) {
            throw new IllegalStateException(
                    String.format("Failed to find blueprint. Status code: %s Body: %s",
                            data.statusCode(),
                            data.body()));
        }

        /*
         * This doesn't handle pagination, but if you have more than 1 page matches here,
         * the name pattern is probably not specific enough
         */
        List<Blueprint> content = SerializationUtils
                .fromJson(data.body(), new QueryBlueprintsResponse()).getContent();
        for (Blueprint b : content) {
            if (b.getName().equals(name)) {
                return b;
            }
        }

        return null;
    }

    public Blueprint createOrUpdateBlueprint(Blueprint blueprint)
            throws IOException, InterruptedException, URISyntaxException {
        Blueprint existing = findBlueprintByName(blueprint.getName());
        if (existing != null) {
            blueprint.setId(existing.getId());
            return updateBlueprint(blueprint);
        }

        return createBlueprint(blueprint);
    }

    @Data
    @NoArgsConstructor
    public static class QueryBlueprintsResponse {
        //Extend on demand
        private List<Blueprint> content;
    }
}
