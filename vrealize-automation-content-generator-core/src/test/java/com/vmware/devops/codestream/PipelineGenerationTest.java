/*
 * Copyright 2021-2021 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.vmware.devops.codestream;

import java.io.IOException;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.vmware.devops.GenerationContext;
import com.vmware.devops.GenerationTestBase;
import com.vmware.devops.SerializationUtils;
import com.vmware.devops.SpecProcessor;
import com.vmware.devops.Utils;
import com.vmware.devops.model.codestream.GerritTrigger;
import com.vmware.devops.model.codestream.Pipeline;
import com.vmware.devops.model.codestream.Variable;

public class PipelineGenerationTest extends GenerationTestBase {

    @Test
    public void jenkinsTest() throws IOException {
        SpecProcessor specProcessor = new SpecProcessor();
        GenerationContext.getInstance().getGlobalConfiguration()
                .setDefaultProject("defaultTestProjectName");
        Pipeline p = (Pipeline) specProcessor
                .process(Utils.readFile("tests/codestream/jenkinsTest.groovy"));
        Assert.assertEquals("testProjectName",
                GenerationContext.getInstance().getGlobalConfiguration().getDefaultProject());

        String output = SerializationUtils
                .prettifyJson(SerializationUtils.toPrettyJson(p.initializePipeline()));
        String expectedOutput = Utils
                .readFile("tests/codestream/jenkinsTestOutput.json");
        Assert.assertEquals(expectedOutput, output);
    }

    @Test
    public void jenkinsMultibranchTest() throws IOException {
        SpecProcessor specProcessor = new SpecProcessor();
        GenerationContext.getInstance().getGlobalConfiguration()
                .setDefaultProject("defaultTestProjectName");
        Pipeline p = (Pipeline) specProcessor
                .process(Utils.readFile("tests/codestream/jenkinsMultibranchTest.groovy"));
        Assert.assertEquals("testProjectName",
                GenerationContext.getInstance().getGlobalConfiguration().getDefaultProject());

        String output = SerializationUtils
                .prettifyJson(SerializationUtils.toPrettyJson(p.initializePipeline()));
        String expectedOutput = Utils
                .readFile("tests/codestream/jenkinsMultibranchOutput.json");
        Assert.assertEquals(expectedOutput, output);
    }

    @Test
    public void userOperationTest() throws IOException {
        SpecProcessor specProcessor = new SpecProcessor();
        GenerationContext.getInstance().getGlobalConfiguration()
                .setDefaultProject("defaultTestProjectName");
        Pipeline p = (Pipeline) specProcessor
                .process(
                        Utils.readFile("tests/codestream/userOperationTest.groovy"));
        Assert.assertEquals("testProjectName",
                GenerationContext.getInstance().getGlobalConfiguration().getDefaultProject());

        String output = SerializationUtils
                .prettifyJson(SerializationUtils.toPrettyJson(p.initializePipeline()));
        String expectedOutput = Utils
                .readFile("tests/codestream/userOperationTestOutput.json");
        Assert.assertEquals(expectedOutput, output);
    }

    @Test
    public void conditionTest() throws IOException {
        SpecProcessor specProcessor = new SpecProcessor();
        GenerationContext.getInstance().getGlobalConfiguration()
                .setDefaultProject("defaultTestProjectName");
        Pipeline p = (Pipeline) specProcessor
                .process(
                        Utils.readFile("tests/codestream/conditionTest.groovy"));
        Assert.assertEquals("testProjectName",
                GenerationContext.getInstance().getGlobalConfiguration().getDefaultProject());

        String output = SerializationUtils
                .prettifyJson(SerializationUtils.toPrettyJson(p.initializePipeline()));
        String expectedOutput = Utils
                .readFile("tests/codestream/conditionTestOutput.json");
        Assert.assertEquals(expectedOutput, output);
    }

    @Test
    public void pipelineTest() throws IOException {
        SpecProcessor specProcessor = new SpecProcessor();
        GenerationContext.getInstance().getGlobalConfiguration()
                .setDefaultProject("defaultTestProjectName");
        Pipeline p = (Pipeline) specProcessor
                .process(Utils.readFile("tests/codestream/pipelineTest.groovy"));
        Assert.assertEquals("testProjectName",
                GenerationContext.getInstance().getGlobalConfiguration().getDefaultProject());

        String output = SerializationUtils
                .prettifyJson(SerializationUtils.toPrettyJson(p.initializePipeline()));
        String expectedOutput = Utils
                .readFile("tests/codestream/pipelineTestOutput.json");
        Assert.assertEquals(expectedOutput, output);
    }

    @Test
    public void pipelineNotificationTest() throws IOException {
        SpecProcessor specProcessor = new SpecProcessor();
        GenerationContext.getInstance().getGlobalConfiguration()
                .setDefaultProject("defaultTestProjectName");
        Pipeline p = (Pipeline) specProcessor
                .process(Utils.readFile("tests/codestream/pipelineNotificationTest.groovy"));
        Assert.assertEquals("testProjectName",
                GenerationContext.getInstance().getGlobalConfiguration().getDefaultProject());

        String output = SerializationUtils
                .prettifyJson(SerializationUtils.toPrettyJson(p.initializePipeline()));
        String expectedOutput = Utils
                .readFile("tests/codestream/pipelineNotificationTestOutput.json");
        Assert.assertEquals(expectedOutput, output);
    }

    @Test
    public void sshTest() throws IOException {
        SpecProcessor specProcessor = new SpecProcessor();
        GenerationContext.getInstance().getGlobalConfiguration()
                .setDefaultProject("defaultTestProjectName");
        Pipeline p = (Pipeline) specProcessor
                .process(Utils.readFile("tests/codestream/sshTest.groovy"));
        List<Variable> variables = p.processInPlaceTaskVariables();

        Assert.assertEquals("testProjectName",
                GenerationContext.getInstance().getGlobalConfiguration().getDefaultProject());
        Assert.assertEquals("testAgent",
                GenerationContext.getInstance().getCodestreamConfiguration()
                        .getDefaultAgentEndpoint());

        String output = SerializationUtils
                .prettifyJson(SerializationUtils.toPrettyJson(p.initializePipeline()));
        String expectedOutput = Utils
                .readFile("tests/codestream/sshTestOutput.json");
        Assert.assertEquals(expectedOutput, output);

        Assert.assertEquals("test-stage-1-task-1-ssh-task-password", variables.get(0).getName());
        Assert.assertEquals("password", variables.get(0).getValue());
        Assert.assertEquals("test-stage-1-task-2-ssh-task-pass-phrase", variables.get(1).getName());
        Assert.assertEquals("passphrase", variables.get(1).getValue());
    }

    @Test
    public void restTest() throws IOException {
        SpecProcessor specProcessor = new SpecProcessor();
        GenerationContext.getInstance().getGlobalConfiguration()
                .setDefaultProject("restTestProjectName");
        Pipeline p = (Pipeline) specProcessor
                .process(Utils.readFile("tests/codestream/restTest.groovy"));
        Assert.assertEquals("testProjectName",
                GenerationContext.getInstance().getGlobalConfiguration().getDefaultProject());
        Assert.assertEquals("testAgent",
                GenerationContext.getInstance().getCodestreamConfiguration()
                        .getDefaultAgentEndpoint());

        String output = SerializationUtils
                .prettifyJson(SerializationUtils.toPrettyJson(p.initializePipeline()));
        String expectedOutput = Utils
                .readFile("tests/codestream/restTestOutput.json");
        Assert.assertEquals(expectedOutput, output);
    }

    @Test
    public void pollTest() throws IOException {
        SpecProcessor specProcessor = new SpecProcessor();
        GenerationContext.getInstance().getGlobalConfiguration()
                .setDefaultProject("pollTestProjectName");
        Pipeline p = (Pipeline) specProcessor
                .process(Utils.readFile("tests/codestream/pollTest.groovy"));
        Assert.assertEquals("testProjectName",
                GenerationContext.getInstance().getGlobalConfiguration().getDefaultProject());
        Assert.assertEquals("testAgent",
                GenerationContext.getInstance().getCodestreamConfiguration()
                        .getDefaultAgentEndpoint());

        String output = SerializationUtils
                .prettifyJson(SerializationUtils.toPrettyJson(p.initializePipeline()));
        String expectedOutput = Utils
                .readFile("tests/codestream/pollTestOutput.json");
        Assert.assertEquals(expectedOutput, output);
    }

    @Test
    public void parallelTest() throws IOException {
        SpecProcessor specProcessor = new SpecProcessor();
        GenerationContext.getInstance().getGlobalConfiguration()
                .setDefaultProject("restTestProjectName");
        Pipeline p = (Pipeline) specProcessor
                .process(Utils.readFile("tests/codestream/parallelTest.groovy"));

        String output = SerializationUtils
                .prettifyJson(SerializationUtils.toPrettyJson(p.initializePipeline()));
        String expectedOutput = Utils
                .readFile("tests/codestream/parallelTestOutput.json");
        Assert.assertEquals(expectedOutput, output);
    }

    @Test
    public void inlineTriggersTest() throws IOException {
        SpecProcessor specProcessor = new SpecProcessor();
        GenerationContext.getInstance().getGlobalConfiguration()
                .setDefaultProject("defaultTestProjectName");
        Pipeline p = (Pipeline) specProcessor
                .process(Utils.readFile("tests/codestream/inlineTriggersTest.groovy"));
        Assert.assertEquals("testProjectName",
                GenerationContext.getInstance().getGlobalConfiguration().getDefaultProject());

        com.vmware.devops.client.codestream.stubs.Pipeline pipeline = p.initializePipeline();
        p.expandTriggers(pipeline);

        Assert.assertEquals("test-gerrit-trigger",
                ((GerritTrigger) p.getTriggers().get(0)).getName());
        Assert.assertEquals("master",
                ((GerritTrigger) p.getTriggers().get(0)).getBranch());
        Assert.assertEquals("testProjectName",
                ((GerritTrigger) p.getTriggers().get(0)).getProject());
        Assert.assertEquals("test",
                ((GerritTrigger) p.getTriggers().get(0)).getConfigurations().get(0).getPipeline());
        Assert.assertEquals("test",
                ((GerritTrigger) p.getTriggers().get(0)).getConfigurations().get(1).getPipeline());
        Assert.assertEquals("defaultValue",
                ((GerritTrigger) p.getTriggers().get(0)).getConfigurations().get(0).getInput()
                        .get("pipelineInput"));
        Assert.assertEquals("defaultValue",
                ((GerritTrigger) p.getTriggers().get(1)).getConfigurations().get(0).getInput()
                        .get("pipelineInput"));
        Assert.assertEquals("b",
                ((GerritTrigger) p.getTriggers().get(0)).getConfigurations().get(0).getInput()
                        .get("a"));
        Assert.assertEquals("", pipeline.getInput().get("GERRIT_BRANCH"));

        Assert.assertEquals("test-custom-name",
                ((GerritTrigger) p.getTriggers().get(1)).getName());
        Assert.assertEquals("custom-branch",
                ((GerritTrigger) p.getTriggers().get(1)).getBranch());
    }

    @Test
    public void noOpTest() throws IOException {
        SpecProcessor specProcessor = new SpecProcessor();
        Pipeline p = (Pipeline) specProcessor
                .process(Utils.readFile("tests/codestream/noOpTest.groovy"));

        String output = SerializationUtils
                .prettifyJson(SerializationUtils.toPrettyJson(p.initializePipeline()));
        String expectedOutput = Utils
                .readFile("tests/codestream/noOpTestOutput.json");
        Assert.assertEquals(expectedOutput, output);
    }
}
