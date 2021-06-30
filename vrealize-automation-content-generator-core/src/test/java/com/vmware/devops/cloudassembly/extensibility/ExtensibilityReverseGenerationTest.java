/*
 * Copyright 2021-2021 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.vmware.devops.cloudassembly.extensibility;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.anyObject;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.junit.Assert;
import org.junit.Test;

import com.vmware.devops.ReverseGenerationContext;
import com.vmware.devops.TestUtils;
import com.vmware.devops.Utils;
import com.vmware.devops.client.Client;
import com.vmware.devops.client.cloudassembly.CloudAssemblyClient;
import com.vmware.devops.client.cloudassembly.extensibility.ExtensibilityClient;
import com.vmware.devops.client.cloudassembly.extensibility.stubs.Action.Runtime;
import com.vmware.devops.client.cloudassembly.extensibility.stubs.Action.ScriptSource;
import com.vmware.devops.client.cloudassembly.extensibility.stubs.Action.Type;
import com.vmware.devops.client.cloudassembly.extensibility.stubs.Subscription.EventTopic;
import com.vmware.devops.client.cloudassembly.extensibility.stubs.Subscription.RunnableType;
import com.vmware.devops.client.cloudassembly.infrastructure.stubs.Project;
import com.vmware.devops.config.EndpointConfiguration;
import com.vmware.devops.model.VraExportedData;
import com.vmware.devops.model.cloudassembly.extensibility.Action;
import com.vmware.devops.model.cloudassembly.extensibility.Subscription;

public class ExtensibilityReverseGenerationTest {

    @Test
    public void basicActionTest() throws Exception {
        File outputDir = TestUtils.createTempDir();
        String scriptSource = "hello world";

        VraExportedData data = new VraExportedData();
        data.setActions(List.of(
                com.vmware.devops.client.cloudassembly.extensibility.stubs.Action.builder()
                        .name("test")
                        .projectId("project-id")
                        .actionType(Type.SCRIPT)
                        .scriptSource(ScriptSource.SCRIPT)
                        .entrypoint("entrypoint")
                        .source(scriptSource)
                        .runtime(Runtime.PYTHON)
                        .shared(true)
                        .inputs(Map.of(
                                "k1", "v1",
                                "k2", true
                        ))
                        .dependencies("x==1.0.0\ny >= 2.0.0")
                        .timeoutSeconds(6)
                        .build()
        ));
        data.setProjects(List.of(
                Project.builder()
                        .id("project-id")
                        .name("test-project")
                        .build()
        ));

        ReverseGenerationContext.getInstance().setVraExportedData(data);
        ReverseGenerationContext.getInstance().setOutputDir(outputDir.getAbsolutePath());

        new Action().dumpAll();

        String output = Utils
                .readFile(
                        new File(outputDir, "test-action.groovy").getAbsolutePath());
        String expectedOutput = Utils
                .readFile(
                        "tests/cloudassembly/extensibility/basicActionReverseGenerateTestOutput.test.groovy");
        Assert.assertEquals(expectedOutput, output);

        File script = new File(outputDir, "test-action-script.py");
        Assert.assertTrue(script.exists());
        Assert.assertEquals(scriptSource, Utils.readFile(script.getAbsolutePath()));
    }

    @Test
    public void pythonPackageActionTest() throws Exception {
        File outputDir = TestUtils.createTempDir();
        String contentResourceName = "hello-world.zip";
        String dummyContent = "hello-world";
        File dummyExportedAction = createDummyExportedAction(dummyContent.getBytes());

        com.vmware.devops.client.cloudassembly.extensibility.stubs.Action action = com.vmware.devops.client.cloudassembly.extensibility.stubs.Action
                .builder()
                .name("test")
                .projectId("project-id")
                .actionType(Type.SCRIPT)
                .scriptSource(ScriptSource.PACKAGE)
                .contentResourceName(contentResourceName)
                .entrypoint("entrypoint")
                .runtime(Runtime.PYTHON)
                .shared(true)
                .timeoutSeconds(6)
                .build();

        VraExportedData data = new VraExportedData();
        data.setActions(List.of(
                action
        ));
        data.setProjects(List.of(
                Project.builder()
                        .id("project-id")
                        .name("test-project")
                        .build()
        ));

        ReverseGenerationContext.getInstance().setVraExportedData(data);
        ReverseGenerationContext.getInstance().setOutputDir(outputDir.getAbsolutePath());

        ExtensibilityClient extensibilityClient = mock(ExtensibilityClient.class);
        doAnswer(invocation -> {
            File output = invocation.getArgumentAt(1, File.class);
            try (FileOutputStream fos = new FileOutputStream(output)) {
                Files.copy(dummyExportedAction.toPath(), fos);
            }
            return null;
        }).when(extensibilityClient).exportAction(eq(action), anyObject());

        CloudAssemblyClient cloudAssemblyClient = mock(CloudAssemblyClient.class);
        doReturn(extensibilityClient).when(cloudAssemblyClient).getExtensibility();

        Client mockClient = mock(Client.class);
        doReturn(cloudAssemblyClient).when(mockClient).getCloudAssembly();

        EndpointConfiguration endpointConfiguration = spy(new EndpointConfiguration());
        doReturn(mockClient).when(endpointConfiguration).getClient();

        ReverseGenerationContext.getInstance().setEndpointConfiguration(endpointConfiguration);

        new Action().dumpAll();

        String output = Utils
                .readFile(
                        new File(outputDir, "test-action.groovy").getAbsolutePath());
        String expectedOutput = Utils
                .readFile(
                        "tests/cloudassembly/extensibility/pythonPackageActionReverseGenerateTestOutput.test.groovy");
        Assert.assertEquals(expectedOutput, output);

        File packageFile = new File(outputDir, contentResourceName);
        Assert.assertTrue(packageFile.exists());
        Assert.assertEquals(dummyContent, Utils.readFile(packageFile.getAbsolutePath()));
    }

    @Test
    public void flowActionTest() throws Exception {
        File outputDir = TestUtils.createTempDir();
        String scriptSource = Utils
                .readFile("tests/cloudassembly/extensibility/basicFlowContent.yaml");

        VraExportedData data = new VraExportedData();
        data.setActions(List.of(
                com.vmware.devops.client.cloudassembly.extensibility.stubs.Action.builder()
                        .name("test")
                        .projectId("project-id")
                        .actionType(Type.FLOW)
                        .source(scriptSource)
                        .shared(false)
                        .timeoutSeconds(13)
                        .build()
        ));
        data.setProjects(List.of(
                Project.builder()
                        .name("test-project")
                        .id("project-id")
                        .build()
        ));

        ReverseGenerationContext.getInstance().setVraExportedData(data);
        ReverseGenerationContext.getInstance().setOutputDir(outputDir.getAbsolutePath());

        new Action().dumpAll();

        String output = Utils
                .readFile(
                        new File(outputDir, "test-action.groovy").getAbsolutePath());
        String expectedOutput = Utils
                .readFile(
                        "tests/cloudassembly/extensibility/flowActionReverseGenerateTestOutput.test.groovy");
        Assert.assertEquals(expectedOutput, output);

        File script = new File(outputDir, "test-action-flow.yaml");
        Assert.assertTrue(script.exists());
        Assert.assertEquals(scriptSource, Utils.readFile(script.getAbsolutePath()));
    }

    @Test
    public void subscriptionTest() throws Exception {
        File outputDir = TestUtils.createTempDir();
        VraExportedData data = new VraExportedData();
        data.setSubscriptions(List.of(
                com.vmware.devops.client.cloudassembly.extensibility.stubs.Subscription
                        .builder()
                        .name("test")
                        .runnableType(RunnableType.ACTION)
                        .runnableId("runnable-id")
                        .recoverRunnableType(RunnableType.ACTION)
                        .recoverRunnableId("recover-runnable-id")
                        .eventTopicId(EventTopic.POST_COMPUTE_PROVISION)
                        .blocking(true)
                        .criteria(
                                "test.input == \"some-value\" && test.otherInput != \"some-other-value\"")
                        .build()
        ));
        data.setActions(List.of(
                com.vmware.devops.client.cloudassembly.extensibility.stubs.Action.builder()
                        .name("test-action")
                        .id("runnable-id")
                        .build(),
                com.vmware.devops.client.cloudassembly.extensibility.stubs.Action.builder()
                        .name("test-recover-action")
                        .id("recover-runnable-id")
                        .build()
        ));

        ReverseGenerationContext.getInstance().setVraExportedData(data);
        ReverseGenerationContext.getInstance().setOutputDir(outputDir.getAbsolutePath());

        new Subscription().dumpAll();
        String output = Utils
                .readFile(
                        new File(outputDir, "600-test-subscription.groovy").getAbsolutePath());
        String expectedOutput = Utils
                .readFile(
                        "tests/cloudassembly/extensibility/subscriptionReverseGenerateTestOutput.test.groovy");
        Assert.assertEquals(expectedOutput, output);
    }

    private File createDummyExportedAction(byte[] dummyData) throws IOException {
        File output = File.createTempFile("test-exported-action", ".zip");
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(output))) {
            ZipEntry entry = new ZipEntry("package.zip");
            zos.putNextEntry(entry);
            zos.write(dummyData);
        }
        return output;
    }
}
