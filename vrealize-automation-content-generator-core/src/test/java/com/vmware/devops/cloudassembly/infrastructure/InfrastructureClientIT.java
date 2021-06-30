/*
 * Copyright 2021-2021 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.vmware.devops.cloudassembly.infrastructure;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.After;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import com.vmware.devops.ClientTestBase;
import com.vmware.devops.TestUtils;
import com.vmware.devops.client.Client;
import com.vmware.devops.client.cloudassembly.infrastructure.InfrastructureClient;
import com.vmware.devops.client.cloudassembly.infrastructure.InfrastructureClient.ModifyProjectPrincipalRequest;
import com.vmware.devops.client.cloudassembly.infrastructure.InfrastructureClient.UpdateProjectPrincipalsRequest;
import com.vmware.devops.client.cloudassembly.infrastructure.stubs.CloudZone;
import com.vmware.devops.client.cloudassembly.infrastructure.stubs.DataCollector;
import com.vmware.devops.client.cloudassembly.infrastructure.stubs.Endpoint;
import com.vmware.devops.client.cloudassembly.infrastructure.stubs.Endpoint.EndpointType;
import com.vmware.devops.client.cloudassembly.infrastructure.stubs.EndpointRegions;
import com.vmware.devops.client.cloudassembly.infrastructure.stubs.ImageName;
import com.vmware.devops.client.cloudassembly.infrastructure.stubs.ImageName.ImageMapping;
import com.vmware.devops.client.cloudassembly.infrastructure.stubs.InstanceName;
import com.vmware.devops.client.cloudassembly.infrastructure.stubs.InstanceTypeInfo;
import com.vmware.devops.client.cloudassembly.infrastructure.stubs.Project;
import com.vmware.devops.client.cloudassembly.infrastructure.stubs.Project.PlacementPolicy;
import com.vmware.devops.client.cloudassembly.infrastructure.stubs.ProjectConfig;
import com.vmware.devops.client.cloudassembly.infrastructure.stubs.ProjectConfig.ProjectState;
import com.vmware.devops.client.cloudassembly.infrastructure.stubs.ProjectPrincipal;
import com.vmware.devops.client.cloudassembly.infrastructure.stubs.ProjectPrincipal.Role;
import com.vmware.devops.client.cloudassembly.infrastructure.stubs.ProjectPrincipal.Type;
import com.vmware.devops.client.cloudassembly.infrastructure.stubs.Region;
import com.vmware.devops.client.cloudassembly.infrastructure.stubs.RegionInfo;

public class InfrastructureClientIT extends ClientTestBase {
    private List<Project> projects = new ArrayList<Project>();
    private List<Endpoint> endpoints = new ArrayList<Endpoint>();
    private CloudZone cloudZone;
    private List<ImageName> imageNames = new ArrayList<ImageName>();
    private List<InstanceName> instanceNames = new ArrayList<InstanceName>();

    @Test
    public void testCreateProject()
            throws InterruptedException, IOException, URISyntaxException {
        String name = "vrealize-automation-content-generator-test-" + System.currentTimeMillis();
        Project project = Project.builder()
                .name(name)
                .build();
        project = getInfrastructureClient().createProject(project);
        projects.add(project);
        Assert.assertNotNull(project.getId());
        project = getInfrastructureClient().findProjectByName(name);
        Assert.assertNotNull(project);

    }

    @Test
    public void testCreateUpdateProject()
            throws InterruptedException, IOException, URISyntaxException {
        String name = "vrealize-automation-content-generator-test-" + System.currentTimeMillis();
        Project project = Project.builder()
                .name(name)
                .build();
        project = getInfrastructureClient().createOrUpdateProject(project);
        projects.add(project);
        Assert.assertNotNull(project.getId());

        project.setProperties(
                Map.of(Project.PLACEMENT_POLICY_PROPERTY_KEY, PlacementPolicy.SPREAD.name()));
        project = getInfrastructureClient().createOrUpdateProject(project);

        Assert.assertEquals(PlacementPolicy.SPREAD.name(),
                project.getProperties().get(Project.PLACEMENT_POLICY_PROPERTY_KEY));
    }

    @Test
    public void testGetAllProjects() throws IOException, InterruptedException, URISyntaxException {
        int projectsCount = 10;
        for (int i = 0; i < projectsCount; i++) {
            String name = "vrealize-automation-content-generator-test-" + i + "-" + System.currentTimeMillis();
            Project project = Project.builder()
                    .name(name)
                    .build();
            project = getInfrastructureClient().createProject(project);
            projects.add(project);
        }
        List<Project> allProjects = getInfrastructureClient().getAllProjects();
        Assert.assertTrue(allProjects.size() >= projectsCount);
        List<String> expectedProjectIds = projects.stream()
                .map(project -> project.getId())
                .collect(Collectors.toList());
        List<String> actualProjectIds = allProjects.stream()
                .map(project -> project.getId())
                .collect(Collectors.toList());
        Assert.assertTrue(actualProjectIds.containsAll(expectedProjectIds));
    }

    @Test
    public void testCreateEndpoint()
            throws InterruptedException, IOException, URISyntaxException {
        String name = "vrealize-automation-content-generator-test-" + System.currentTimeMillis();
        Endpoint endpoint = Endpoint.builder()
                .name(name)
                .endpointType(EndpointType.NIMBUS)
                .endpointProperties(Map.of(
                        "dcId", "onprem"
                ))
                .customProperties(Map.of(
                        "isExternal", "false"
                ))
                .build();
        endpoint = getInfrastructureClient().createEndpoint(endpoint);
        Assert.assertNotNull(endpoint.getDocumentSelfLink());
        endpoints.add(endpoint);
        EndpointRegions regions = EndpointRegions.builder()
                .createDefaultZones(true)
                .endpointLink(endpoint.getDocumentSelfLink())
                .enabledRegionIds(List.of("sc", "wdc"))
                .build();

        regions = getInfrastructureClient().updateEndpointRegions(regions);
        Assert.assertNotNull(regions.getEnabledRegionIds());

        endpoint = getInfrastructureClient().findEndpointByName(name);
        Assert.assertNotNull(endpoint.getDocumentSelfLink());
    }

    @Test
    public void testGetAllRegions() throws IOException, InterruptedException, URISyntaxException {
        List<String> expectedRegions = List.of("sc", "wdc", "sof");
        String name = "vrealize-automation-content-generator-test-" + System.currentTimeMillis();
        Endpoint endpoint = Endpoint.builder()
                .name(name)
                .endpointType(EndpointType.NIMBUS)
                .endpointProperties(Map.of(
                        "dcId", "onprem"
                ))
                .customProperties(Map.of(
                        "isExternal", "false"
                ))
                .build();
        endpoint = getInfrastructureClient().createEndpoint(endpoint);
        final String documentSelfLink = endpoint.getDocumentSelfLink();
        Assert.assertNotNull(documentSelfLink);
        endpoints.add(endpoint);
        EndpointRegions regions = EndpointRegions.builder()
                .createDefaultZones(true)
                .endpointLink(endpoint.getDocumentSelfLink())
                .enabledRegionIds(expectedRegions)
                .build();

        regions = getInfrastructureClient().updateEndpointRegions(regions);
        Collection<Region> allRegions = getInfrastructureClient().getAllRegions();
        Assert.assertTrue(allRegions.size() >= expectedRegions.size());
        List<Region> filteredRegions = allRegions.stream()
                .filter(region -> region.getEndpoint().getDocumentSelfLink().equals(documentSelfLink))
                .collect(Collectors.toList());
        Assert.assertEquals(expectedRegions.size(), filteredRegions.size());
    }

    @Test
    public void testCreateUpdateEndpoint()
            throws InterruptedException, IOException, URISyntaxException {
        String name = "vrealize-automation-content-generator-test-" + System.currentTimeMillis();
        Endpoint endpoint = Endpoint.builder()
                .name(name)
                .endpointType(EndpointType.NIMBUS)
                .endpointProperties(Map.of(
                        "dcId", "onprem"
                ))
                .customProperties(new HashMap<>(Map.of(
                        "isExternal", "false"
                )))
                .build();
        Endpoint result = getInfrastructureClient().createOrUpdateEndpoint(endpoint);
        endpoints.add(endpoint);
        String documentSelfLink = result.getDocumentSelfLink();
        Assert.assertNotNull(documentSelfLink);
        endpoint.setDocumentSelfLink(documentSelfLink);

        endpoint.getCustomProperties().put("hello", "world");

        endpoint = getInfrastructureClient().createOrUpdateEndpoint(endpoint);
        Assert.assertEquals(documentSelfLink, endpoint.getDocumentSelfLink());
        Assert.assertTrue(endpoint.getCustomProperties().containsKey("hello"));
        Assert.assertEquals("world", endpoint.getCustomProperties().get("hello"));
    }

    @Test
    public void testGetAllEndpoints() throws IOException, InterruptedException, URISyntaxException {
        int endpointsCount = 3;
        for (int i = 0; i < endpointsCount; i++) {
            String name = "vrealize-automation-content-generator-test-" + i + "-" + System.currentTimeMillis();
            Endpoint endpoint = Endpoint.builder()
                    .name(name)
                    .endpointType(EndpointType.NIMBUS)
                    .endpointProperties(Map.of(
                            "dcId", "onprem"
                    ))
                    .customProperties(Map.of(
                            "isExternal", "false"
                    ))
                    .build();
            endpoint = getInfrastructureClient().createEndpoint(endpoint);
            Assert.assertNotNull(endpoint.getDocumentSelfLink());
            endpoints.add(endpoint);
        }
        Collection<Endpoint> allEndpoints = getInfrastructureClient().getAllEndpoints();
        Assert.assertTrue(allEndpoints.size() >= endpointsCount);
        List<String> expectedEndpointSelfLinks = endpoints.stream()
                .map(endpoint -> endpoint.getDocumentSelfLink())
                .collect(Collectors.toList());
        List<String> actualEndpointSelfLinks = allEndpoints.stream()
                .map(endpoint -> endpoint.getDocumentSelfLink())
                .collect(Collectors.toList());
        Assert.assertTrue(actualEndpointSelfLinks.containsAll(expectedEndpointSelfLinks));
    }

    @Test
    public void testGetAllCloudZones() throws IOException, InterruptedException, URISyntaxException {
        String endpointName = "vrealize-automation-content-generator-test-" + System.currentTimeMillis();
        Endpoint endpoint = Endpoint.builder()
                .name(endpointName)
                .endpointType(EndpointType.NIMBUS)
                .endpointProperties(Map.of(
                        "dcId", "onprem"
                ))
                .customProperties(Map.of(
                        "isExternal", "false"
                ))
                .build();
        endpoint = getInfrastructureClient().createEndpoint(endpoint);
        Assert.assertNotNull(endpoint.getDocumentSelfLink());
        endpoints.add(endpoint);
        EndpointRegions regions = EndpointRegions.builder()
                .createDefaultZones(true)
                .endpointLink(endpoint.getDocumentSelfLink())
                .enabledRegionIds(List.of("sc", "wdc"))
                .build();
        regions = getInfrastructureClient().updateEndpointRegions(regions);

        Collection<CloudZone> allCloudZones = getInfrastructureClient().getAllCloudZones();
        Assert.assertTrue(allCloudZones.size() >= 2);
        Assert.assertTrue(allCloudZones.stream()
                .anyMatch(zone -> zone.getName().equals(endpointName + " / sc")));
        Assert.assertTrue(allCloudZones.stream()
                .anyMatch(zone -> zone.getName().equals(endpointName + " / wdc")));
    }

    @Test
    public void createProjectWithCloudZone()
            throws InterruptedException, IOException, URISyntaxException {
        String name = "vrealize-automation-content-generator-test-" + System.currentTimeMillis();
        Endpoint endpoint = Endpoint.builder()
                .name(name)
                .endpointType(EndpointType.NIMBUS)
                .endpointProperties(Map.of(
                        "dcId", "onprem"
                ))
                .customProperties(new HashMap<>(Map.of(
                        "isExternal", "false"
                )))
                .build();
        endpoint = getInfrastructureClient().createEndpoint(endpoint);
        endpoints.add(endpoint);

        EndpointRegions regions = EndpointRegions.builder()
                .createDefaultZones(true)
                .endpointLink(endpoint.getDocumentSelfLink())
                .enabledRegionIds(List.of("sc"))
                .build();
        getInfrastructureClient().updateEndpointRegions(regions);

        cloudZone = getInfrastructureClient().findCloudZoneByName(name + " / sc");
        Assert.assertNotNull(cloudZone.getDocumentSelfLink());

        Project project = Project.builder()
                .name(name)
                .properties(
                        Map.of(Project.PLACEMENT_POLICY_PROPERTY_KEY, PlacementPolicy.SPREAD.name())
                )
                .build();
        project = getInfrastructureClient().createProject(project);
        projects.add(project);
        ProjectConfig projectConfig = getInfrastructureClient()
                .updateProjectConfig(ProjectConfig.builder()
                        .projectState(ProjectState.builder()
                                .documentSelfLink(
                                        InfrastructureClient.PROJECTS_ENDPOINT + "/" + project
                                                .getId())
                                .build())
                        .cloudZones(List.of(ProjectConfig.CloudZone.builder()
                                .placementZoneLink(cloudZone.getDocumentSelfLink())
                                .build()))
                        .build());
        Assert.assertEquals(cloudZone.getDocumentSelfLink(),
                projectConfig.getCloudZones().get(0).getPlacementZoneLink());
    }

    @Test
    public void testCreateImageName()
            throws InterruptedException, IOException, URISyntaxException {
        String name = "vrealize-automation-content-generator-test-" + System.currentTimeMillis();

        Endpoint endpoint = Endpoint.builder()
                .name(name)
                .endpointType(EndpointType.NIMBUS)
                .endpointProperties(Map.of(
                        "dcId", "onprem"
                ))
                .customProperties(new HashMap<>(Map.of(
                        "isExternal", "false"
                )))
                .build();
        endpoint = getInfrastructureClient().createEndpoint(endpoint);
        endpoints.add(endpoint);

        EndpointRegions regions = EndpointRegions.builder()
                .createDefaultZones(true)
                .endpointLink(endpoint.getDocumentSelfLink())
                .enabledRegionIds(List.of("sc"))
                .enabledRegions(List.of(RegionInfo.builder()
                        .name("Santa Clara")
                        .regionId("sc")
                        .build()))
                .build();
        getInfrastructureClient().updateEndpointRegions(regions);

        Region scRegion = getInfrastructureClient()
                .findRegionByEndpointAndRegionName(name, "Santa Clara");

        ImageName imageName = ImageName.builder()
                .name(name)
                .imageMapping(Map.of(
                        scRegion.getDocumentSelfLink(), ImageMapping.builder()
                                .image("xxx")
                                .build()
                ))
                .build();
        imageName = getInfrastructureClient().createImageName(imageName);
        imageNames.add(imageName);
        Assert.assertNotNull("xxx",
                imageName.getImageMapping().get(scRegion.getDocumentSelfLink()).getImage());
    }

    @Test
    public void testCreateOrUpdateImageName()
            throws InterruptedException, IOException, URISyntaxException {
        String name = "vrealize-automation-content-generator-test-" + System.currentTimeMillis();

        Endpoint endpoint = Endpoint.builder()
                .name(name)
                .endpointType(EndpointType.NIMBUS)
                .endpointProperties(Map.of(
                        "dcId", "onprem"
                ))
                .customProperties(new HashMap<>(Map.of(
                        "isExternal", "false"
                )))
                .build();
        endpoint = getInfrastructureClient().createEndpoint(endpoint);
        endpoints.add(endpoint);

        EndpointRegions regions = EndpointRegions.builder()
                .createDefaultZones(true)
                .endpointLink(endpoint.getDocumentSelfLink())
                .enabledRegionIds(List.of("sc"))
                .enabledRegions(List.of(RegionInfo.builder()
                        .name("Santa Clara")
                        .regionId("sc")
                        .build()))
                .build();
        getInfrastructureClient().updateEndpointRegions(regions);

        Region scRegion = getInfrastructureClient()
                .findRegionByEndpointAndRegionName(name, "Santa Clara");

        ImageName imageName = ImageName.builder()
                .name(name)
                .imageMapping(Map.of(
                        scRegion.getDocumentSelfLink(), ImageMapping.builder()
                                .image("xxx")
                                .build()
                ))
                .build();
        imageName = getInfrastructureClient().createOrUpdateImageName(imageName);
        imageNames.add(imageName);
        Assert.assertEquals(name, imageName.getName());
        Assert.assertNotNull("xxx",
                imageName.getImageMapping().get(scRegion.getDocumentSelfLink()).getImage());

        imageName.getImageMapping()
                .put(scRegion.getDocumentSelfLink(),
                        ImageMapping.builder()
                                .image("xxxxxx")
                                .build());
        imageName = getInfrastructureClient().createOrUpdateImageName(imageName);
        Assert.assertEquals(name, imageName.getName());
        Assert.assertNotNull("xxx",
                imageName.getImageMapping().get(scRegion.getDocumentSelfLink()).getImage());
    }

    @Test
    public void testGetAllImageNames() throws IOException, InterruptedException, URISyntaxException {
        String name = "vrealize-automation-content-generator-test-" + System.currentTimeMillis();

        Endpoint endpoint = Endpoint.builder()
                .name(name)
                .endpointType(EndpointType.NIMBUS)
                .endpointProperties(Map.of(
                        "dcId", "onprem"
                ))
                .customProperties(new HashMap<>(Map.of(
                        "isExternal", "false"
                )))
                .build();
        endpoint = getInfrastructureClient().createEndpoint(endpoint);
        endpoints.add(endpoint);

        EndpointRegions regions = EndpointRegions.builder()
                .createDefaultZones(true)
                .endpointLink(endpoint.getDocumentSelfLink())
                .enabledRegionIds(List.of("sc"))
                .enabledRegions(List.of(RegionInfo.builder()
                        .name("Santa Clara")
                        .regionId("sc")
                        .build()))
                .build();
        getInfrastructureClient().updateEndpointRegions(regions);

        Region scRegion = getInfrastructureClient()
                .findRegionByEndpointAndRegionName(name, "Santa Clara");

        int imageNamesCount = 5;
        for (int i = 0; i < imageNamesCount; i++) {
            String imageNameName = "vrealize-automation-content-generator-test-" + i + "-" + System.currentTimeMillis();
            ImageName imageName = ImageName.builder()
                    .name(imageNameName)
                    .imageMapping(Map.of(
                            scRegion.getDocumentSelfLink(), ImageMapping.builder()
                                    .image(imageNameName)
                                    .build()
                    ))
                    .build();
            imageName = getInfrastructureClient().createImageName(imageName);
            assertEquals(imageNameName, imageName.getName());
            imageNames.add(imageName);
        }
        List<ImageName> allImageNames = getInfrastructureClient().getAllImageNames();
        Assert.assertTrue(allImageNames.size() >= imageNames.size());
        List<String> expectedImageNamesNames = imageNames.stream()
                .map(imageName -> imageName.getName())
                .collect(Collectors.toList());
        List<ImageName> allCreatedImageNames = allImageNames.stream()
                .filter(imageName -> expectedImageNamesNames.contains(imageName.getName()))
                .collect(Collectors.toList());
        Assert.assertEquals(imageNamesCount, allCreatedImageNames.size());
        allCreatedImageNames.forEach(imageName ->
                Assert.assertEquals(imageName.getName(), imageName.getImageMapping()
                        .get(scRegion.getDocumentSelfLink()).getImage()));
    }

    @Test
    public void testCreateInstanceName()
            throws InterruptedException, IOException, URISyntaxException {
        String name = "vrealize-automation-content-generator-test-" + System.currentTimeMillis();

        Endpoint endpoint = Endpoint.builder()
                .name(name)
                .endpointType(EndpointType.NIMBUS)
                .endpointProperties(Map.of(
                        "dcId", "onprem"
                ))
                .customProperties(new HashMap<>(Map.of(
                        "isExternal", "false"
                )))
                .build();
        endpoint = getInfrastructureClient().createEndpoint(endpoint);
        endpoints.add(endpoint);

        EndpointRegions regions = EndpointRegions.builder()
                .createDefaultZones(true)
                .endpointLink(endpoint.getDocumentSelfLink())
                .enabledRegionIds(List.of("sc"))
                .enabledRegions(List.of(RegionInfo.builder()
                        .name("Santa Clara")
                        .regionId("sc")
                        .build()))
                .build();
        getInfrastructureClient().updateEndpointRegions(regions);

        Region scRegion = getInfrastructureClient()
                .findRegionByEndpointAndRegionName(name, "Santa Clara");

        InstanceName instanceName = InstanceName.builder()
                .name(name)
                .instanceTypeMapping(Map.of(
                        scRegion.getDocumentSelfLink(), InstanceTypeInfo.builder()
                                .instanceType("Default")
                                .build()
                ))
                .build();
        instanceName = getInfrastructureClient().createInstanceName(instanceName);
        instanceNames.add(instanceName);
        Assert.assertNotNull("Default",
                instanceName.getInstanceTypeMapping().get(scRegion.getDocumentSelfLink())
                        .getInstanceType());
    }

    @Test
    public void testCreateOrUpdateInstanceName()
            throws InterruptedException, IOException, URISyntaxException {
        String name = "vrealize-automation-content-generator-test-" + System.currentTimeMillis();

        Endpoint endpoint = Endpoint.builder()
                .name(name)
                .endpointType(EndpointType.NIMBUS)
                .endpointProperties(Map.of(
                        "dcId", "onprem"
                ))
                .customProperties(new HashMap<>(Map.of(
                        "isExternal", "false"
                )))
                .build();
        endpoint = getInfrastructureClient().createEndpoint(endpoint);
        endpoints.add(endpoint);

        EndpointRegions regions = EndpointRegions.builder()
                .createDefaultZones(true)
                .endpointLink(endpoint.getDocumentSelfLink())
                .enabledRegionIds(List.of("sc", "wdc"))
                .enabledRegions(List.of(RegionInfo.builder()
                                .name("Santa Clara")
                                .regionId("sc")
                                .build(),
                        RegionInfo.builder()
                                .name("Washington DC")
                                .regionId("wdc")
                                .build()))
                .build();
        getInfrastructureClient().updateEndpointRegions(regions);

        Region scRegion = getInfrastructureClient()
                .findRegionByEndpointAndRegionName(name, "Santa Clara");

        InstanceName instanceName = InstanceName.builder()
                .name(name)
                .instanceTypeMapping(Map.of(
                        scRegion.getDocumentSelfLink(), InstanceTypeInfo.builder()
                                .instanceType("Default")
                                .build()
                ))
                .build();
        instanceName = getInfrastructureClient().createOrUpdateInstanceName(instanceName);
        instanceNames.add(instanceName);
        Assert.assertEquals(name, instanceName.getName());
        Assert.assertNotNull("Default",
                instanceName.getInstanceTypeMapping().get(scRegion.getDocumentSelfLink())
                        .getInstanceType());

        Region wdcRegion = getInfrastructureClient()
                .findRegionByEndpointAndRegionName(name, "Washington DC");

        instanceName.getInstanceTypeMapping()
                .put(wdcRegion.getDocumentSelfLink(),
                        InstanceTypeInfo.builder()
                                .instanceType("Default")
                                .build());
        instanceName = getInfrastructureClient().createOrUpdateInstanceName(instanceName);
        Assert.assertEquals(name, instanceName.getName());
        Assert.assertNotNull("Default",
                instanceName.getInstanceTypeMapping().get(scRegion.getDocumentSelfLink())
                        .getInstanceType());
        Assert.assertNotNull("Default",
                instanceName.getInstanceTypeMapping().get(wdcRegion.getDocumentSelfLink())
                        .getInstanceType());
    }

    @Test
    public void testGetAllInstanceNames() throws IOException, InterruptedException, URISyntaxException {
        String name = "vrealize-automation-content-generator-test-" + System.currentTimeMillis();

        Endpoint endpoint = Endpoint.builder()
                .name(name)
                .endpointType(EndpointType.NIMBUS)
                .endpointProperties(Map.of(
                        "dcId", "onprem"
                ))
                .customProperties(new HashMap<>(Map.of(
                        "isExternal", "false"
                )))
                .build();
        endpoint = getInfrastructureClient().createEndpoint(endpoint);
        endpoints.add(endpoint);

        EndpointRegions regions = EndpointRegions.builder()
                .createDefaultZones(true)
                .endpointLink(endpoint.getDocumentSelfLink())
                .enabledRegionIds(List.of("sc"))
                .enabledRegions(List.of(RegionInfo.builder()
                        .name("Santa Clara")
                        .regionId("sc")
                        .build()))
                .build();
        getInfrastructureClient().updateEndpointRegions(regions);

        Region scRegion = getInfrastructureClient()
                .findRegionByEndpointAndRegionName(name, "Santa Clara");

        int instanceNamesCount = 5;
        for (int i = 0; i < instanceNamesCount; i++) {
            String instanceNameName = "vrealize-automation-content-generator-test-" + i + "-" + System.currentTimeMillis();
            InstanceName instanceName = InstanceName.builder()
                    .name(instanceNameName)
                    .instanceTypeMapping(Map.of(
                            scRegion.getDocumentSelfLink(), InstanceTypeInfo.builder()
                                    .instanceType(instanceNameName)
                                    .build()
                    ))
                    .build();
            instanceName = getInfrastructureClient().createInstanceName(instanceName);
            assertEquals(instanceNameName, instanceName.getName());
            instanceNames.add(instanceName);
        }
        List<InstanceName> allInstanceNames = getInfrastructureClient().getAllInstanceNames();
        Assert.assertTrue(allInstanceNames.size() >= instanceNames.size());
        List<String> expectedInstanceNamesNames = instanceNames.stream()
                .map(instanceName -> instanceName.getName())
                .collect(Collectors.toList());
        List<InstanceName> allCreatedInstanceNames = allInstanceNames.stream()
                .filter(instanceName -> expectedInstanceNamesNames.contains(instanceName.getName()))
                .collect(Collectors.toList());
        Assert.assertEquals(instanceNamesCount, allCreatedInstanceNames.size());
        allCreatedInstanceNames.forEach(instanceName ->
                Assert.assertEquals(instanceName.getName(), instanceName.getInstanceTypeMapping()
                        .get(scRegion.getDocumentSelfLink()).getInstanceType()));
    }

    @Test
    public void testUpdateProjectPrincipals()
            throws InterruptedException, IOException, URISyntaxException {
        String name = "vrealize-automation-content-generator-test-" + System.currentTimeMillis();
        Project project = Project.builder()
                .name(name)
                .build();
        project = getInfrastructureClient().createProject(project);
        projects.add(project);
        String testUser = TestUtils.getProperty("project.principals.user");
        String testGroup = TestUtils.getProperty("project.principals.group");

        getInfrastructureClient().updateProjectPrincipals(
                project.getId(), UpdateProjectPrincipalsRequest.builder()
                        .modify(List.of(
                                ModifyProjectPrincipalRequest.builder()
                                        .email(testUser)
                                        .role(Role.MEMBER)
                                        .type(Type.USER)
                                        .build(),
                                ModifyProjectPrincipalRequest.builder()
                                        .email(testGroup)
                                        .role(Role.ADMINISTRATOR)
                                        .type(Type.GROUP)
                                        .build()
                        ))
                        .build()
        );

        project = getInfrastructureClient().findProjectByName(name);
        Assert.assertEquals(testUser, project.getMembers().get(0).getEmail());
        Assert.assertEquals(testGroup, project.getAdministrators().get(0).getEmail());

        getInfrastructureClient().updateProjectPrincipals(
                project.getId(), UpdateProjectPrincipalsRequest.builder()
                        .remove(List.of(
                                ProjectPrincipal.builder()
                                        .email(testGroup)
                                        .type(Type.GROUP)
                                        .build()
                        ))
                        .build()
        );

        project = getInfrastructureClient().findProjectByName(name);
        Assert.assertEquals(0, project.getAdministrators().size());
    }

    @Test
    public void fetchRegionsForEndpoint()
            throws InterruptedException, IOException, URISyntaxException {
        String name = "vrealize-automation-content-generator-test-" + System.currentTimeMillis();
        Endpoint endpoint = Endpoint.builder()
                .name(name)
                .endpointType(EndpointType.NIMBUS)
                .endpointProperties(Map.of(
                        "dcId", "onprem"
                ))
                .customProperties(Map.of(
                        "isExternal", "false"
                ))
                .build();
        List<RegionInfo> regions = getInfrastructureClient().fetchRegionsForEndpoint(endpoint);
        Assert.assertEquals(3, regions.size());
    }

    @Test
    @Ignore("This can only run on the cloud and requires some infra")
    public void findDataCollectorByName()
            throws InterruptedException, IOException, URISyntaxException {
        DataCollector dataCollector = new Client("https://console.cloud.vmware.com",
                "https://api.mgmt.cloud.vmware.com",
                "xxx")
                .getCloudAssembly().getInfrastructure()
                .findDataCollectorByName("ip-172-31-20-194");
        Assert.assertNotNull(dataCollector.getProxyId());
    }

    @After
    public void cleanup() throws InterruptedException, IOException, URISyntaxException {
        if (!imageNames.isEmpty()) {
            for (ImageName imageName : imageNames) {
                getInfrastructureClient().deleteImageName(imageName.getName());
            }
            imageNames.clear();
        }

        if (!instanceNames.isEmpty()) {
            for (InstanceName instanceName : instanceNames) {
                getInfrastructureClient().deleteInstanceName(instanceName.getName());
            }
            instanceNames.clear();
        }

        if (cloudZone != null && !projects.isEmpty()) {
            for (Project project : projects) {
                getInfrastructureClient().updateProjectConfig(ProjectConfig.builder()
                        .projectState(ProjectState.builder()
                                .documentSelfLink(
                                        InfrastructureClient.PROJECTS_ENDPOINT + "/"
                                                + project.getId())
                                .build())
                        .cloudZones(Collections.emptyList())
                        .build());
            }
            cloudZone = null;
        }

        if (!projects.isEmpty()) {
            for (Project project : projects) {
                getInfrastructureClient().deleteProject(project.getId());
            }
            projects.clear();
        }

        if (!endpoints.isEmpty()) {
            for (Endpoint endpoint : endpoints) {
                getInfrastructureClient().deleteEndpoint(endpoint.getDocumentSelfLink());
            }
            endpoints.clear();
        }
    }
}
