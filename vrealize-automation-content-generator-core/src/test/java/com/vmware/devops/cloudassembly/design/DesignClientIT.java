/*
 * Copyright 2021-2021 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.vmware.devops.cloudassembly.design;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.vmware.devops.ClientTestBase;
import com.vmware.devops.client.cloudassembly.design.stubs.Blueprint;
import com.vmware.devops.client.cloudassembly.infrastructure.stubs.Project;

public class DesignClientIT extends ClientTestBase {

    private static Project testProject;
    private List<Blueprint> blueprints = new ArrayList<>();

    @BeforeClass
    public static void setup() throws InterruptedException, IOException, URISyntaxException {
        testProject = getInfrastructureClient().createProject(Project.builder()
                .name("vrealize-automation-content-generator-test-" + System.currentTimeMillis())
                .build());
    }

    @Test
    public void testCreateBlueprint()
            throws InterruptedException, IOException, URISyntaxException {
        String name = "vrealize-automation-content-generator-test-" + System.currentTimeMillis();
        Blueprint blueprint = getDesignClient().createBlueprint(Blueprint.builder()
                .name(name)
                .content("formatVersion: 1\ninputs: {}\nresources: {}\n")
                .projectId(testProject.getId())
                .build());
        blueprints.add(blueprint);
        Assert.assertNotNull(blueprint.getId());
        blueprint = getDesignClient().findBlueprintByName(name);
    }

    @Test
    public void testCreateOrUpdateBlueprint()
            throws InterruptedException, IOException, URISyntaxException {
        String name = "vrealize-automation-content-generator-test-" + System.currentTimeMillis();
        Blueprint blueprint = getDesignClient().createOrUpdateBlueprint(Blueprint.builder()
                .name(name)
                .content("formatVersion: 1\ninputs: {}\nresources: {}\n")
                .projectId(testProject.getId())
                .build());
        blueprints.add(blueprint);
        Assert.assertNotNull(blueprint.getId());

        blueprint.setContent("test");
        blueprint = getDesignClient().createOrUpdateBlueprint(blueprint);
        // Create / createOrUpdate returns partial data
        blueprint = getDesignClient().getBlueprint(blueprint.getId());
        Assert.assertTrue(blueprint.getContent().equals("test"));
    }

    @Test
    public void testGetAllBlueprints()
            throws IOException, InterruptedException, URISyntaxException {
        int blueprintsCount = 10;
        for (int i = 0; i < blueprintsCount; i++) {
            String name = "vrealize-automation-content-generator-test-" + i + "-" + System.currentTimeMillis();
            Blueprint blueprint = getDesignClient().createBlueprint(Blueprint.builder()
                    .name(name)
                    .content(name)
                    .projectId(testProject.getId())
                    .build());
            Assert.assertNotNull(blueprint.getId());
            blueprints.add(blueprint);
        }
        List<Blueprint> allBlueprints = getDesignClient().getAllBlueprints();
        Assert.assertTrue(allBlueprints.size() >= blueprintsCount);
        List<String> expectedBlueprintIds = blueprints.stream()
                .map(blueprint -> blueprint.getId())
                .collect(Collectors.toList());
        List<Blueprint> expectedBlueprints = allBlueprints.stream()
                .filter(blueprint -> expectedBlueprintIds.contains(blueprint.getId()))
                .collect(Collectors.toList());
        Assert.assertEquals(blueprintsCount, expectedBlueprints.size());
        for (Blueprint blueprint : expectedBlueprints) {
            Assert.assertEquals(blueprint.getName(), blueprint.getContent());
        }
    }

    @After
    public void cleanup() throws InterruptedException, IOException, URISyntaxException {
        if (!blueprints.isEmpty()) {
            for (Blueprint blueprint : blueprints) {
                getDesignClient().deleteBlueprint(blueprint.getId());
            }
        }
        blueprints.clear();
    }

    @AfterClass
    public static void classCleanup() throws InterruptedException, IOException, URISyntaxException {
        getInfrastructureClient().deleteProject(testProject.getId());
    }
}
