/*
 * Copyright 2021-2021 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.vmware.devops.cloudassembly.infrastructure;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

import java.io.IOException;
import java.util.List;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.vmware.devops.GenerationTestBase;
import com.vmware.devops.IdCache;
import com.vmware.devops.SerializationUtils;
import com.vmware.devops.SpecProcessor;
import com.vmware.devops.Utils;
import com.vmware.devops.client.cloudassembly.infrastructure.stubs.ProjectPrincipal;
import com.vmware.devops.client.cloudassembly.infrastructure.stubs.RegionInfo;
import com.vmware.devops.model.cloudassembly.infrastructure.FlavorMapping;
import com.vmware.devops.model.cloudassembly.infrastructure.ImageMapping;
import com.vmware.devops.model.cloudassembly.infrastructure.NimbusCloudAccount;
import com.vmware.devops.model.cloudassembly.infrastructure.Project;
import com.vmware.devops.model.cloudassembly.infrastructure.VsphereCloudAccount;

public class InfrastructureGenerationTest extends GenerationTestBase {

    @BeforeClass
    public static void setup() {
        // So we don't need client
        IdCache.CLOUD_ZONE_LINK_CACHE.getNameToId().put("nimbus / sc", "fakeLink");
        IdCache.REGION_LINK_CACHE.getNameToId().put("nimbus / sc", "fakeLink");
        IdCache.REGION_LINK_CACHE.getNameToId().put("vc6 / Datacenter", "fakeLink2");
    }

    @Test
    public void nimbusCloudAccountTest() throws IOException {
        SpecProcessor specProcessor = new SpecProcessor();
        NimbusCloudAccount account = (NimbusCloudAccount) specProcessor
                .process(Utils.readFile(
                        "tests/cloudassembly/infrastructure/nimbusCloudAccountTest.groovy"));

        String output = SerializationUtils
                .prettifyJson(SerializationUtils.toPrettyJson(account.getEndpoint()));
        String expectedOutput = Utils
                .readFile(
                        "tests/cloudassembly/infrastructure/nimbusCloudAccountEndpointTestOutput.json");
        Assert.assertEquals(expectedOutput, output);

        output = SerializationUtils
                .prettifyJson(SerializationUtils.toPrettyJson(account.getEndpointRegions()));
        expectedOutput = Utils
                .readFile(
                        "tests/cloudassembly/infrastructure/nimbusCloudAccountRegionsTestOutput.json");
        Assert.assertEquals(expectedOutput, output);
    }

    @Test
    public void projectTest() throws IOException {
        SpecProcessor specProcessor = new SpecProcessor();
        Project project = (Project) specProcessor
                .process(Utils.readFile("tests/cloudassembly/infrastructure/projectTest.groovy"));
        project.initializeProjectConfig();

        com.vmware.devops.client.cloudassembly.infrastructure.stubs.Project projectStub = project
                .initializeProject();

        String output = SerializationUtils
                .prettifyJson(SerializationUtils.toPrettyJson(projectStub));
        String expectedOutput = Utils
                .readFile("tests/cloudassembly/infrastructure/projectOutput.json");
        Assert.assertEquals(expectedOutput, output);

        output = SerializationUtils
                .prettifyJson(SerializationUtils.toPrettyJson(project.initializeProjectConfig()));
        expectedOutput = Utils
                .readFile("tests/cloudassembly/infrastructure/projectConfigOutput.json");
        Assert.assertEquals(expectedOutput, output);

        projectStub.setAdministrators(List.of(ProjectPrincipal.builder()
                .email("removeMe")
                .build()));

        output = SerializationUtils
                .prettifyJson(SerializationUtils
                        .toPrettyJson(project.createUpdateProjectPrincipalsRequest(projectStub)));
        expectedOutput = Utils
                .readFile(
                        "tests/cloudassembly/infrastructure/projectUpdatePrincipalsRequestOutput.json");
        Assert.assertEquals(expectedOutput, output);
    }

    @Test
    public void imageMappingTest() throws IOException {
        SpecProcessor specProcessor = new SpecProcessor();
        ImageMapping imageMapping = (ImageMapping) specProcessor
                .process(Utils.readFile(
                        "tests/cloudassembly/infrastructure/imageMappingTest.groovy"));

        String output = SerializationUtils
                .prettifyJson(SerializationUtils.toPrettyJson(imageMapping.initializeImageName()));
        String expectedOutput = Utils
                .readFile("tests/cloudassembly/infrastructure/imageMappingTestOutput.json");
        Assert.assertEquals(expectedOutput, output);
    }

    @Test
    public void flavorMappingTest() throws IOException {
        SpecProcessor specProcessor = new SpecProcessor();
        FlavorMapping flavorMapping = (FlavorMapping) specProcessor
                .process(Utils.readFile(
                        "tests/cloudassembly/infrastructure/flavorMappingTest.groovy"));

        String output = SerializationUtils
                .prettifyJson(
                        SerializationUtils.toPrettyJson(flavorMapping.initializeInstanceName()));
        String expectedOutput = Utils
                .readFile("tests/cloudassembly/infrastructure/flavorMappingTestOutput.json");
        Assert.assertEquals(expectedOutput, output);
    }

    @Test
    public void vsphereCloudAccountTest() throws IOException {
        SpecProcessor specProcessor = new SpecProcessor();
        VsphereCloudAccount account = (VsphereCloudAccount) specProcessor
                .process(Utils.readFile(
                        "tests/cloudassembly/infrastructure/vsphereCloudAccountTest.groovy"));

        VsphereCloudAccount mock = spy(account);
        doReturn("fakeCert").when(mock).fetchCertificate();
        doReturn(List.of(RegionInfo.builder()
                .name("Datacenter")
                .regionId("dummyId")
                .build())).when(mock).filterEnabledRegions();

        String output = SerializationUtils
                .prettifyJson(SerializationUtils.toPrettyJson(mock.getEndpoint()));
        String expectedOutput = Utils
                .readFile(
                        "tests/cloudassembly/infrastructure/vsphereCloudAccountEndpointTestOutput.json");
        Assert.assertEquals(expectedOutput, output);

        output = SerializationUtils
                .prettifyJson(SerializationUtils.toPrettyJson(mock.getEndpointRegions()));
        expectedOutput = Utils
                .readFile(
                        "tests/cloudassembly/infrastructure/vsphereCloudAccountRegionsTestOutput.json");
        Assert.assertEquals(expectedOutput, output);
    }
}
