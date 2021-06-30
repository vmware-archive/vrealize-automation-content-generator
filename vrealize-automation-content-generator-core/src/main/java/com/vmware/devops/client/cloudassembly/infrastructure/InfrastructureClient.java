/*
 * Copyright 2021-2021 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.vmware.devops.client.cloudassembly.infrastructure;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import com.vmware.devops.SerializationUtils;
import com.vmware.devops.Utils;
import com.vmware.devops.client.Client;
import com.vmware.devops.client.cloudassembly.infrastructure.stubs.CloudZone;
import com.vmware.devops.client.cloudassembly.infrastructure.stubs.DataCollector;
import com.vmware.devops.client.cloudassembly.infrastructure.stubs.Endpoint;
import com.vmware.devops.client.cloudassembly.infrastructure.stubs.Endpoint.EndpointType;
import com.vmware.devops.client.cloudassembly.infrastructure.stubs.EndpointRegions;
import com.vmware.devops.client.cloudassembly.infrastructure.stubs.ImageName;
import com.vmware.devops.client.cloudassembly.infrastructure.stubs.InstanceName;
import com.vmware.devops.client.cloudassembly.infrastructure.stubs.Project;
import com.vmware.devops.client.cloudassembly.infrastructure.stubs.ProjectConfig;
import com.vmware.devops.client.cloudassembly.infrastructure.stubs.ProjectPrincipal;
import com.vmware.devops.client.cloudassembly.infrastructure.stubs.Region;
import com.vmware.devops.client.cloudassembly.infrastructure.stubs.RegionInfo;

public class InfrastructureClient {

    public static final String PROJECTS_ENDPOINT = "/project-service/api/projects";
    public static final String ENDPOINT_ENDPOINT = "/provisioning/uerp/provisioning/mgmt/endpoints";
    public static final String ENDPOINT_REGIONS_ENDPOINT = "/provisioning/uerp/provisioning/mgmt/endpoint-regions";
    public static final String CLOUD_ZONE_ENDPOINT = "/provisioning/uerp/provisioning/mgmt/cloud-zone";
    public static final String PROJECT_CONFIG_ENDPOINT = "/provisioning/mgmt/project-config";
    public static final String IMAGE_NAMES_ENDPOINT = "/provisioning/uerp/provisioning/mgmt/image-names";
    public static final String REGION_ENDPOINT = "/provisioning/mgmt/region";
    public static final String REGIONS_INFO_ENDPOINT = "/provisioning/mgmt/regions-info";
    public static final String PROJECTS_PRINCIPALS_ENDPOINT = PROJECTS_ENDPOINT + "/%s/principals";
    public static final String INSTANCE_NAMES_ENDPOINT = "/provisioning/mgmt/instance-names";
    public static final String QUERY_DATA_COLLECTORS = "/query/data-collector-tasks";

    @Getter
    private String instance;

    @Getter
    private String accessToken;

    public InfrastructureClient(String instance, String accessToken) {
        this.instance = instance;
        this.accessToken = accessToken;
    }

    public Project createProject(Project project)
            throws IOException, InterruptedException, URISyntaxException {
        HttpRequest request = HttpRequest
                .newBuilder(new URL(new URL(instance), PROJECTS_ENDPOINT).toURI())
                .POST(BodyPublishers
                        .ofString(SerializationUtils.toJson(project)))
                .header(Client.ACCEPT_HEADER, Client.CONTENT_TYPE_APPLICATION_JSON)
                .header(Client.CONTENT_TYPE_HEADER, Client.CONTENT_TYPE_APPLICATION_JSON)
                .header(Client.AUTHORIZATION_HEADER,
                        Client.getAuthorizationHeaderValue(accessToken))
                .build();

        HttpResponse<String> data = Client.HTTP_CLIENT.send(request, BodyHandlers.ofString());
        if (data.statusCode() != 201) {
            throw new IllegalStateException(
                    String.format("Failed to create project. Status code: %s Body: %s",
                            data.statusCode(),
                            data.body()));
        }

        return SerializationUtils.fromJson(data.body(), new Project());
    }

    public Project updateProject(Project project)
            throws IOException, InterruptedException, URISyntaxException {
        HttpRequest request = HttpRequest
                .newBuilder(new URL(new URL(instance), PROJECTS_ENDPOINT + "/" + project.getId())
                        .toURI())
                .method("PATCH", BodyPublishers
                        .ofString(SerializationUtils.toJson(project)))
                .header(Client.ACCEPT_HEADER, Client.CONTENT_TYPE_APPLICATION_JSON)
                .header(Client.CONTENT_TYPE_HEADER, Client.CONTENT_TYPE_APPLICATION_JSON)
                .header(Client.AUTHORIZATION_HEADER,
                        Client.getAuthorizationHeaderValue(accessToken))
                .build();

        HttpResponse<String> data = Client.HTTP_CLIENT.send(request, BodyHandlers.ofString());
        if (data.statusCode() != 200) {
            throw new IllegalStateException(
                    String.format("Failed to update project. Status code: %s Body: %s",
                            data.statusCode(),
                            data.body()));
        }

        return SerializationUtils.fromJson(data.body(), new Project());
    }

    public Project createOrUpdateProject(Project project)
            throws IOException, InterruptedException, URISyntaxException {
        Project existing = findProjectByName(project.getName());
        if (existing != null) {
            project.setId(existing.getId());
            return updateProject(project);
        }

        return createProject(project);
    }

    public List<Project> getAllProjects()
            throws IOException, InterruptedException, URISyntaxException {
        // TODO current implementation gets the the maximum allowed documents for one request - 2000
        // If there are more than 2000 documents, proper pagination must be implemented
        String queryParams = "?size=2000";
        HttpRequest request = HttpRequest
                .newBuilder(new URL(new URL(instance), PROJECTS_ENDPOINT + queryParams).toURI())
                .GET()
                .header(Client.AUTHORIZATION_HEADER,
                        Client.getAuthorizationHeaderValue(accessToken))
                .header(Client.ACCEPT_HEADER, Client.CONTENT_TYPE_APPLICATION_JSON)
                .build();

        HttpResponse<String> data = Client.HTTP_CLIENT.send(request, BodyHandlers.ofString());
        if (data.statusCode() != 200) {
            throw new IllegalStateException(
                    String.format("Failed to fetch all projects. Status code: %s Body: %s",
                            data.statusCode(),
                            data.body()));
        }
        return SerializationUtils.fromJson(data.body(), new QueryProjectsResponse()).getContent();
    }

    public void deleteProject(String id)
            throws IOException, InterruptedException, URISyntaxException {
        HttpRequest request = HttpRequest
                .newBuilder(new URL(new URL(instance), PROJECTS_ENDPOINT + "/" + id).toURI())
                .DELETE()
                .header(Client.AUTHORIZATION_HEADER,
                        Client.getAuthorizationHeaderValue(accessToken))
                .build();

        HttpResponse<String> data = Client.HTTP_CLIENT.send(request, BodyHandlers.ofString());
        if (data.statusCode() != 200) {
            throw new IllegalStateException(
                    String.format("Failed to delete project. Status code: %s Body: %s",
                            data.statusCode(),
                            data.body()));
        }
    }

    public Project findProjectByName(String name)
            throws IOException, InterruptedException, URISyntaxException {
        HttpRequest request = HttpRequest
                .newBuilder(
                        new URL(new URL(instance), PROJECTS_ENDPOINT + "?$filter=" +
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
                    String.format("Failed to find project. Status code: %s Body: %s",
                            data.statusCode(),
                            data.body()));
        }

        List<Project> content = SerializationUtils
                .fromJson(data.body(), new QueryProjectsResponse()).getContent();

        if (content.size() == 1) {
            return content.get(0);
        }

        return null;
    }

    public Endpoint createEndpoint(Endpoint endpoint)
            throws IOException, InterruptedException, URISyntaxException {
        HttpRequest request = HttpRequest
                .newBuilder(new URL(new URL(instance), ENDPOINT_ENDPOINT + "?validate").toURI())
                .PUT(BodyPublishers
                        .ofString(SerializationUtils.toJson(endpoint)))
                .header(Client.ACCEPT_HEADER, Client.CONTENT_TYPE_APPLICATION_JSON)
                .header(Client.CONTENT_TYPE_HEADER, Client.CONTENT_TYPE_APPLICATION_JSON)
                .header(Client.AUTHORIZATION_HEADER,
                        Client.getAuthorizationHeaderValue(accessToken))
                .build();

        HttpResponse<String> data = Client.HTTP_CLIENT.send(request, BodyHandlers.ofString());
        if (data.statusCode() != 204) {
            throw new IllegalStateException(
                    String.format("Failed to validate endpoint. Status code: %s Body: %s",
                            data.statusCode(),
                            data.body()));
        }

        request = HttpRequest
                .newBuilder(new URL(new URL(instance), ENDPOINT_ENDPOINT).toURI())
                .POST(BodyPublishers
                        .ofString(SerializationUtils.toJson(endpoint)))
                .header(Client.ACCEPT_HEADER, Client.CONTENT_TYPE_APPLICATION_JSON)
                .header(Client.CONTENT_TYPE_HEADER, Client.CONTENT_TYPE_APPLICATION_JSON)
                .header(Client.AUTHORIZATION_HEADER,
                        Client.getAuthorizationHeaderValue(accessToken))
                .build();

        data = Client.HTTP_CLIENT.send(request, BodyHandlers.ofString());
        if (data.statusCode() != 200) {
            throw new IllegalStateException(
                    String.format("Failed to create endpoint. Status code: %s Body: %s",
                            data.statusCode(),
                            data.body()));
        }

        return SerializationUtils.fromJson(data.body(), new Endpoint());
    }

    public Endpoint updateEndpoint(Endpoint endpoint)
            throws IOException, InterruptedException, URISyntaxException {
        HttpRequest request = HttpRequest
                .newBuilder(new URL(new URL(instance),
                        ENDPOINT_ENDPOINT + endpoint.getDocumentSelfLink()).toURI())
                .PUT(BodyPublishers
                        .ofString(SerializationUtils.toJson(endpoint)))
                .header(Client.ACCEPT_HEADER, Client.CONTENT_TYPE_APPLICATION_JSON)
                .header(Client.CONTENT_TYPE_HEADER, Client.CONTENT_TYPE_APPLICATION_JSON)
                .header(Client.AUTHORIZATION_HEADER,
                        Client.getAuthorizationHeaderValue(accessToken))
                .build();

        HttpResponse<String> data = Client.HTTP_CLIENT.send(request, BodyHandlers.ofString());
        if (data.statusCode() != 204) {
            throw new IllegalStateException(
                    String.format("Failed to update endpoint. Status code: %s Body: %s",
                            data.statusCode(),
                            data.body()));
        }

        return endpoint;
    }

    public Endpoint createOrUpdateEndpoint(Endpoint endpoint)
            throws IOException, InterruptedException, URISyntaxException {
        Endpoint existing = findEndpointByName(endpoint.getName());
        if (existing != null) {
            endpoint.setDocumentSelfLink(existing.getDocumentSelfLink());
            return updateEndpoint(endpoint);
        }

        return createEndpoint(endpoint);
    }

    public void deleteEndpoint(String documentSelfLink)
            throws IOException, InterruptedException, URISyntaxException {
        HttpRequest request = HttpRequest
                .newBuilder(
                        new URL(new URL(instance), ENDPOINT_ENDPOINT + documentSelfLink)
                                .toURI())
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

    public EndpointRegions updateEndpointRegions(EndpointRegions endpointRegions)
            throws IOException, InterruptedException, URISyntaxException {
        HttpRequest request = HttpRequest
                .newBuilder(new URL(new URL(instance), ENDPOINT_REGIONS_ENDPOINT).toURI())
                .POST(BodyPublishers
                        .ofString(SerializationUtils.toJson(endpointRegions)))
                .header(Client.ACCEPT_HEADER, Client.CONTENT_TYPE_APPLICATION_JSON)
                .header(Client.CONTENT_TYPE_HEADER, Client.CONTENT_TYPE_APPLICATION_JSON)
                .header(Client.AUTHORIZATION_HEADER,
                        Client.getAuthorizationHeaderValue(accessToken))
                .build();

        HttpResponse<String> data = Client.HTTP_CLIENT.send(request, BodyHandlers.ofString());
        if (data.statusCode() != 200) {
            throw new IllegalStateException(
                    String.format("Failed to create endpoint regions. Status code: %s Body: %s",
                            data.statusCode(),
                            data.body()));
        }

        return SerializationUtils.fromJson(data.body(), new EndpointRegions());
    }

    public Endpoint findEndpointByName(String name)
            throws IOException, InterruptedException, URISyntaxException {
        HttpRequest request = HttpRequest
                .newBuilder(
                        new URL(new URL(instance), ENDPOINT_ENDPOINT + "?expand=&$filter=" +
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
                    String.format("Failed to find endpoint. Status code: %s Body: %s",
                            data.statusCode(),
                            data.body()));
        }

        Map<String, Endpoint> documents = SerializationUtils
                .fromJson(data.body(), new QueryEndpointsResponse()).getDocuments();

        if (documents.size() == 1) {
            return new ArrayList<>(documents.values()).get(0);
        }

        return null;
    }

    public List<Endpoint> getAllEndpoints()
            throws IOException, InterruptedException, URISyntaxException {
        // TODO current implementation gets 2000 documents
        // If there are more than 2000 documents, proper pagination must be implemented
        String queryParams = "?$limit=2000";
        queryParams += "&expand=";
        // Filter only the endpoint types which we support
        String filter = Arrays.asList(EndpointType.values()).stream()
                .map(type -> String.format("endpointType eq '%s'", type.getValue()))
                .collect(Collectors.joining(" or "));
        queryParams += "&$filter=" + Utils.urlEncode(filter);
        HttpRequest request = HttpRequest
                .newBuilder(new URL(new URL(instance), ENDPOINT_ENDPOINT + queryParams).toURI())
                .GET()
                .header(Client.AUTHORIZATION_HEADER,
                        Client.getAuthorizationHeaderValue(accessToken))
                .header(Client.ACCEPT_HEADER, Client.CONTENT_TYPE_APPLICATION_JSON)
                .build();

        HttpResponse<String> data = Client.HTTP_CLIENT.send(request, BodyHandlers.ofString());
        if (data.statusCode() != 200) {
            throw new IllegalStateException(
                    String.format("Failed to find endpoint. Status code: %s Body: %s",
                            data.statusCode(),
                            data.body()));
        }
        return new ArrayList<>(SerializationUtils
                .fromJson(data.body(), new QueryEndpointsResponse()).getDocuments().values());
    }

    public CloudZone findCloudZoneByName(String name)
            throws IOException, InterruptedException, URISyntaxException {
        HttpRequest request = HttpRequest
                .newBuilder(
                        new URL(new URL(instance), CLOUD_ZONE_ENDPOINT + "?expand=&$filter=" +
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
                    String.format("Failed to find cloud zone. Status code: %s Body: %s",
                            data.statusCode(),
                            data.body()));
        }

        Map<String, CloudZone> documents = SerializationUtils
                .fromJson(data.body(), new QueryCloudZonesResponse()).getDocuments();

        if (documents.size() == 1) {
            return new ArrayList<>(documents.values()).get(0);
        }

        return null;
    }

    public List<CloudZone> getAllCloudZones()
            throws IOException, InterruptedException, URISyntaxException {
        // TODO current implementation gets 2000 documents
        // If there are more than 2000 documents, proper pagination must be implemented
        // Filter only the zones for endpoint types which we support
        String queryParams = "?$limit=2000";
        queryParams += "&expand=";
        String filter = Arrays.asList(EndpointType.values()).stream()
                .map(type -> String.format("endpoints.item.endpointType eq '%s'", type.getValue()))
                .collect(Collectors.joining(" or "));
        queryParams += "&$filter=" + Utils.urlEncode(filter);
        HttpRequest request = HttpRequest
                .newBuilder(new URL(new URL(instance), CLOUD_ZONE_ENDPOINT + queryParams).toURI())
                .GET()
                .header(Client.AUTHORIZATION_HEADER,
                        Client.getAuthorizationHeaderValue(accessToken))
                .header(Client.ACCEPT_HEADER, Client.CONTENT_TYPE_APPLICATION_JSON)
                .build();

        HttpResponse<String> data = Client.HTTP_CLIENT.send(request, BodyHandlers.ofString());
        if (data.statusCode() != 200) {
            throw new IllegalStateException(
                    String.format("Failed to fetch all cloud zones. Status code: %s Body: %s",
                            data.statusCode(),
                            data.body()));
        }
        return new ArrayList<>(SerializationUtils
                .fromJson(data.body(), new QueryCloudZonesResponse()).getDocuments().values());
    }

    public ProjectConfig updateProjectConfig(ProjectConfig projectConfig)
            throws IOException, InterruptedException, URISyntaxException {
        HttpRequest request = HttpRequest
                .newBuilder(new URL(new URL(instance), PROJECT_CONFIG_ENDPOINT).toURI())
                .method("PATCH", BodyPublishers
                        .ofString(SerializationUtils.toJson(projectConfig)))
                .header(Client.ACCEPT_HEADER, Client.CONTENT_TYPE_APPLICATION_JSON)
                .header(Client.CONTENT_TYPE_HEADER, Client.CONTENT_TYPE_APPLICATION_JSON)
                .header(Client.AUTHORIZATION_HEADER,
                        Client.getAuthorizationHeaderValue(accessToken))
                .build();

        HttpResponse<String> data = Client.HTTP_CLIENT.send(request, BodyHandlers.ofString());
        if (data.statusCode() != 200) {
            throw new IllegalStateException(
                    String.format("Failed to update project config. Status code: %s Body: %s",
                            data.statusCode(),
                            data.body()));
        }

        return SerializationUtils.fromJson(data.body(), new ProjectConfig());
    }

    public ImageName createImageName(ImageName imageName)
            throws IOException, InterruptedException, URISyntaxException {
        HttpRequest request = HttpRequest
                .newBuilder(new URL(new URL(instance), IMAGE_NAMES_ENDPOINT).toURI())
                .POST(BodyPublishers
                        .ofString(SerializationUtils.toJson(imageName)))
                .header(Client.ACCEPT_HEADER, Client.CONTENT_TYPE_APPLICATION_JSON)
                .header(Client.CONTENT_TYPE_HEADER, Client.CONTENT_TYPE_APPLICATION_JSON)
                .header(Client.AUTHORIZATION_HEADER,
                        Client.getAuthorizationHeaderValue(accessToken))
                .build();

        HttpResponse<String> data = Client.HTTP_CLIENT.send(request, BodyHandlers.ofString());
        if (data.statusCode() != 200) {
            throw new IllegalStateException(
                    String.format("Failed to create image name. Status code: %s Body: %s",
                            data.statusCode(),
                            data.body()));
        }

        return SerializationUtils.fromJson(data.body(), new ImageName());
    }

    public ImageName updateImageName(ImageName imageName)
            throws IOException, InterruptedException, URISyntaxException {
        HttpRequest request = HttpRequest
                .newBuilder(new URL(new URL(instance), IMAGE_NAMES_ENDPOINT).toURI())
                .PUT(BodyPublishers
                        .ofString(SerializationUtils.toJson(imageName)))
                .header(Client.ACCEPT_HEADER, Client.CONTENT_TYPE_APPLICATION_JSON)
                .header(Client.CONTENT_TYPE_HEADER, Client.CONTENT_TYPE_APPLICATION_JSON)
                .header(Client.AUTHORIZATION_HEADER,
                        Client.getAuthorizationHeaderValue(accessToken))
                .build();

        HttpResponse<String> data = Client.HTTP_CLIENT.send(request, BodyHandlers.ofString());
        if (data.statusCode() != 200) {
            throw new IllegalStateException(
                    String.format("Failed to update image name. Status code: %s Body: %s",
                            data.statusCode(),
                            data.body()));
        }

        return SerializationUtils.fromJson(data.body(), new ImageName());
    }

    public void deleteImageName(String name)
            throws IOException, InterruptedException, URISyntaxException {
        ImageName imageName = findImageNameByName(name);
        imageName.setOldImageMapping(imageName.getImageMapping());
        imageName.setImageMapping(null);

        HttpRequest request = HttpRequest
                .newBuilder(
                        new URL(new URL(instance), IMAGE_NAMES_ENDPOINT)
                                .toURI())
                .PUT(BodyPublishers
                        .ofString(SerializationUtils.toJson(imageName)))
                .header(Client.AUTHORIZATION_HEADER,
                        Client.getAuthorizationHeaderValue(accessToken))
                .build();

        HttpResponse<String> data = Client.HTTP_CLIENT.send(request, BodyHandlers.ofString());
        if (data.statusCode() != 200) {
            throw new IllegalStateException(
                    String.format("Failed to delete image name. Status code: %s Body: %s",
                            data.statusCode(),
                            data.body()));
        }
    }

    public ImageName createOrUpdateImageName(ImageName imageName)
            throws InterruptedException, IOException, URISyntaxException {
        ImageName existing = findImageNameByName(imageName.getName());
        if (existing != null) {
            imageName.setOldImageMapping(existing.getImageMapping());
            return updateImageName(imageName);
        }

        return createImageName(imageName);
    }

    public ImageName findImageNameByName(String name)
            throws IOException, InterruptedException, URISyntaxException {
        HttpRequest request = HttpRequest
                .newBuilder(
                        new URL(new URL(instance), IMAGE_NAMES_ENDPOINT + "?view=list&$filter=" +
                                Utils.urlEncode(
                                        String.format("imageMappingNormalized.item eq '%s'", name)))
                                .toURI())
                .GET()
                .header(Client.AUTHORIZATION_HEADER,
                        Client.getAuthorizationHeaderValue(accessToken))
                .header(Client.ACCEPT_HEADER, Client.CONTENT_TYPE_APPLICATION_JSON)
                .build();

        HttpResponse<String> data = Client.HTTP_CLIENT.send(request, BodyHandlers.ofString());
        if (data.statusCode() != 200) {
            throw new IllegalStateException(
                    String.format("Failed to find image name. Status code: %s Body: %s",
                            data.statusCode(),
                            data.body()));
        }

        List<ImageName> content = SerializationUtils
                .fromJson(data.body(),
                        SerializationUtils.getCollectionTypeOf(List.class, ImageName.class));

        if (content.size() == 1) {
            return content.get(0);
        }

        return null;
    }

    public List<ImageName> getAllImageNames()
            throws IOException, InterruptedException, URISyntaxException {
        String queryParams = "?view=list";
        // Filter only the image names for endpoint types which we support
        String filter = Arrays.asList(EndpointType.values()).stream()
                .map(type -> String.format("endpoints.item.endpointType eq '%s'", type.getValue()))
                .collect(Collectors.joining(" or "));
        queryParams += "&$filter=" + Utils.urlEncode(filter);
        HttpRequest request = HttpRequest
                .newBuilder(
                        new URL(new URL(instance), IMAGE_NAMES_ENDPOINT + queryParams).toURI())
                .GET()
                .header(Client.AUTHORIZATION_HEADER,
                        Client.getAuthorizationHeaderValue(accessToken))
                .header(Client.ACCEPT_HEADER, Client.CONTENT_TYPE_APPLICATION_JSON)
                .build();

        HttpResponse<String> data = Client.HTTP_CLIENT.send(request, BodyHandlers.ofString());
        if (data.statusCode() != 200) {
            throw new IllegalStateException(
                    String.format("Failed to fetch all image names. Status code: %s Body: %s",
                            data.statusCode(),
                            data.body()));
        }

        return SerializationUtils
                .fromJson(data.body(),
                        SerializationUtils.getCollectionTypeOf(List.class, ImageName.class));
    }

    public List<RegionInfo> fetchRegionsForEndpoint(Endpoint endpoint)
            throws IOException, InterruptedException, URISyntaxException {
        HttpRequest request = HttpRequest
                .newBuilder(
                        new URL(new URL(instance), REGIONS_INFO_ENDPOINT).toURI())
                .POST(BodyPublishers
                        .ofString(SerializationUtils.toJson(endpoint)))
                .header(Client.AUTHORIZATION_HEADER,
                        Client.getAuthorizationHeaderValue(accessToken))
                .header(Client.ACCEPT_HEADER, Client.CONTENT_TYPE_APPLICATION_JSON)
                .build();

        HttpResponse<String> data = Client.HTTP_CLIENT.send(request, BodyHandlers.ofString());
        if (data.statusCode() != 200) {
            throw new IllegalStateException(
                    String.format("Failed to fetch regions for endpoint. Status code: %s Body: %s",
                            data.statusCode(),
                            data.body()));
        }

        return SerializationUtils
                .fromJson(data.body(), new FetchRegionsForEndpointResponse()).getRegions();
    }

    public Region findRegionByEndpointAndRegionName(String endpointName, String regionName)
            throws IOException, InterruptedException, URISyntaxException {
        HttpRequest request = HttpRequest
                .newBuilder(
                        new URL(new URL(instance), REGION_ENDPOINT + "?expand=&$filter=" +
                                Utils.urlEncode(String.format("regionName eq '%s'", regionName)))
                                .toURI())
                .GET()
                .header(Client.AUTHORIZATION_HEADER,
                        Client.getAuthorizationHeaderValue(accessToken))
                .header(Client.ACCEPT_HEADER, Client.CONTENT_TYPE_APPLICATION_JSON)
                .build();

        HttpResponse<String> data = Client.HTTP_CLIENT.send(request, BodyHandlers.ofString());
        if (data.statusCode() != 200) {
            throw new IllegalStateException(
                    String.format("Failed to find region. Status code: %s Body: %s",
                            data.statusCode(),
                            data.body()));
        }

        Map<String, Region> documents = SerializationUtils
                .fromJson(data.body(), new QueryRegionsResponse()).getDocuments();

        for (Region r : documents.values()) {
            if (r.getEndpoint().getName().equals(endpointName)) {
                return r;
            }
        }

        return null;
    }

    public List<Region> getAllRegions()
            throws IOException, InterruptedException, URISyntaxException {
        HttpRequest request = HttpRequest
                .newBuilder(
                        new URL(new URL(instance), REGION_ENDPOINT + "?expand").toURI())
                .GET()
                .header(Client.AUTHORIZATION_HEADER,
                        Client.getAuthorizationHeaderValue(accessToken))
                .header(Client.ACCEPT_HEADER, Client.CONTENT_TYPE_APPLICATION_JSON)
                .build();

        HttpResponse<String> data = Client.HTTP_CLIENT.send(request, BodyHandlers.ofString());
        if (data.statusCode() != 200) {
            throw new IllegalStateException(
                    String.format("Failed to fetch all regions. Status code: %s Body: %s",
                            data.statusCode(),
                            data.body()));
        }

        return new ArrayList<>(SerializationUtils
                .fromJson(data.body(), new QueryRegionsResponse()).getDocuments().values());
    }

    public void updateProjectPrincipals(String projectId,
            UpdateProjectPrincipalsRequest updateRequest)
            throws IOException, URISyntaxException, InterruptedException {
        HttpRequest request = HttpRequest
                .newBuilder(new URL(new URL(instance),
                        String.format(PROJECTS_PRINCIPALS_ENDPOINT, projectId))
                        .toURI())
                .method("PATCH", BodyPublishers
                        .ofString(SerializationUtils.toJson(updateRequest)))
                .header(Client.ACCEPT_HEADER, Client.CONTENT_TYPE_APPLICATION_JSON)
                .header(Client.CONTENT_TYPE_HEADER, Client.CONTENT_TYPE_APPLICATION_JSON)
                .header(Client.AUTHORIZATION_HEADER,
                        Client.getAuthorizationHeaderValue(accessToken))
                .build();

        HttpResponse<String> data = Client.HTTP_CLIENT.send(request, BodyHandlers.ofString());
        if (data.statusCode() != 200) {
            throw new IllegalStateException(
                    String.format("Failed to update project principals. Status code: %s Body: %s",
                            data.statusCode(),
                            data.body()));
        }
    }

    public InstanceName createInstanceName(InstanceName instanceName)
            throws IOException, InterruptedException, URISyntaxException {
        HttpRequest request = HttpRequest
                .newBuilder(new URL(new URL(instance), INSTANCE_NAMES_ENDPOINT).toURI())
                .POST(BodyPublishers
                        .ofString(SerializationUtils.toJson(instanceName)))
                .header(Client.ACCEPT_HEADER, Client.CONTENT_TYPE_APPLICATION_JSON)
                .header(Client.CONTENT_TYPE_HEADER, Client.CONTENT_TYPE_APPLICATION_JSON)
                .header(Client.AUTHORIZATION_HEADER,
                        Client.getAuthorizationHeaderValue(accessToken))
                .build();

        HttpResponse<String> data = Client.HTTP_CLIENT.send(request, BodyHandlers.ofString());
        if (data.statusCode() != 200) {
            throw new IllegalStateException(
                    String.format("Failed to create instance name. Status code: %s Body: %s",
                            data.statusCode(),
                            data.body()));
        }

        return SerializationUtils.fromJson(data.body(), new InstanceName());
    }

    public InstanceName updateInstanceName(InstanceName instanceName)
            throws IOException, InterruptedException, URISyntaxException {
        HttpRequest request = HttpRequest
                .newBuilder(new URL(new URL(instance), INSTANCE_NAMES_ENDPOINT).toURI())
                .PUT(BodyPublishers
                        .ofString(SerializationUtils.toJson(instanceName)))
                .header(Client.ACCEPT_HEADER, Client.CONTENT_TYPE_APPLICATION_JSON)
                .header(Client.CONTENT_TYPE_HEADER, Client.CONTENT_TYPE_APPLICATION_JSON)
                .header(Client.AUTHORIZATION_HEADER,
                        Client.getAuthorizationHeaderValue(accessToken))
                .build();

        HttpResponse<String> data = Client.HTTP_CLIENT.send(request, BodyHandlers.ofString());
        if (data.statusCode() != 200) {
            throw new IllegalStateException(
                    String.format("Failed to update instance name. Status code: %s Body: %s",
                            data.statusCode(),
                            data.body()));
        }

        return SerializationUtils.fromJson(data.body(), new InstanceName());
    }

    public void deleteInstanceName(String name)
            throws IOException, InterruptedException, URISyntaxException {
        InstanceName instanceName = findInstanceNameByName(name);
        instanceName.setOldInstanceTypeMapping(instanceName.getInstanceTypeMapping());
        instanceName.setInstanceTypeMapping(null);

        HttpRequest request = HttpRequest
                .newBuilder(
                        new URL(new URL(instance), INSTANCE_NAMES_ENDPOINT)
                                .toURI())
                .PUT(BodyPublishers
                        .ofString(SerializationUtils.toJson(instanceName)))
                .header(Client.AUTHORIZATION_HEADER,
                        Client.getAuthorizationHeaderValue(accessToken))
                .build();

        HttpResponse<String> data = Client.HTTP_CLIENT.send(request, BodyHandlers.ofString());
        if (data.statusCode() != 200) {
            throw new IllegalStateException(
                    String.format("Failed to delete instance name. Status code: %s Body: %s",
                            data.statusCode(),
                            data.body()));
        }
    }

    public InstanceName createOrUpdateInstanceName(InstanceName instanceName)
            throws InterruptedException, IOException, URISyntaxException {
        InstanceName existing = findInstanceNameByName(instanceName.getName());
        if (existing != null) {
            instanceName.setOldInstanceTypeMapping(existing.getInstanceTypeMapping());
            return updateInstanceName(instanceName);
        }

        return createInstanceName(instanceName);
    }

    public InstanceName findInstanceNameByName(String name)
            throws IOException, InterruptedException, URISyntaxException {
        HttpRequest request = HttpRequest
                .newBuilder(
                        new URL(new URL(instance), INSTANCE_NAMES_ENDPOINT + "?view=list&$filter=" +
                                Utils.urlEncode(
                                        String.format("instanceTypeMappingNormalized.item eq '%s'",
                                                name)))
                                .toURI())
                .GET()
                .header(Client.AUTHORIZATION_HEADER,
                        Client.getAuthorizationHeaderValue(accessToken))
                .header(Client.ACCEPT_HEADER, Client.CONTENT_TYPE_APPLICATION_JSON)
                .build();

        HttpResponse<String> data = Client.HTTP_CLIENT.send(request, BodyHandlers.ofString());
        if (data.statusCode() != 200) {
            throw new IllegalStateException(
                    String.format("Failed to find instance name. Status code: %s Body: %s",
                            data.statusCode(),
                            data.body()));
        }

        List<InstanceName> content = SerializationUtils
                .fromJson(data.body(),
                        SerializationUtils.getCollectionTypeOf(List.class, InstanceName.class));

        if (content.size() == 1) {
            return content.get(0);
        }

        return null;
    }

    public List<InstanceName> getAllInstanceNames()
            throws IOException, InterruptedException, URISyntaxException {
        String queryParams = "?view=list";
        // Filter only the instance names for endpoint types which we support
        String filter = Arrays.asList(EndpointType.values()).stream()
                .map(type -> String.format("endpoints.item.endpointType eq '%s'", type.getValue()))
                .collect(Collectors.joining(" or "));
        queryParams += "&$filter=" + Utils.urlEncode(filter);
        HttpRequest request = HttpRequest
                .newBuilder(
                        new URL(new URL(instance), INSTANCE_NAMES_ENDPOINT + queryParams).toURI())
                .GET()
                .header(Client.AUTHORIZATION_HEADER,
                        Client.getAuthorizationHeaderValue(accessToken))
                .header(Client.ACCEPT_HEADER, Client.CONTENT_TYPE_APPLICATION_JSON)
                .build();

        HttpResponse<String> data = Client.HTTP_CLIENT.send(request, BodyHandlers.ofString());
        if (data.statusCode() != 200) {
            throw new IllegalStateException(
                    String.format("Failed to fetch all instance names. Status code: %s Body: %s",
                            data.statusCode(),
                            data.body()));
        }

        return SerializationUtils
                .fromJson(data.body(),
                        SerializationUtils.getCollectionTypeOf(List.class, InstanceName.class));
    }

    public DataCollector findDataCollectorByName(String name)
            throws IOException, InterruptedException, URISyntaxException {
        // I couldn't make the API to filter by name here :(
        HttpRequest request = HttpRequest
                .newBuilder(new URL(new URL(instance), QUERY_DATA_COLLECTORS).toURI())
                .POST(BodyPublishers
                        .ofString(SerializationUtils.toJson(new QueryDataCollectorsRequest())))
                .header(Client.ACCEPT_HEADER, Client.CONTENT_TYPE_APPLICATION_JSON)
                .header(Client.CONTENT_TYPE_HEADER, Client.CONTENT_TYPE_APPLICATION_JSON)
                .header(Client.AUTHORIZATION_HEADER,
                        Client.getAuthorizationHeaderValue(accessToken))
                .build();

        HttpResponse<String> data = Client.HTTP_CLIENT.send(request, BodyHandlers.ofString());
        if (data.statusCode() != 200) {
            throw new IllegalStateException(
                    String.format(
                            "Failed to create query data collectors task. Status code: %s Body: %s",
                            data.statusCode(),
                            data.body()));
        }

        Map<String, DataCollector> results = SerializationUtils
                .fromJson(data.body(), new QueryDataCollectorsResponse()).getResults()
                .getDocuments();
        for (DataCollector r : results.values()) {
            if (r.getName().equals(name)) {
                return r;
            }
        }

        return null;
    }

    @Data
    @NoArgsConstructor
    public static class QueryDataCollectorsRequest {
        private TaskInfo taskInfo = new TaskInfo();
        private Filter filter = new Filter();

        @Data
        @NoArgsConstructor
        public static class TaskInfo {
            private boolean isDirect = true;
        }

        @Data
        @NoArgsConstructor
        public static class Filter {
        }
    }

    @Data
    @NoArgsConstructor
    public static class QueryDataCollectorsResponse {
        //Extend on demand
        private Results results;

        @Data
        @NoArgsConstructor
        public static class Results {
            private Map<String, DataCollector> documents;
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateProjectPrincipalsRequest {
        @Builder.Default
        List<ModifyProjectPrincipalRequest> modify = Collections.emptyList();

        @Builder.Default
        List<ProjectPrincipal> remove = Collections.emptyList();
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ModifyProjectPrincipalRequest {
        private String email;
        private ProjectPrincipal.Type type;
        private ProjectPrincipal.Role role;
    }

    @Data
    @NoArgsConstructor
    public static class QueryProjectsResponse {
        //Extend on demand
        private List<Project> content;
    }

    @Data
    @NoArgsConstructor
    public static class QueryEndpointsResponse {
        //Extend on demand
        private Map<String, Endpoint> documents;
    }

    @Data
    @NoArgsConstructor
    public static class QueryCloudZonesResponse {
        //Extend on demand
        private Map<String, CloudZone> documents;
    }

    @Data
    @NoArgsConstructor
    public static class QueryRegionsResponse {
        //Extend on demand
        private Map<String, Region> documents;
    }

    @Data
    @NoArgsConstructor
    public static class FetchRegionsForEndpointResponse {
        //Extend on demand
        private List<RegionInfo> regions;
    }
}
