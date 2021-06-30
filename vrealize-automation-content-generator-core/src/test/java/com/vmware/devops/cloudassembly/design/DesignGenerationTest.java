/*
 * Copyright 2021-2021 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.vmware.devops.cloudassembly.design;

import java.io.IOException;
import java.net.URISyntaxException;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.vmware.devops.GenerationContext;
import com.vmware.devops.GenerationTestBase;
import com.vmware.devops.IdCache;
import com.vmware.devops.SerializationUtils;
import com.vmware.devops.SpecProcessor;
import com.vmware.devops.Utils;
import com.vmware.devops.client.cloudassembly.design.stubs.Blueprint;
import com.vmware.devops.model.cloudassembly.design.CloudTemplate;
import com.vmware.devops.model.cloudassembly.extensibility.Action;

public class DesignGenerationTest extends GenerationTestBase {

    @BeforeClass
    public static void setup() {
        // So we don't need client
        IdCache.PROJECT_ID_CACHE.getNameToId().put("testProjectName", "test-id");
        IdCache.ACTION_ID_CACHE.getNameToId().put("testAction", "fakeId");
        IdCache.BLUEPRINT_ID_CACHE.getNameToId().put("testBlueprint", "fakeId");
    }

    @Test
    public void cloudTemplateTest() throws IOException, URISyntaxException, InterruptedException {
        SpecProcessor specProcessor = new SpecProcessor();
        CloudTemplate t = (CloudTemplate) specProcessor
                .process(Utils.readFile("tests/cloudassembly/design/cloudTemplateTest.groovy"));

        String output = SerializationUtils
                .prettifyJson(SerializationUtils.toPrettyJson(t.initializeBlueprint()));
        String expectedOutput = Utils
                .readFile("tests/cloudassembly/design/cloudTemplateTestOutput.json");
        Assert.assertEquals(expectedOutput, output);
    }

    @Test
    public void cloudTemplateWithContentFileTest() throws IOException, URISyntaxException, InterruptedException {
        SpecProcessor specProcessor = new SpecProcessor();
        GenerationContext.getInstance().getGlobalConfiguration()
                .setDefaultProject("testProjectName");
        CloudTemplate t = (CloudTemplate) specProcessor
                .process(Utils.readFile("tests/cloudassembly/design/cloudTemplateWithContentFileTest.groovy"));

        String output = SerializationUtils
                .prettifyJson(SerializationUtils.toPrettyJson(t.initializeBlueprint()));
        String expectedOutput = Utils
                .readFile("tests/cloudassembly/design/cloudTemplateWithContentTestOutput.json");
        Assert.assertEquals(expectedOutput, output);
    }

    @Test
    public void inPlaceSubscriptionsForBlueprints()
            throws IOException, URISyntaxException, InterruptedException {
        SpecProcessor specProcessor = new SpecProcessor();
        GenerationContext.getInstance().getGlobalConfiguration()
                .setDefaultProject("defaultTestProjectName");
        CloudTemplate t = (CloudTemplate) specProcessor
                .process(Utils.readFile(
                        "tests/cloudassembly/design/cloudTemplateWithSubscriptionsTest.groovy"));

        Blueprint blueprint = t.initializeBlueprint();
        t.expandSubscriptions(blueprint);
        Assert.assertEquals("testBlueprint-testAction-POST_COMPUTE_PROVISION",
                t.getExtensibility().getSubscriptions().get(0).getName());
        Assert.assertEquals("event.data.blueprintId == \"fakeId\"",
                t.getExtensibility().getSubscriptions().get(0).getCriteria().toString());
        Assert.assertEquals("testBlueprint-testAction-test2",
                t.getExtensibility().getSubscriptions().get(1).getName());
        Assert.assertEquals("(dummy) && (event.data.blueprintId == \"fakeId\")",
                t.getExtensibility().getSubscriptions().get(1).getCriteria().toString());
    }

    @Test
    public void inPlaceActionsForBlueprints()
            throws IOException, URISyntaxException, InterruptedException {
        SpecProcessor specProcessor = new SpecProcessor();
        GenerationContext.getInstance().getGlobalConfiguration()
                .setDefaultProject("defaultTestProjectName");
        CloudTemplate t = (CloudTemplate) specProcessor
                .process(Utils.readFile(
                        "tests/cloudassembly/design/cloudTemplateWithActionTest.groovy"));
        Blueprint blueprint = t.initializeBlueprint();
        t.expandExtensibilityContent(blueprint);
        t.expandSubscriptions(blueprint);
        Assert.assertEquals("testBlueprint-testAction-POST_COMPUTE_PROVISION",
                t.getExtensibility().getSubscriptions().get(0).getName());
        Assert.assertEquals("event.data.blueprintId == \"fakeId\"",
                t.getExtensibility().getSubscriptions().get(0).getCriteria().toString());

        Assert.assertEquals("testBlueprint-testAction-test2",
                t.getExtensibility().getSubscriptions().get(1).getName());
        Assert.assertEquals("(dummy) && (event.data.blueprintId == \"fakeId\")",
                t.getExtensibility().getSubscriptions().get(1).getCriteria().toString());

        Assert.assertEquals("testBlueprint-POST_COMPUTE_PROVISION",
                ((Action) t.getExtensibility().getContent().get(0).getRunnable()).initializeAction()
                        .getName());
        Assert.assertEquals("testBlueprint-pcpa",
                ((Action) t.getExtensibility().getContent().get(1).getRunnable()).initializeAction()
                        .getName());

        Assert.assertEquals("testBlueprint-testBlueprint-POST_COMPUTE_PROVISION-POST_COMPUTE_PROVISION",
                t.getExtensibility().getSubscriptions().get(2).getName());
        Assert.assertEquals("testBlueprint-testBlueprint-pcpa-pcps",
                t.getExtensibility().getSubscriptions().get(3).getName());
    }
}
