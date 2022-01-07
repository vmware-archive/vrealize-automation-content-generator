/*
 * Copyright 2021-2021 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.vmware.devops.cloudassembly.extensibility;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.vmware.devops.GenerationTestBase;
import com.vmware.devops.IdCache;
import com.vmware.devops.SerializationUtils;
import com.vmware.devops.SpecProcessor;
import com.vmware.devops.Utils;
import com.vmware.devops.client.cloudassembly.extensibility.stubs.Subscription.EventType;
import com.vmware.devops.client.cloudassembly.extensibility.stubs.Subscription.ResourceAction;
import com.vmware.devops.client.cloudassembly.extensibility.stubs.Subscription.RunnableType;
import com.vmware.devops.model.cloudassembly.extensibility.Action;
import com.vmware.devops.model.cloudassembly.extensibility.Criteria;
import com.vmware.devops.model.cloudassembly.extensibility.Criteria.BlueprintNameCriteria;
import com.vmware.devops.model.cloudassembly.extensibility.Criteria.EventTypeCriteria;
import com.vmware.devops.model.cloudassembly.extensibility.Criteria.ResourceActionNameCriteria;
import com.vmware.devops.model.cloudassembly.extensibility.Subscription;

public class ExtensibilityGenerationTest extends GenerationTestBase {
    @BeforeClass
    public static void setup() {
        // So we don't need client
        IdCache.PROJECT_ID_CACHE.getNameToId().put("testProjectName", "fakeId");
        IdCache.ACTION_ID_CACHE.getNameToId().put("testAction", "fakeId");
        IdCache.BLUEPRINT_ID_CACHE.getNameToId().put("testBlueprint", "fakeId");
    }

    @Test
    public void pythonScriptActionTest()
            throws IOException, URISyntaxException, InterruptedException {
        SpecProcessor specProcessor = new SpecProcessor();
        Action action = (Action) specProcessor
                .process(Utils.readFile(
                        "tests/cloudassembly/extensibility/pythonScriptActionTest.groovy"));
        String output = SerializationUtils
                .prettifyJson(SerializationUtils.toPrettyJson(action.initializeAction()));
        String expectedOutput = Utils
                .readFile(
                        "tests/cloudassembly/extensibility/pythonScriptActionTestOutput.json");
        Assert.assertEquals(expectedOutput, output);
    }

    @Test
    public void pythonPackageActionTest()
            throws IOException, URISyntaxException, InterruptedException {
        createCompressedActionPackage();
        SpecProcessor specProcessor = new SpecProcessor();
        Action action = (Action) specProcessor
                .process(Utils.readFile(
                        "tests/cloudassembly/extensibility/pythonPackageActionTest.groovy"));

        Assert.assertNotNull(action.initializeAction().getCompressedContent());
    }

    @Test
    public void subscriptionTest() throws IOException, URISyntaxException, InterruptedException {
        SpecProcessor specProcessor = new SpecProcessor();
        Subscription subscription = (Subscription) specProcessor
                .process(Utils.readFile(
                        "tests/cloudassembly/extensibility/subscriptionTest.groovy"));
        String output = SerializationUtils
                .prettifyJson(SerializationUtils.toPrettyJson(subscription.initializeSubscription()));
        String expectedOutput = Utils
                .readFile(
                        "tests/cloudassembly/extensibility/subscriptionTestOutput.json");
        Assert.assertEquals(expectedOutput, output);
    }

    @Test
    public void criteriaOperationsTest() {
        Criteria a = new Criteria("a");
        Criteria b = new Criteria("b");
        Assert.assertEquals("(a) && (b)", a.and(b).toString());
        Assert.assertEquals("(a) || (b)", a.or(b).toString());
        Assert.assertEquals("!(a)", a.not().toString());
        Assert.assertEquals("a", a.and(Criteria.EMPTY_CRITERIA).toString());
        Assert.assertEquals("a", a.or(Criteria.EMPTY_CRITERIA).toString());
        Assert.assertEquals("", Criteria.EMPTY_CRITERIA.not().toString());
    }

    @Test
    public void blueprintNameCriteriaTest() throws Exception {
        // This ensures we don't make calls to fetch IDs when creating the criteria
        new BlueprintNameCriteria("xxx")
                .or(new BlueprintNameCriteria("yyy"))
                .and(new BlueprintNameCriteria("zzz"))
                .not();

        Assert.assertEquals("event.data.blueprintId == \"fakeId\"",
                new BlueprintNameCriteria("testBlueprint").toString());
    }

    @Test
    public void eventTypeCriteriaTest() {
        Assert.assertEquals("event.data.eventType == \"CREATE_DEPLOYMENT\"",
                new EventTypeCriteria(EventType.CREATE_DEPLOYMENT).toString());
    }

    @Test
    public void resourceNameCriteriaTest() {
        Assert.assertEquals("event.data.actionName == \"ChangeLease\"",
                new ResourceActionNameCriteria(ResourceAction.CHANGE_LEASE).toString());
    }

    @Test
    public void inPlaceSubscriptionsForActions()
            throws IOException, URISyntaxException, InterruptedException {
        SpecProcessor specProcessor = new SpecProcessor();
        Action action = (Action) specProcessor
                .process(Utils.readFile(
                        "tests/cloudassembly/extensibility/pythonScriptActionTestWithSubscriptions.groovy"));
        com.vmware.devops.client.cloudassembly.extensibility.stubs.Action actionStub = action
                .initializeAction();
        action.expandSubscriptions(actionStub);
        Assert.assertEquals("test-action-POST_COMPUTE_PROVISION",
                action.getSubscriptions().get(0).getName());
        Assert.assertEquals("test-action",
                action.getSubscriptions().get(0).getRunnableName());
        Assert.assertEquals(RunnableType.ACTION,
                action.getSubscriptions().get(0).getRunnableType());

        Assert.assertEquals("test-action-test2",
                action.getSubscriptions().get(1).getName());
        Assert.assertEquals("test-action",
                action.getSubscriptions().get(1).getRunnableName());
        Assert.assertEquals(RunnableType.ACTION,
                action.getSubscriptions().get(1).getRunnableType());
    }

    public void createCompressedActionPackage() throws IOException {
        String outputFilePath = System.getProperty("java.io.tmpdir") + "/testContent.zip";
        File outputFile = new File(outputFilePath);
        if (outputFile.exists()) {
            outputFile.delete();
        }

        FileOutputStream fos = new FileOutputStream(outputFilePath);
        ZipOutputStream zipOut = new ZipOutputStream(fos);
        InputStream is = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("tests/cloudassembly/extensibility/basicActionScript.py");
        ZipEntry zipEntry = new ZipEntry("basicActionScript.py");
        zipOut.putNextEntry(zipEntry);
        byte[] bytes = new byte[1024];
        int length;
        while ((length = is.read(bytes)) >= 0) {
            zipOut.write(bytes, 0, length);
        }
        zipOut.close();
        is.close();
        fos.close();
    }

    @Test
    public void flowActionTest()
            throws Exception {
        SpecProcessor specProcessor = new SpecProcessor();
        Action action = (Action) specProcessor
                .process(Utils.readFile(
                        "tests/cloudassembly/extensibility/flowActionTest.groovy"));
        List<Action> childActions = action.expandFlow();

        String output = SerializationUtils
                .prettifyJson(SerializationUtils.toPrettyJson(action.initializeAction()));
        String expectedOutput = Utils
                .readFile(
                        "tests/cloudassembly/extensibility/flowActionTestOutput.json");
        Assert.assertEquals(expectedOutput, output);

        Assert.assertEquals(1, childActions.size());
        Assert.assertEquals("test-flow-third", childActions.get(0).getName());
    }

    @Test
    public void flowActionWithContentFileTest() throws Exception {
        SpecProcessor specProcessor = new SpecProcessor();
        Action action = (Action) specProcessor
                .process(Utils.readFile(
                        "tests/cloudassembly/extensibility/flowActionWithContentFileTest.groovy"));
        List<Action> childActions = action.expandFlow();

        String output = SerializationUtils
                .prettifyJson(SerializationUtils.toPrettyJson(action.initializeAction()));
        String expectedOutput = Utils
                .readFile(
                        "tests/cloudassembly/extensibility/flowActionWithContentFileTestOutput.json");
        Assert.assertEquals(expectedOutput, output);
    }
}
