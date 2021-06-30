/*
 * Copyright 2021-2021 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.vmware.devops.cloudassembly.infrastructure;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import com.vmware.devops.ReverseGenerationContext;
import com.vmware.devops.TestUtils;
import com.vmware.devops.Utils;
import com.vmware.devops.client.Client;
import com.vmware.devops.client.cloudassembly.CloudAssemblyClient;
import com.vmware.devops.client.cloudassembly.infrastructure.InfrastructureClient;
import com.vmware.devops.client.cloudassembly.infrastructure.stubs.DataCollector;
import com.vmware.devops.client.cloudassembly.infrastructure.stubs.Endpoint;
import com.vmware.devops.client.cloudassembly.infrastructure.stubs.Endpoint.EndpointType;
import com.vmware.devops.client.cloudassembly.infrastructure.stubs.ImageName;
import com.vmware.devops.client.cloudassembly.infrastructure.stubs.InstanceName;
import com.vmware.devops.client.cloudassembly.infrastructure.stubs.InstanceTypeInfo;
import com.vmware.devops.client.cloudassembly.infrastructure.stubs.Project.PlacementPolicy;
import com.vmware.devops.client.cloudassembly.infrastructure.stubs.ProjectPrincipal;
import com.vmware.devops.client.cloudassembly.infrastructure.stubs.ProjectPrincipal.Type;
import com.vmware.devops.client.cloudassembly.infrastructure.stubs.Region;
import com.vmware.devops.client.cloudassembly.infrastructure.stubs.RegionInfo;
import com.vmware.devops.config.EndpointConfiguration;
import com.vmware.devops.model.VraExportedData;
import com.vmware.devops.model.cloudassembly.infrastructure.FlavorMapping;
import com.vmware.devops.model.cloudassembly.infrastructure.ImageMapping;
import com.vmware.devops.model.cloudassembly.infrastructure.NimbusCloudAccount;
import com.vmware.devops.model.cloudassembly.infrastructure.NimbusCloudAccount.NimbusRegion;
import com.vmware.devops.model.cloudassembly.infrastructure.Project;
import com.vmware.devops.model.cloudassembly.infrastructure.VsphereCloudAccount;

public class InfrastructureReverseGenerationTest {

    @Test
    public void nimbusCloudAccountTest() throws Exception {
        File outputDir = TestUtils.createTempDir();

        Endpoint endpoint = Endpoint.builder()
                .name("test")
                .endpointType(EndpointType.NIMBUS)
                .build();

        VraExportedData data = new VraExportedData();
        data.setRegions(List.of(
                Region.builder()
                        .regionId(NimbusRegion.SC.getId())
                        .build(),
                Region.builder()
                        .regionId(NimbusRegion.WDC.getId())
                        .build()));
        data.setEndpoints(List.of(
                endpoint
        ));
        ReverseGenerationContext.getInstance().setVraExportedData(data);
        ReverseGenerationContext.getInstance().setOutputDir(outputDir.getAbsolutePath());

        InfrastructureClient infrastructureClient = mock(InfrastructureClient.class);
        doReturn(List.of(
                RegionInfo.builder()
                        .regionId(NimbusRegion.SC.getId())
                        .build(),
                RegionInfo.builder()
                        .regionId(NimbusRegion.WDC.getId())
                        .build()
        )).when(infrastructureClient).fetchRegionsForEndpoint(endpoint);

        CloudAssemblyClient cloudAssemblyClient = mock(CloudAssemblyClient.class);
        doReturn(infrastructureClient).when(cloudAssemblyClient).getInfrastructure();

        Client mockClient = mock(Client.class);
        doReturn(cloudAssemblyClient).when(mockClient).getCloudAssembly();

        EndpointConfiguration endpointConfiguration = spy(new EndpointConfiguration());
        doReturn(mockClient).when(endpointConfiguration).getClient();

        ReverseGenerationContext.getInstance().setEndpointConfiguration(endpointConfiguration);

        new NimbusCloudAccount().dumpAll();

        String output = Utils
                .readFile(
                        new File(outputDir, "010-test-nimbus-cloud-account.groovy").getAbsolutePath());
        String expectedOutput = Utils
                .readFile(
                        "tests/cloudassembly/infrastructure/nimbusCloudAccountReverseGenerateTestOutput.test.groovy");
        Assert.assertEquals(expectedOutput, output);
    }

    @Test
    public void vsphereCloudAccountTest() throws Exception {
        File outputDir = TestUtils.createTempDir();

        Endpoint endpoint = Endpoint.builder()
                .name("test")
                .documentSelfLink("test-link")
                .endpointType(EndpointType.VSPHERE)
                .endpointProperties(Map.of(
                        VsphereCloudAccount.HOST_NAME_ENDPOINT_PROPERTY_KEY, "vc-hostname",
                        VsphereCloudAccount.PRIVATE_KEY_ID_ENDPOINT_PROPERTY_KEY,
                        "username",
                        VsphereCloudAccount.DC_ID_ENDPOINT_PROPERTY_KEY, "dc-id"
                ))
                .build();

        VraExportedData data = new VraExportedData();
        data.setRegions(List.of(
                Region.builder()
                        .regionId("dc-1")
                        .regionName("Datacenter-1")
                        .endpoint(endpoint)
                        .build(),
                Region.builder()
                        .regionId("dc-2")
                        .regionName("Datacenter-2")
                        .endpoint(endpoint)
                        .build(),
                Region.builder()
                        .regionId("dc-3")
                        .regionName("Datacenter-3")
                        .endpoint(Endpoint.builder()
                                .documentSelfLink("test-link-2")
                                .build())
                        .build()
        ));
        data.setEndpoints(List.of(
                endpoint
        ));
        data.setDataCollectors(List.of(
                DataCollector.builder()
                        .name("data-collector")
                        .proxyId("dc-id")
                        .build()
        ));

        ReverseGenerationContext.getInstance().setVraExportedData(data);
        ReverseGenerationContext.getInstance().setOutputDir(outputDir.getAbsolutePath());

        new VsphereCloudAccount().dumpAll();

        String output = Utils
                .readFile(
                        new File(outputDir, "010-test-vsphere-cloud-account.groovy").getAbsolutePath());
        String expectedOutput = Utils
                .readFile(
                        "tests/cloudassembly/infrastructure/vsphereCloudAccountReverseGenerateTestOutput.test.groovy");
        Assert.assertEquals(expectedOutput, output);
    }

    @Test
    public void projectReverseGenerationTest() throws Exception {
        File outputDir = TestUtils.createTempDir();

        VraExportedData data = new VraExportedData();
        data.setProjects(List.of(
                com.vmware.devops.client.cloudassembly.infrastructure.stubs.Project
                        .builder()
                        .name("test")
                        .id("dummy-id")
                        .administrators(List.of(
                                ProjectPrincipal.builder()
                                        .email("admin@org.com")
                                        .type(Type.USER)
                                        .build(),
                                ProjectPrincipal.builder()
                                        .email("admin-group")
                                        .type(Type.GROUP)
                                        .build()
                        ))
                        .members(List.of(
                                ProjectPrincipal.builder()
                                        .email("member@org.com")
                                        .type(Type.USER)
                                        .build()
                        ))
                        .viewers(List.of(
                                ProjectPrincipal.builder()
                                        .email("viewer@org.com")
                                        .type(Type.USER)
                                        .build()
                        ))
                        .properties(Map.of(
                                com.vmware.devops.client.cloudassembly.infrastructure.stubs.Project.PLACEMENT_POLICY_PROPERTY_KEY,
                                PlacementPolicy.SPREAD.toString()
                        ))
                        .build()
        ));

        data.setCloudZones(List.of(
                com.vmware.devops.client.cloudassembly.infrastructure.stubs.CloudZone
                        .builder()
                        .name("sc")
                        .projectIds(List.of("dummy-id"))
                        .build(),
                com.vmware.devops.client.cloudassembly.infrastructure.stubs.CloudZone
                        .builder()
                        .name("wdc")
                        .projectIds(List.of("dummy-id"))
                        .build()
        ));

        ReverseGenerationContext.getInstance().setVraExportedData(data);
        ReverseGenerationContext.getInstance().setOutputDir(outputDir.getAbsolutePath());

        new Project().dumpAll();

        String output = Utils
                .readFile(
                        new File(outputDir, "020-test-project.groovy").getAbsolutePath());
        String expectedOutput = Utils
                .readFile(
                        "tests/cloudassembly/infrastructure/projectReverseGenerateTestOutput.test.groovy");
        Assert.assertEquals(expectedOutput, output);
    }

    @Test
    public void imageMappingReverseGenerationTest() throws Exception {
        File outputDir = TestUtils.createTempDir();

        VraExportedData data = new VraExportedData();
        data.setImageNames(List.of(
                ImageName.builder()
                        .name("test")
                        .imageMapping(Map.of(
                                "link/to/sc", ImageName.ImageMapping.builder()
                                        .image("image-1")
                                        .build(),
                                "link/to/wdc", ImageName.ImageMapping.builder()
                                        .image("image-2")
                                        .build()
                        ))
                        .build()
        ));

        Endpoint endpoint = Endpoint.builder()
                .name("endpoint")
                .build();

        data.setRegions(List.of(
                Region.builder()
                        .documentSelfLink("link/to/sc")
                        .regionName("sc")
                        .endpoint(endpoint)
                        .build(),
                Region.builder()
                        .documentSelfLink("link/to/wdc")
                        .regionName("wdc")
                        .endpoint(endpoint)
                        .build()
        ));

        ReverseGenerationContext.getInstance().setVraExportedData(data);
        ReverseGenerationContext.getInstance().setOutputDir(outputDir.getAbsolutePath());

        new ImageMapping().dumpAll();
        String output = Utils
                .readFile(
                        new File(outputDir, "test-image-mapping.groovy").getAbsolutePath());
        String expectedOutput = Utils
                .readFile(
                        "tests/cloudassembly/infrastructure/imageMappingReverseGenerateTestOutput.test.groovy");
        Assert.assertEquals(expectedOutput, output);
    }

    @Test
    public void flavorMappingReverseGenerationTest() throws Exception {
        File outputDir = TestUtils.createTempDir();

        VraExportedData data = new VraExportedData();
        data.setInstanceNames(List.of(
                InstanceName.builder()
                        .name("small")
                        .instanceTypeMapping(Map.of(
                                "link/to/sc", InstanceTypeInfo.builder()
                                        .instanceType("instance-1")
                                        .build(),
                                "link/to/wdc", InstanceTypeInfo.builder()
                                        .instanceType("instance-2")
                                        .build(),
                                "link/to/datacenter", InstanceTypeInfo.builder()
                                        .cpuCount(8)
                                        .memoryMb(4096)
                                        .build()))
                        .build()
        ));

        data.setRegions(List.of(
                Region.builder()
                        .documentSelfLink("link/to/sc")
                        .regionName("sc")
                        .endpoint(Endpoint.builder()
                                .name("nimbus-endpoint")
                                .endpointType(EndpointType.NIMBUS)
                                .build())
                        .build(),
                Region.builder()
                        .documentSelfLink("link/to/wdc")
                        .regionName("wdc")
                        .endpoint(Endpoint.builder()
                                .name("nimbus-endpoint")
                                .endpointType(EndpointType.NIMBUS)
                                .build())
                        .build(),
                Region.builder()
                        .documentSelfLink("link/to/datacenter")
                        .regionName("DATACENTER-1")
                        .endpoint(Endpoint.builder()
                                .name("vsphere-endpoint")
                                .endpointType(EndpointType.VSPHERE)
                                .build())
                        .build()
        ));

        ReverseGenerationContext.getInstance().setVraExportedData(data);
        ReverseGenerationContext.getInstance().setOutputDir(outputDir.getAbsolutePath());

        new FlavorMapping().dumpAll();
        String output = Utils
                .readFile(
                        new File(outputDir, "small-flavor-mapping.groovy").getAbsolutePath());
        String expectedOutput = Utils
                .readFile(
                        "tests/cloudassembly/infrastructure/flavorMappingReverseGenerateTestOutput.test.groovy");
        Assert.assertEquals(expectedOutput, output);
    }
}
