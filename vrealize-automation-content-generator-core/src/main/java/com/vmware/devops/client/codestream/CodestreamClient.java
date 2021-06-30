/*
 * Copyright 2021-2021 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.vmware.devops.client.codestream;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import com.vmware.devops.SerializationUtils;
import com.vmware.devops.Utils;
import com.vmware.devops.client.Client;
import com.vmware.devops.client.codestream.stubs.CloudProxy;
import com.vmware.devops.client.codestream.stubs.Endpoint;
import com.vmware.devops.client.codestream.stubs.EndpointCertificate;
import com.vmware.devops.client.codestream.stubs.GerritListener;
import com.vmware.devops.client.codestream.stubs.GerritTrigger;
import com.vmware.devops.client.codestream.stubs.Pipeline;
import com.vmware.devops.client.codestream.stubs.Variable;

public class CodestreamClient {
    private static final String PIPELINES_ENDPOINT = "codestream/api/pipelines";
    private static final String GERRIT_LISTENERS_ENDPOINT = "codestream/api/gerrit-listeners";
    private static final String GERRIT_TRIGGERS_ENDPOINT = "codestream/api/gerrit-triggers";
    private static final String VARIABLES_ENDPOINT = "codestream/api/variables";
    private static final String ENDPOINTS_ENDPOINT = "codestream/api/endpoints";
    private static final String ENDPOINT_CERTIFICATE_ENDPOINT = "codestream/api/endpoint-certificate";
    private static final String CLOUD_PROXY_ENDPOINT = "/codestream/api/cloud-proxy";

    @Getter
    private String instance;

    @Getter
    private String accessToken;

    public CodestreamClient(String instance, String accessToken) {
        this.instance = instance;
        this.accessToken = accessToken;
    }

    public Pipeline createPipeline(Pipeline pipeline)
            throws IOException, InterruptedException, URISyntaxException {
        HttpRequest request = HttpRequest
                .newBuilder(new URL(new URL(instance), PIPELINES_ENDPOINT).toURI())
                .POST(BodyPublishers
                        .ofString(SerializationUtils.toJson(pipeline)))
                .header(Client.ACCEPT_HEADER, Client.CONTENT_TYPE_APPLICATION_JSON)
                .header(Client.CONTENT_TYPE_HEADER, Client.CONTENT_TYPE_APPLICATION_JSON)
                .header(Client.AUTHORIZATION_HEADER,
                        Client.getAuthorizationHeaderValue(accessToken))
                .build();

        HttpResponse<String> data = Client.HTTP_CLIENT.send(request, BodyHandlers.ofString());
        if (data.statusCode() != 200) {
            throw new IllegalStateException(
                    String.format("Failed to create pipeline. Status code: %s Body: %s",
                            data.statusCode(),
                            data.body()));
        }

        boolean enable = pipeline.isEnabled();
        pipeline = SerializationUtils.fromJson(data.body(), new Pipeline());
        if (enable) {
            pipeline.setEnabled(true);
            pipeline = this.updatePipeline(pipeline);
        }

        return pipeline;
    }

    public Pipeline createOrUpdatePipeline(Pipeline pipeline)
            throws IOException, InterruptedException, URISyntaxException {
        Pipeline existing = findPipelineByName(pipeline.getName());
        if (existing != null) {
            pipeline.setId(existing.getId());
            return updatePipeline(pipeline);
        }
        return createPipeline(pipeline);
    }

    public void deletePipeline(String id)
            throws IOException, InterruptedException, URISyntaxException {
        HttpRequest request = HttpRequest
                .newBuilder(new URL(new URL(instance), PIPELINES_ENDPOINT + "/" + id).toURI())
                .DELETE()
                .header(Client.AUTHORIZATION_HEADER,
                        Client.getAuthorizationHeaderValue(accessToken))
                .build();

        HttpResponse<String> data = Client.HTTP_CLIENT.send(request, BodyHandlers.ofString());
        if (data.statusCode() != 200) {
            throw new IllegalStateException(
                    String.format("Failed to delete pipeline. Status code: %s Body: %s",
                            data.statusCode(),
                            data.body()));
        }
    }

    public Pipeline fetchPipeline(String id)
            throws IOException, URISyntaxException, InterruptedException {
        HttpRequest request = HttpRequest
                .newBuilder(new URL(new URL(instance), PIPELINES_ENDPOINT + "/" + id).toURI())
                .GET()
                .header(Client.AUTHORIZATION_HEADER,
                        Client.getAuthorizationHeaderValue(accessToken))
                .build();

        HttpResponse<String> data = Client.HTTP_CLIENT.send(request, BodyHandlers.ofString());
        if (data.statusCode() != 200) {
            throw new IllegalStateException(
                    String.format("Failed to get pipeline. Status code: %s Body: %s",
                            data.statusCode(),
                            data.body()));
        }

        return SerializationUtils.fromJson(data.body(), new Pipeline());
    }

    public Pipeline fetchPipelineByProjectAndPipelineName(String projectName, String pipelineName)
            throws IOException, URISyntaxException, InterruptedException {
        HttpRequest request = HttpRequest
                .newBuilder(new URL(new URL(instance),
                        PIPELINES_ENDPOINT + "/" + Utils.urlEncode(projectName) + "/"
                                + pipelineName).toURI())
                .GET()
                .header(Client.AUTHORIZATION_HEADER,
                        Client.getAuthorizationHeaderValue(accessToken))
                .build();

        HttpResponse<String> data = Client.HTTP_CLIENT.send(request, BodyHandlers.ofString());
        if (data.statusCode() != 200) {
            throw new IllegalStateException(
                    String.format("Failed to get pipeline. Status code: %s Body: %s",
                            data.statusCode(),
                            data.body()));
        }

        return SerializationUtils.fromJson(data.body(), new Pipeline());
    }

    public Pipeline findPipelineByName(String pipelineName)
            throws IOException, URISyntaxException, InterruptedException {
        HttpRequest request = HttpRequest
                .newBuilder(
                        new URL(new URL(instance), PIPELINES_ENDPOINT + "?$filter=" + Utils
                                .urlEncode(String.format("name eq '%s'", pipelineName)))
                                .toURI())
                .GET()
                .header(Client.AUTHORIZATION_HEADER,
                        Client.getAuthorizationHeaderValue(accessToken))
                .build();

        HttpResponse<String> data = Client.HTTP_CLIENT.send(request, BodyHandlers.ofString());
        if (data.statusCode() != 200) {
            throw new IllegalStateException(
                    String.format("Failed to find pipeline. Status code: %s Body: %s",
                            data.statusCode(),
                            data.body()));
        }

        Map<String, Pipeline> documents = SerializationUtils
                .fromJson(data.body(), new CodestreamClient.QueryPipelinesResponse())
                .getDocuments();

        if (documents.size() == 1) {
            return new ArrayList<>(documents.values()).get(0);
        }

        return null;
    }

    public Pipeline updatePipeline(Pipeline pipeline)
            throws IOException, URISyntaxException, InterruptedException {
        HttpRequest request = HttpRequest
                .newBuilder(new URL(new URL(instance), PIPELINES_ENDPOINT + "/" + pipeline.getId())
                        .toURI())
                .PUT(BodyPublishers
                        .ofString(SerializationUtils.toJson(pipeline)))
                .header(Client.ACCEPT_HEADER, Client.CONTENT_TYPE_APPLICATION_JSON)
                .header(Client.CONTENT_TYPE_HEADER, Client.CONTENT_TYPE_APPLICATION_JSON)
                .header(Client.AUTHORIZATION_HEADER,
                        Client.getAuthorizationHeaderValue(accessToken))
                .build();

        HttpResponse<String> data = Client.HTTP_CLIENT.send(request, BodyHandlers.ofString());
        if (data.statusCode() != 200) {
            throw new IllegalStateException(
                    String.format("Failed to update pipeline. Status code: %s Body: %s",
                            data.statusCode(),
                            data.body()));
        }

        return SerializationUtils.fromJson(data.body(), new Pipeline());
    }

    public GerritTrigger createGerritTrigger(GerritTrigger trigger)
            throws IOException, InterruptedException, URISyntaxException {
        HttpRequest request = HttpRequest
                .newBuilder(new URL(new URL(instance), GERRIT_TRIGGERS_ENDPOINT).toURI())
                .POST(BodyPublishers
                        .ofString(SerializationUtils.toJson(trigger)))
                .header(Client.ACCEPT_HEADER, Client.CONTENT_TYPE_APPLICATION_JSON)
                .header(Client.CONTENT_TYPE_HEADER, Client.CONTENT_TYPE_APPLICATION_JSON)
                .header(Client.AUTHORIZATION_HEADER,
                        Client.getAuthorizationHeaderValue(accessToken))
                .build();

        HttpResponse<String> data = Client.HTTP_CLIENT.send(request, BodyHandlers.ofString());
        if (data.statusCode() != 200) {
            throw new IllegalStateException(
                    String.format("Failed to create Gerrit trigger. Status code: %s Body: %s",
                            data.statusCode(),
                            data.body()));
        }

        boolean enable = trigger.isEnabled();
        trigger = SerializationUtils.fromJson(data.body(), new GerritTrigger());
        if (enable) {
            trigger.setEnabled(true);
            trigger = this.updateGerritTrigger(trigger);
        }

        return trigger;
    }

    public void deleteGerritTrigger(String id)
            throws IOException, InterruptedException, URISyntaxException {
        HttpRequest request = HttpRequest
                .newBuilder(new URL(new URL(instance), GERRIT_TRIGGERS_ENDPOINT + "/" + id).toURI())
                .DELETE()
                .header(Client.AUTHORIZATION_HEADER,
                        Client.getAuthorizationHeaderValue(accessToken))
                .build();

        HttpResponse<String> data = Client.HTTP_CLIENT.send(request, BodyHandlers.ofString());
        if (data.statusCode() != 200) {
            throw new IllegalStateException(
                    String.format("Failed to delete Gerrit trigger. Status code: %s Body: %s",
                            data.statusCode(),
                            data.body()));
        }
    }

    public GerritTrigger fetchGerritTrigger(String id)
            throws IOException, URISyntaxException, InterruptedException {
        HttpRequest request = HttpRequest
                .newBuilder(new URL(new URL(instance), GERRIT_TRIGGERS_ENDPOINT + "/" + id).toURI())
                .GET()
                .header(Client.AUTHORIZATION_HEADER,
                        Client.getAuthorizationHeaderValue(accessToken))
                .build();

        HttpResponse<String> data = Client.HTTP_CLIENT.send(request, BodyHandlers.ofString());
        if (data.statusCode() != 200) {
            throw new IllegalStateException(
                    String.format("Failed to get Gerrit trigger. Status code: %s Body: %s",
                            data.statusCode(),
                            data.body()));
        }

        return SerializationUtils.fromJson(data.body(), new GerritTrigger());
    }

    public GerritTrigger updateGerritTrigger(GerritTrigger gerritTrigger)
            throws IOException, URISyntaxException, InterruptedException {
        HttpRequest request = HttpRequest
                .newBuilder(new URL(new URL(instance),
                        GERRIT_TRIGGERS_ENDPOINT + "/" + gerritTrigger.getId()).toURI())
                .PUT(BodyPublishers
                        .ofString(SerializationUtils.toJson(gerritTrigger)))
                .header(Client.ACCEPT_HEADER, Client.CONTENT_TYPE_APPLICATION_JSON)
                .header(Client.CONTENT_TYPE_HEADER, Client.CONTENT_TYPE_APPLICATION_JSON)
                .header(Client.AUTHORIZATION_HEADER,
                        Client.getAuthorizationHeaderValue(accessToken))
                .build();

        HttpResponse<String> data = Client.HTTP_CLIENT.send(request, BodyHandlers.ofString());
        if (data.statusCode() != 200) {
            throw new IllegalStateException(
                    String.format("Failed to update Gerrit trigger. Status code: %s Body: %s",
                            data.statusCode(),
                            data.body()));
        }

        return SerializationUtils.fromJson(data.body(), new GerritTrigger());
    }

    public GerritTrigger findGerritTriggerrByName(String name)
            throws IOException, URISyntaxException, InterruptedException {
        HttpRequest request = HttpRequest
                .newBuilder(
                        new URL(new URL(instance), GERRIT_TRIGGERS_ENDPOINT + "?$filter=" + Utils
                                .urlEncode(String.format("name eq '%s'", name)))
                                .toURI())
                .GET()
                .header(Client.AUTHORIZATION_HEADER,
                        Client.getAuthorizationHeaderValue(accessToken))
                .build();

        HttpResponse<String> data = Client.HTTP_CLIENT.send(request, BodyHandlers.ofString());
        if (data.statusCode() != 200) {
            throw new IllegalStateException(
                    String.format(
                            "Failed to find gerrit trigger with name: %s. Status code: %s Body: %s",
                            name,
                            data.statusCode(),
                            data.body()));
        }

        Map<String, GerritTrigger> documents = SerializationUtils
                .fromJson(data.body(), new CodestreamClient.QueryTriggersResponse())
                .getDocuments();

        if (documents.size() == 1) {
            return new ArrayList<>(documents.values()).get(0);
        }

        return null;
    }

    public GerritTrigger createOrUpdateGerritTrigger(GerritTrigger trigger)
            throws IOException, InterruptedException, URISyntaxException {
        GerritTrigger existing = findGerritTriggerrByName(trigger.getName());
        if (existing != null) {
            trigger.setId(existing.getId());
            return updateGerritTrigger(trigger);
        }
        return createGerritTrigger(trigger);
    }

    public List<GerritTrigger> getAllGerritTriggers()
            throws IOException, URISyntaxException, InterruptedException {
        // TODO current implementation gets the the maximum allowed documents for one request - 2000
        // If there are more than 2000 documents, proper pagination must be implemented
        String queryParams = "?count=2000";
        HttpRequest request = HttpRequest
                .newBuilder(
                        new URL(new URL(instance), GERRIT_TRIGGERS_ENDPOINT + queryParams)
                                .toURI())
                .GET()
                .header(Client.AUTHORIZATION_HEADER,
                        Client.getAuthorizationHeaderValue(accessToken))
                .build();

        HttpResponse<String> data = Client.HTTP_CLIENT.send(request, BodyHandlers.ofString());
        if (data.statusCode() != 200) {
            throw new IllegalStateException(
                    String.format(
                            "Failed to fetch all gerrit triggers. Status code: %s Body: %s",
                            data.statusCode(),
                            data.body()));
        }

        return new ArrayList<>(SerializationUtils
                .fromJson(data.body(), new CodestreamClient.QueryTriggersResponse())
                .getDocuments().values());
    }

    public Variable createVariable(Variable variable)
            throws IOException, InterruptedException, URISyntaxException {
        HttpRequest request = HttpRequest
                .newBuilder(new URL(new URL(instance), VARIABLES_ENDPOINT).toURI())
                .POST(BodyPublishers
                        .ofString(SerializationUtils.toJson(variable)))
                .header(Client.ACCEPT_HEADER, Client.CONTENT_TYPE_APPLICATION_JSON)
                .header(Client.CONTENT_TYPE_HEADER, Client.CONTENT_TYPE_APPLICATION_JSON)
                .header(Client.AUTHORIZATION_HEADER,
                        Client.getAuthorizationHeaderValue(accessToken))
                .build();

        HttpResponse<String> data = Client.HTTP_CLIENT.send(request, BodyHandlers.ofString());
        if (data.statusCode() != 200) {
            throw new IllegalStateException(
                    String.format("Failed to create variable. Status code: %s Body: %s",
                            data.statusCode(),
                            data.body()));
        }

        return SerializationUtils.fromJson(data.body(), new Variable());
    }

    public Variable createOrUpdateVariable(Variable variable)
            throws IOException, InterruptedException, URISyntaxException {
        Variable existing = findVariableByName(variable.getName());
        if (existing != null) {
            variable.setId(existing.getId());
            return updateVariable(variable);
        }
        return createVariable(variable);
    }

    public void deleteVariable(String id)
            throws IOException, InterruptedException, URISyntaxException {
        HttpRequest request = HttpRequest
                .newBuilder(new URL(new URL(instance), VARIABLES_ENDPOINT + "/" + id).toURI())
                .DELETE()
                .header(Client.AUTHORIZATION_HEADER,
                        Client.getAuthorizationHeaderValue(accessToken))
                .build();

        HttpResponse<String> data = Client.HTTP_CLIENT.send(request, BodyHandlers.ofString());
        if (data.statusCode() != 200) {
            throw new IllegalStateException(
                    String.format("Failed to delete pipeline. Status code: %s Body: %s",
                            data.statusCode(),
                            data.body()));
        }
    }

    public Variable updateVariable(Variable variable)
            throws IOException, URISyntaxException, InterruptedException {
        HttpRequest request = HttpRequest
                .newBuilder(new URL(new URL(instance), VARIABLES_ENDPOINT + "/" + variable.getId())
                        .toURI())
                .PUT(BodyPublishers
                        .ofString(SerializationUtils.toJson(variable)))
                .header(Client.ACCEPT_HEADER, Client.CONTENT_TYPE_APPLICATION_JSON)
                .header(Client.CONTENT_TYPE_HEADER, Client.CONTENT_TYPE_APPLICATION_JSON)
                .header(Client.AUTHORIZATION_HEADER,
                        Client.getAuthorizationHeaderValue(accessToken))
                .build();

        HttpResponse<String> data = Client.HTTP_CLIENT.send(request, BodyHandlers.ofString());
        if (data.statusCode() != 200) {
            throw new IllegalStateException(
                    String.format("Failed to update variable: %s. Status code: %s Body: %s",
                            variable.getName(),
                            data.statusCode(),
                            data.body()));
        }

        return SerializationUtils.fromJson(data.body(), new Variable());
    }

    public Variable findVariableByName(String name)
            throws IOException, URISyntaxException, InterruptedException {
        HttpRequest request = HttpRequest
                .newBuilder(
                        new URL(new URL(instance), VARIABLES_ENDPOINT + "?$filter=" + Utils
                                .urlEncode(String.format("name eq '%s'", name)))
                                .toURI())
                .GET()
                .header(Client.AUTHORIZATION_HEADER,
                        Client.getAuthorizationHeaderValue(accessToken))
                .build();

        HttpResponse<String> data = Client.HTTP_CLIENT.send(request, BodyHandlers.ofString());
        if (data.statusCode() != 200) {
            throw new IllegalStateException(
                    String.format("Failed to find variable with name: %s. Status code: %s Body: %s",
                            name,
                            data.statusCode(),
                            data.body()));
        }

        Map<String, Variable> documents = SerializationUtils
                .fromJson(data.body(), new CodestreamClient.QueryVariablesResponse())
                .getDocuments();

        if (documents.size() == 1) {
            return new ArrayList<>(documents.values()).get(0);
        }

        return null;
    }

    public List<Variable> getAllVariables()
            throws IOException, URISyntaxException, InterruptedException {
        // TODO current implementation gets the the maximum allowed documents for one request - 2000
        // If there are more than 2000 documents, proper pagination must be implemented
        String queryParams = "?count=2000";
        HttpRequest request = HttpRequest
                .newBuilder(
                        new URL(new URL(instance), VARIABLES_ENDPOINT + queryParams)
                                .toURI())
                .GET()
                .header(Client.AUTHORIZATION_HEADER,
                        Client.getAuthorizationHeaderValue(accessToken))
                .build();

        HttpResponse<String> data = Client.HTTP_CLIENT.send(request, BodyHandlers.ofString());
        if (data.statusCode() != 200) {
            throw new IllegalStateException(
                    String.format("Failed to fetch all variables. Status code: %s Body: %s",
                            data.statusCode(),
                            data.body()));
        }

        return new ArrayList<>(SerializationUtils
                .fromJson(data.body(), new CodestreamClient.QueryVariablesResponse())
                .getDocuments().values());
    }

    public GerritListener createGerritListener(GerritListener listener)
            throws IOException, InterruptedException, URISyntaxException {
        HttpRequest request = HttpRequest
                .newBuilder(new URL(new URL(instance), GERRIT_LISTENERS_ENDPOINT).toURI())
                .POST(BodyPublishers
                        .ofString(SerializationUtils.toJson(listener)))
                .header(Client.ACCEPT_HEADER, Client.CONTENT_TYPE_APPLICATION_JSON)
                .header(Client.CONTENT_TYPE_HEADER, Client.CONTENT_TYPE_APPLICATION_JSON)
                .header(Client.AUTHORIZATION_HEADER,
                        Client.getAuthorizationHeaderValue(accessToken))
                .build();

        HttpResponse<String> data = Client.HTTP_CLIENT.send(request, BodyHandlers.ofString());
        if (data.statusCode() != 200) {
            throw new IllegalStateException(
                    String.format("Failed to create gerrit listener. Status code: %s Body: %s",
                            data.statusCode(),
                            data.body()));
        }

        GerritListener result = SerializationUtils.fromJson(data.body(), new GerritListener());

        if (listener.isConnected()) {
            result = connectGerritListener(result.getId(), true);
        }

        return result;
    }

    public void deleteGerritListener(String id)
            throws IOException, InterruptedException, URISyntaxException {
        HttpRequest request = HttpRequest
                .newBuilder(
                        new URL(new URL(instance), GERRIT_LISTENERS_ENDPOINT + "/" + id).toURI())
                .DELETE()
                .header(Client.AUTHORIZATION_HEADER,
                        Client.getAuthorizationHeaderValue(accessToken))
                .build();

        HttpResponse<String> data = Client.HTTP_CLIENT.send(request, BodyHandlers.ofString());
        if (data.statusCode() != 200) {
            throw new IllegalStateException(
                    String.format("Failed to delete gerrit listener. Status code: %s Body: %s",
                            data.statusCode(),
                            data.body()));
        }
    }

    public GerritListener updateGerritListener(GerritListener listener)
            throws IOException, URISyntaxException, InterruptedException {
        HttpRequest request = HttpRequest
                .newBuilder(new URL(new URL(instance),
                        GERRIT_LISTENERS_ENDPOINT + "/" + listener.getId())
                        .toURI())
                .PUT(BodyPublishers
                        .ofString(SerializationUtils.toJson(listener)))
                .header(Client.ACCEPT_HEADER, Client.CONTENT_TYPE_APPLICATION_JSON)
                .header(Client.CONTENT_TYPE_HEADER, Client.CONTENT_TYPE_APPLICATION_JSON)
                .header(Client.AUTHORIZATION_HEADER,
                        Client.getAuthorizationHeaderValue(accessToken))
                .build();

        HttpResponse<String> data = Client.HTTP_CLIENT.send(request, BodyHandlers.ofString());
        if (data.statusCode() != 200) {
            throw new IllegalStateException(
                    String.format("Failed to update gerrit listener: %s. Status code: %s Body: %s",
                            listener.getName(),
                            data.statusCode(),
                            data.body()));
        }

        GerritListener result = SerializationUtils.fromJson(data.body(), new GerritListener());

        if (listener.isConnected()) {
            result = connectGerritListener(result.getId(), true);
        }

        return result;
    }

    public GerritListener connectGerritListener(String id, boolean isConnected)
            throws IOException, URISyntaxException, InterruptedException {
        HttpRequest request = HttpRequest
                .newBuilder(new URL(new URL(instance),
                        GERRIT_LISTENERS_ENDPOINT + "/" + id)
                        .toURI())
                .method("PATCH", BodyPublishers
                        .ofString(SerializationUtils.toJson(GerritListener.builder()
                                .connected(isConnected)
                                .build())
                        )
                )
                .header(Client.ACCEPT_HEADER, Client.CONTENT_TYPE_APPLICATION_JSON)
                .header(Client.CONTENT_TYPE_HEADER, Client.CONTENT_TYPE_APPLICATION_JSON)
                .header(Client.AUTHORIZATION_HEADER,
                        Client.getAuthorizationHeaderValue(accessToken))
                .build();

        HttpResponse<String> data = Client.HTTP_CLIENT.send(request, BodyHandlers.ofString());
        if (data.statusCode() != 200) {
            throw new IllegalStateException(
                    String.format(
                            "Failed to activate gerrit listener: %s. Status code: %s Body: %s",
                            id,
                            data.statusCode(),
                            data.body()));
        }

        return SerializationUtils.fromJson(data.body(), new GerritListener());
    }

    public GerritListener findGerritListenerByName(String name)
            throws IOException, URISyntaxException, InterruptedException {
        HttpRequest request = HttpRequest
                .newBuilder(
                        new URL(new URL(instance), GERRIT_LISTENERS_ENDPOINT + "?$filter=" + Utils
                                .urlEncode(String.format("name eq '%s'", name)))
                                .toURI())
                .GET()
                .header(Client.AUTHORIZATION_HEADER,
                        Client.getAuthorizationHeaderValue(accessToken))
                .build();

        HttpResponse<String> data = Client.HTTP_CLIENT.send(request, BodyHandlers.ofString());
        if (data.statusCode() != 200) {
            throw new IllegalStateException(
                    String.format(
                            "Failed to find gerrit listener with name: %s. Status code: %s Body: %s",
                            name,
                            data.statusCode(),
                            data.body()));
        }

        Map<String, GerritListener> documents = SerializationUtils
                .fromJson(data.body(), new CodestreamClient.QueryListenersResponse())
                .getDocuments();

        if (documents.size() == 1) {
            return new ArrayList<>(documents.values()).get(0);
        }

        return null;
    }

    public GerritListener createOrUpdateGerritListener(GerritListener listener)
            throws IOException, InterruptedException, URISyntaxException {
        GerritListener existing = findGerritListenerByName(listener.getName());
        if (existing != null) {
            listener.setId(existing.getId());

            if (existing.equals(listener)) {
                return existing;
            } else {
                connectGerritListener(listener.getId(), false);
            }

            return updateGerritListener(listener);
        }
        return createGerritListener(listener);
    }

    public Endpoint createEndpoint(Endpoint endpoint)
            throws IOException, InterruptedException, URISyntaxException {
        HttpRequest request = HttpRequest
                .newBuilder(new URL(new URL(instance), ENDPOINTS_ENDPOINT).toURI())
                .POST(BodyPublishers
                        .ofString(SerializationUtils.toJson(endpoint)))
                .header(Client.ACCEPT_HEADER, Client.CONTENT_TYPE_APPLICATION_JSON)
                .header(Client.CONTENT_TYPE_HEADER, Client.CONTENT_TYPE_APPLICATION_JSON)
                .header(Client.AUTHORIZATION_HEADER,
                        Client.getAuthorizationHeaderValue(accessToken))
                .build();

        HttpResponse<String> data = Client.HTTP_CLIENT.send(request, BodyHandlers.ofString());
        if (data.statusCode() != 200) {
            throw new IllegalStateException(
                    String.format("Failed to create endpoint. Status code: %s Body: %s",
                            data.statusCode(),
                            data.body()));
        }

        return SerializationUtils.fromJson(data.body(), new Endpoint());
    }

    public void deleteEndpoint(String id)
            throws IOException, InterruptedException, URISyntaxException {
        HttpRequest request = HttpRequest
                .newBuilder(new URL(new URL(instance), ENDPOINTS_ENDPOINT + "/" + id).toURI())
                .DELETE()
                .header(Client.AUTHORIZATION_HEADER,
                        Client.getAuthorizationHeaderValue(accessToken))
                .build();

        HttpResponse<String> data = Client.HTTP_CLIENT.send(request, BodyHandlers.ofString());
        if (data.statusCode() != 200) {
            throw new IllegalStateException(
                    String.format("Failed to delete endpoint. Status code: %s Body: %s",
                            data.statusCode(),
                            data.body()));
        }
    }

    public Endpoint findEndpointByName(String name)
            throws IOException, URISyntaxException, InterruptedException {
        HttpRequest request = HttpRequest
                .newBuilder(
                        new URL(new URL(instance), ENDPOINTS_ENDPOINT + "?$filter=" + Utils
                                .urlEncode(String.format("name eq '%s'", name)))
                                .toURI())
                .GET()
                .header(Client.AUTHORIZATION_HEADER,
                        Client.getAuthorizationHeaderValue(accessToken))
                .build();

        HttpResponse<String> data = Client.HTTP_CLIENT.send(request, BodyHandlers.ofString());
        if (data.statusCode() != 200) {
            throw new IllegalStateException(
                    String.format("Failed to find endpoint with name: %s. Status code: %s Body: %s",
                            name,
                            data.statusCode(),
                            data.body()));
        }

        Map<String, Endpoint> documents = SerializationUtils
                .fromJson(data.body(), new CodestreamClient.QueryEndpointsResponse())
                .getDocuments();

        if (documents.size() == 1) {
            return new ArrayList<>(documents.values()).get(0);
        }

        return null;
    }

    public Endpoint updateEndpoint(Endpoint endpoint)
            throws IOException, URISyntaxException, InterruptedException {
        HttpRequest request = HttpRequest
                .newBuilder(new URL(new URL(instance), ENDPOINTS_ENDPOINT + "/" + endpoint.getId())
                        .toURI())
                .PUT(BodyPublishers
                        .ofString(SerializationUtils.toJson(endpoint)))
                .header(Client.ACCEPT_HEADER, Client.CONTENT_TYPE_APPLICATION_JSON)
                .header(Client.CONTENT_TYPE_HEADER, Client.CONTENT_TYPE_APPLICATION_JSON)
                .header(Client.AUTHORIZATION_HEADER,
                        Client.getAuthorizationHeaderValue(accessToken))
                .build();

        HttpResponse<String> data = Client.HTTP_CLIENT.send(request, BodyHandlers.ofString());
        if (data.statusCode() != 200) {
            throw new IllegalStateException(
                    String.format("Failed to update endpoint: %s. Status code: %s Body: %s",
                            endpoint.getName(),
                            data.statusCode(),
                            data.body()));
        }

        return SerializationUtils.fromJson(data.body(), new Endpoint());
    }

    public Endpoint createOrUpdateEndpoint(Endpoint endpoint)
            throws IOException, InterruptedException, URISyntaxException {
        Endpoint existing = findEndpointByName(endpoint.getName());
        if (existing != null) {
            endpoint.setId(existing.getId());
            return updateEndpoint(endpoint);
        }
        return createEndpoint(endpoint);
    }

    public EndpointCertificate getEndpointCertificate(String url, String cloudProxyId)
            throws IOException, InterruptedException, URISyntaxException {
        String requestUrl = ENDPOINT_CERTIFICATE_ENDPOINT + "?url=" + url;
        if (cloudProxyId != null) {
            requestUrl += "&cloudProxyId=" + cloudProxyId;
        }
        HttpRequest request = HttpRequest
                .newBuilder(new URL(new URL(instance), requestUrl).toURI())
                .GET()
                .header(Client.AUTHORIZATION_HEADER,
                        Client.getAuthorizationHeaderValue(accessToken))
                .build();

        HttpResponse<String> data = Client.HTTP_CLIENT.send(request, BodyHandlers.ofString());
        if (data.statusCode() != 200) {
            throw new IllegalStateException(
                    String.format("Failed to get endpoint certificate. Status code: %s Body: %s",
                            data.statusCode(),
                            data.body()));
        }

        return SerializationUtils.fromJson(data.body(), new EndpointCertificate());
    }

    public CloudProxy findCloudProxyByName(String name)
            throws IOException, URISyntaxException, InterruptedException {
        HttpRequest request = HttpRequest
                .newBuilder(
                        new URL(new URL(instance), CLOUD_PROXY_ENDPOINT)
                                .toURI())
                .GET()
                .header(Client.AUTHORIZATION_HEADER,
                        Client.getAuthorizationHeaderValue(accessToken))
                .build();

        HttpResponse<String> data = Client.HTTP_CLIENT.send(request, BodyHandlers.ofString());
        if (data.statusCode() != 200) {
            throw new IllegalStateException(
                    String.format(
                            "Failed to find cloud proxy with name: %s. Status code: %s Body: %s",
                            name,
                            data.statusCode(),
                            data.body()));
        }

        Map<String, CloudProxy> documents = SerializationUtils
                .fromJson(data.body(), new CodestreamClient.QueryCloudProxiesResponse())
                .getDocuments();

        List<CloudProxy> filtered = documents.values().stream()
                .filter(cloudProxy -> getProxyName(cloudProxy).equals(name))
                .collect(Collectors.toList());

        if (filtered.size() == 1) {
            return filtered.get(0);
        }

        return null;
    }

    public static String getProxyName(CloudProxy cloudProxy) {
        if (cloudProxy.getCustomProperties().get(CloudProxy.PROXY_NAME_KEY) != null) {
            return cloudProxy.getCustomProperties().get(CloudProxy.PROXY_NAME_KEY);
        }

        return cloudProxy.getCustomProperties().get(CloudProxy.HOST_NAME_KEY);
    }

    public List<Pipeline> getAllPipelines()
            throws IOException, URISyntaxException, InterruptedException {
        // TODO current implementation gets the the maximum allowed documents for one request - 2000
        // If there are more than 2000 documents, proper pagination must be implemented
        String queryParams = "?count=2000";
        HttpRequest request = HttpRequest
                .newBuilder(new URL(new URL(instance), PIPELINES_ENDPOINT + queryParams).toURI())
                .GET()
                .header(Client.ACCEPT_HEADER, Client.CONTENT_TYPE_APPLICATION_JSON)
                .header(Client.AUTHORIZATION_HEADER,
                        Client.getAuthorizationHeaderValue(accessToken))
                .build();
        HttpResponse<String> data = Client.HTTP_CLIENT.send(request, BodyHandlers.ofString());
        if (data.statusCode() != 200) {
            throw new IllegalStateException(
                    String.format("Failed to fetch all pipelines. Status code: %s Body: %s",
                            data.statusCode(),
                            data.body()));
        }
        return new ArrayList<>(SerializationUtils
                .fromJson(data.body(), new QueryPipelinesResponse()).getDocuments().values());
    }

    public List<CloudProxy> getAllCloudProxies()
            throws IOException, URISyntaxException, InterruptedException {
        // TODO current implementation gets the the maximum allowed documents for one request - 2000
        // If there are more than 2000 documents, proper pagination must be implemented
        String queryParams = "?count=2000";
        HttpRequest request = HttpRequest
                .newBuilder(new URL(new URL(instance), CLOUD_PROXY_ENDPOINT + queryParams).toURI())
                .GET()
                .header(Client.ACCEPT_HEADER, Client.CONTENT_TYPE_APPLICATION_JSON)
                .header(Client.AUTHORIZATION_HEADER,
                        Client.getAuthorizationHeaderValue(accessToken))
                .build();
        HttpResponse<String> data = Client.HTTP_CLIENT.send(request, BodyHandlers.ofString());
        if (data.statusCode() != 200) {
            throw new IllegalStateException(
                    String.format("Failed to fetch all cloud proxies. Status code: %s Body: %s",
                            data.statusCode(),
                            data.body()));
        }
        return new ArrayList<>(SerializationUtils
                .fromJson(data.body(), new QueryCloudProxiesResponse()).getDocuments().values());
    }

    public List<Endpoint> getAllEndpoints()
            throws IOException, URISyntaxException, InterruptedException {
        // TODO current implementation gets the the maximum allowed documents for one request - 2000
        // If there are more than 2000 documents, proper pagination must be implemented
        String queryParams = "?count=2000";
        HttpRequest request = HttpRequest
                .newBuilder(new URL(new URL(instance), ENDPOINTS_ENDPOINT + queryParams).toURI())
                .GET()
                .header(Client.ACCEPT_HEADER, Client.CONTENT_TYPE_APPLICATION_JSON)
                .header(Client.AUTHORIZATION_HEADER,
                        Client.getAuthorizationHeaderValue(accessToken))
                .build();
        HttpResponse<String> data = Client.HTTP_CLIENT.send(request, BodyHandlers.ofString());
        if (data.statusCode() != 200) {
            throw new IllegalStateException(
                    String.format("Failed to fetch all endpoints. Status code: %s Body: %s",
                            data.statusCode(),
                            data.body()));
        }
        return new ArrayList<>(SerializationUtils
                .fromJson(data.body(), new QueryEndpointsResponse()).getDocuments().values());
    }

    @Data
    @NoArgsConstructor
    public static class QueryPipelinesResponse {
        //Extend on demand
        private Map<String, Pipeline> documents;
    }

    @Data
    @NoArgsConstructor
    public static class QueryVariablesResponse {
        //Extend on demand
        private Map<String, Variable> documents;
    }

    @Data
    @NoArgsConstructor
    public static class QueryListenersResponse {
        //Extend on demand
        private Map<String, GerritListener> documents;
    }

    @Data
    @NoArgsConstructor
    public static class QueryTriggersResponse {
        //Extend on demand
        private Map<String, GerritTrigger> documents;
    }

    @Data
    @NoArgsConstructor
    public static class QueryEndpointsResponse {
        //Extend on demand
        private Map<String, Endpoint> documents;
    }

    @Data
    @NoArgsConstructor
    public static class QueryCloudProxiesResponse {
        //Extend on demand
        private Map<String, CloudProxy> documents;
    }
}
