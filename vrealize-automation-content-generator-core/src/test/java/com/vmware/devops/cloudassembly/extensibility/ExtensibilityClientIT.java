/*
 * Copyright 2021-2021 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.vmware.devops.cloudassembly.extensibility;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.ZipFile;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.vmware.devops.ClientTestBase;
import com.vmware.devops.Utils;
import com.vmware.devops.client.cloudassembly.extensibility.stubs.Action;
import com.vmware.devops.client.cloudassembly.extensibility.stubs.Action.Runtime;
import com.vmware.devops.client.cloudassembly.extensibility.stubs.Action.ScriptSource;
import com.vmware.devops.client.cloudassembly.extensibility.stubs.Action.Type;
import com.vmware.devops.client.cloudassembly.extensibility.stubs.Subscription;
import com.vmware.devops.client.cloudassembly.extensibility.stubs.Subscription.EventTopic;
import com.vmware.devops.client.cloudassembly.extensibility.stubs.Subscription.RunnableType;
import com.vmware.devops.client.cloudassembly.infrastructure.stubs.Project;

public class ExtensibilityClientIT extends ClientTestBase {
    private static Project testProject;

    private List<Action> actions = new ArrayList<Action>();
    private List<Subscription> subscriptions = new ArrayList<Subscription>();

    @BeforeClass
    public static void setup() throws InterruptedException, IOException, URISyntaxException {
        testProject = getInfrastructureClient().createProject(Project.builder()
                .name("vrealize-automation-content-generator-test-" + System.currentTimeMillis())
                .build());
    }

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void createAction() throws InterruptedException, IOException, URISyntaxException {
        String name = "vrealize-automation-content-generator-test-" + System.currentTimeMillis();
        String content = Utils.readFile("tests/cloudassembly/extensibility/basicActionScript.py");
        String entrypoint = "handler";
        Action action = Action.builder()
                .name(name)
                .projectId(testProject.getId())
                .actionType(Type.SCRIPT)
                .scriptSource(ScriptSource.SCRIPT)
                .runtime(Runtime.PYTHON)
                .source(content)
                .entrypoint(entrypoint)
                .build();
        action = getExtensibilityClient().createAction(action);
        actions.add(action);
        Assert.assertEquals(name, action.getName());
        Assert.assertEquals(Type.SCRIPT, action.getActionType());
        Assert.assertEquals(Runtime.PYTHON, action.getRuntime());
        Assert.assertEquals(content, action.getSource());
        Assert.assertEquals(entrypoint, action.getEntrypoint());

        action = getExtensibilityClient().findActionByName(action.getName());
        Assert.assertNotNull(action);
    }

    @Test
    public void createUpdateAction() throws InterruptedException, IOException, URISyntaxException {
        String name = "vrealize-automation-content-generator-test-" + System.currentTimeMillis();
        String content = Utils.readFile("tests/cloudassembly/extensibility/basicActionScript.py");
        Action action = Action.builder()
                .name(name)
                .projectId(testProject.getId())
                .actionType(Type.SCRIPT)
                .scriptSource(ScriptSource.SCRIPT)
                .runtime(Runtime.PYTHON)
                .source(content)
                .entrypoint("handler")
                .build();
        action = getExtensibilityClient().createOrUpdateAction(action);
        actions.add(action);

        action.setEntrypoint("updatedHandler");

        action = getExtensibilityClient().createOrUpdateAction(action);
        Assert.assertEquals("updatedHandler", action.getEntrypoint());
    }

    @Test
    public void testGetAllActions() throws URISyntaxException, IOException, InterruptedException {
        int actionsCount = 10;
        String content = Utils.readFile("tests/cloudassembly/extensibility/basicActionScript.py");
        String entrypoint = "handler";
        for (int i = 0; i < actionsCount; i++) {
            String name = "vrealize-automation-content-generator-test-" + i + "-" + System.currentTimeMillis();
            Action action = Action.builder()
                    .name(name)
                    .projectId(testProject.getId())
                    .actionType(Type.SCRIPT)
                    .scriptSource(ScriptSource.SCRIPT)
                    .runtime(Runtime.PYTHON)
                    .source(content)
                    .entrypoint(entrypoint)
                    .build();
            action = getExtensibilityClient().createAction(action);
            Assert.assertEquals(name, action.getName());
            actions.add(action);
        }
        List<Action> allActions = getExtensibilityClient().getAllActions();
        Assert.assertTrue(allActions.size() >= actionsCount);
        List<String> expectedActionNames = actions.stream()
                .map(action -> action.getName())
                .collect(Collectors.toList());
        List<String> allActionNames = allActions.stream()
                .map(action -> action.getName())
                .collect(Collectors.toList());
        Assert.assertTrue(allActionNames.containsAll(expectedActionNames));
    }

    @Test
    public void testExportAction() throws IOException, InterruptedException, URISyntaxException {
        String name = "vrealize-automation-content-generator-test-" + System.currentTimeMillis();
        String content = Utils.readFile("tests/cloudassembly/extensibility/basicActionScript.py");
        String entrypoint = "handler";
        Action action = Action.builder()
                .name(name)
                .projectId(testProject.getId())
                .actionType(Type.SCRIPT)
                .scriptSource(ScriptSource.SCRIPT)
                .runtime(Runtime.PYTHON)
                .source(content)
                .entrypoint(entrypoint)
                .build();
        action = getExtensibilityClient().createAction(action);
        actions.add(action);
        File outputFile = temporaryFolder.newFile();
        getExtensibilityClient().exportAction(action, outputFile);
        Assert.assertTrue(outputFile.exists());
        Assert.assertTrue(outputFile.length() > 0);
        ZipFile zipfile = new ZipFile(outputFile);
        zipfile.close();
    }

    @Test
    public void createSubscription() throws InterruptedException, IOException, URISyntaxException {
        String name = "vrealize-automation-content-generator-test-" + System.currentTimeMillis();

        String content = Utils.readFile("tests/cloudassembly/extensibility/basicActionScript.py");
        Action action = Action.builder()
                .name(name)
                .projectId(testProject.getId())
                .actionType(Type.SCRIPT)
                .scriptSource(ScriptSource.SCRIPT)
                .runtime(Runtime.PYTHON)
                .source(content)
                .entrypoint("handler")
                .build();
        action = getExtensibilityClient().createOrUpdateAction(action);
        actions.add(action);

        Subscription subscription = Subscription.builder()
                .blocking(true)
                .id(name)
                .type(Subscription.Type.RUNNABLE)
                .name(name)
                .disabled(true)
                .eventTopicId(EventTopic.POST_COMPUTE_PROVISION)
                .runnableId(action.getId())
                .runnableType(RunnableType.ACTION)
                .build();

        getExtensibilityClient().createSubscription(subscription);
        subscriptions.add(subscription);

        subscription = getExtensibilityClient().findSubscriptionByName(name);
        Assert.assertNotNull(subscription.getId());
        Assert.assertEquals(name, subscription.getName());
        Assert.assertEquals(EventTopic.POST_COMPUTE_PROVISION, subscription.getEventTopicId());
        Assert.assertEquals(action.getId(), subscription.getRunnableId());
        Assert.assertEquals(RunnableType.ACTION, subscription.getRunnableType());
    }

    @Test
    public void createOrUpdateSubscription() throws InterruptedException, IOException, URISyntaxException {
        String name = "vrealize-automation-content-generator-test-" + System.currentTimeMillis();

        String content = Utils.readFile("tests/cloudassembly/extensibility/basicActionScript.py");
        Action action = Action.builder()
                .name(name)
                .projectId(testProject.getId())
                .actionType(Type.SCRIPT)
                .scriptSource(ScriptSource.SCRIPT)
                .runtime(Runtime.PYTHON)
                .source(content)
                .entrypoint("handler")
                .build();
        action = getExtensibilityClient().createOrUpdateAction(action);
        actions.add(action);

        Subscription subscription = Subscription.builder()
                .blocking(true)
                .type(Subscription.Type.RUNNABLE)
                .name(name)
                .disabled(true)
                .eventTopicId(EventTopic.POST_COMPUTE_PROVISION)
                .runnableId(action.getId())
                .runnableType(RunnableType.ACTION)
                .build();

        getExtensibilityClient().createOrUpdateSubscription(subscription);
        subscriptions.add(subscription);

        subscription = getExtensibilityClient().findSubscriptionByName(name);
        String id = subscription.getId();

        subscription.setTimeout(13);
        getExtensibilityClient().createOrUpdateSubscription(subscription);
        subscription = getExtensibilityClient().findSubscriptionByName(name);
        Assert.assertEquals(id, subscription.getId());
        Assert.assertEquals(13, subscription.getTimeout());
    }

    @Test
    public void testGetAllSubscriptions() throws IOException, InterruptedException, URISyntaxException {
        int subscriptionsCount = 10;
        String content = Utils.readFile("tests/cloudassembly/extensibility/basicActionScript.py");
        String entrypoint = "handler";
        for (int i = 0; i < subscriptionsCount; i++) {
            String name = "vrealize-automation-content-generator-test-" + i + "-" + System.currentTimeMillis();
            Action action = Action.builder()
                    .name(name)
                    .projectId(testProject.getId())
                    .actionType(Type.SCRIPT)
                    .scriptSource(ScriptSource.SCRIPT)
                    .runtime(Runtime.PYTHON)
                    .source(content)
                    .entrypoint(entrypoint)
                    .build();
            action = getExtensibilityClient().createAction(action);
            Assert.assertEquals(name, action.getName());
            actions.add(action);

            Subscription subscription = Subscription.builder()
                    .blocking(true)
                    .id(name)
                    .type(Subscription.Type.RUNNABLE)
                    .name(name)
                    .disabled(true)
                    .eventTopicId(EventTopic.POST_COMPUTE_PROVISION)
                    .runnableId(action.getId())
                    .runnableType(RunnableType.ACTION)
                    .build();

            getExtensibilityClient().createSubscription(subscription);
            Assert.assertNotNull(getExtensibilityClient().findSubscriptionByName(name));
            subscriptions.add(subscription);
        }
        List<Subscription> allSubscriptions = getExtensibilityClient().getAllSubscriptions();
        Assert.assertTrue(allSubscriptions.size() >= subscriptionsCount);
        List<String> expectedSubscriptionNames = subscriptions.stream()
                .map(subscription -> subscription.getName())
                .collect(Collectors.toList());
        List<String> allSubscriptionNames = allSubscriptions.stream()
                .map(subscription -> subscription.getName())
                .collect(Collectors.toList());
        Assert.assertTrue(allSubscriptionNames.containsAll(expectedSubscriptionNames));
    }

    @After
    public void cleanup() throws InterruptedException, IOException, URISyntaxException {
        if (!subscriptions.isEmpty()) {
            for (Subscription subscription : subscriptions) {
                getExtensibilityClient().deleteSubscription(subscription.getId());
            }
            subscriptions.clear();
        }

        if (!actions.isEmpty()) {
            for (Action action : actions) {
                getExtensibilityClient().deleteAction(action.getSelfLink());
            }
            actions.clear();
        }
    }

    @AfterClass
    public static void classCleanup() throws InterruptedException, IOException, URISyntaxException {
        getInfrastructureClient().deleteProject(testProject.getId());
    }
}
