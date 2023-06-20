/*
 * Copyright 2021-2021 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.vmware.devops.codestream;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import com.vmware.devops.ReverseGenerationContext;
import com.vmware.devops.TestUtils;
import com.vmware.devops.Utils;
import com.vmware.devops.client.codestream.stubs.CloudProxy;
import com.vmware.devops.client.codestream.stubs.Notification;
import com.vmware.devops.client.codestream.stubs.Notification.Event;
import com.vmware.devops.client.codestream.stubs.Notification.WebhookNotificaton.WebhookAction;
import com.vmware.devops.client.codestream.stubs.Stage;
import com.vmware.devops.client.codestream.stubs.Task;
import com.vmware.devops.client.codestream.stubs.Task.ConditionInput;
import com.vmware.devops.client.codestream.stubs.Task.EndpointKeys;
import com.vmware.devops.client.codestream.stubs.Task.JenkinsInput;
import com.vmware.devops.client.codestream.stubs.Task.PipelineInput;
import com.vmware.devops.client.codestream.stubs.Task.PollInput;
import com.vmware.devops.client.codestream.stubs.Task.RestInput;
import com.vmware.devops.client.codestream.stubs.Task.RestInput.RestActions;
import com.vmware.devops.client.codestream.stubs.Task.SshInput;
import com.vmware.devops.client.codestream.stubs.Task.Type;
import com.vmware.devops.client.codestream.stubs.Task.UserOperationInput;
import com.vmware.devops.model.VraExportedData;
import com.vmware.devops.model.codestream.Pipeline;
import com.vmware.devops.model.codestream.PollTask;

public class PipelineReverseGenerationTest {

    @Test
    public void test() throws IOException {
        File outputDir = TestUtils.createTempDir();

        String sshScript1 = "Hello world 1";
        String sshScript2 = "Hello world 2";

        CloudProxy cloudProxy = CloudProxy.builder()
                .id("proxy-id")
                .customProperties(Map.of(
                        CloudProxy.PROXY_NAME_KEY, "cloudProxyName")
                )
                .build();

        com.vmware.devops.client.codestream.stubs.Pipeline pipeline = com.vmware.devops.client.codestream.stubs.Pipeline
                .builder()
                .enabled(false)
                .project("test-project")
                .name("test")
                .stageOrder(List.of(
                        "Stage1",
                        "Stage2"
                ))
                .input(Map.of(
                        "k1", "v1",
                        "k2", "v2"
                        ))
                .description("description")
                .concurrency(3)
                .output(Map.of(
                        "k1", "v1",
                        "k2", "v2"
                ))
                .stages(Map.of(
                        "Stage2", Stage.builder()
                                .tasks(Map.of(
                                        "jenkins", Task.builder()
                                                .type(Type.JENKINS)
                                                .endpoints(Map.of(
                                                        EndpointKeys.JENKINS_SERVER, "jenkins"
                                                ))
                                                .input(JenkinsInput.builder()
                                                        .parameters(Map.of(
                                                                "k1", "v1",
                                                                "k2", "v2"
                                                        ))
                                                        .job("job")
                                                        .jobFolder("job-folder")
                                                        .build()
                                                )
                                                .preCondition("pre-condition")
                                                .ignoreFailure(false)
                                                .build(),
                                        "rest", Task.builder()
                                                .type(Type.REST)
                                                .ignoreFailure(true)
                                                .preCondition("pre-condition")
                                                .input(RestInput.builder()
                                                        .payload("payload")
                                                        .headers(Map.of(
                                                                "k1", "v1",
                                                                "k2", "v2"
                                                        ))
                                                        .url("url")
                                                        .action(RestActions.POST)
                                                        .build()
                                                )
                                                .endpoints(Map.of(
                                                        EndpointKeys.AGENT, "agent"
                                                ))
                                                .build(),
                                        "pipeline", Task.builder()
                                                .type(Type.PIPELINE)
                                                .ignoreFailure(true)
                                                .preCondition("pre-condition")
                                                .input(PipelineInput.builder()
                                                        .inputProperties(Map.of(
                                                                "k1", "v1",
                                                                "k2", "v2"
                                                        ))
                                                        .pipeline("pipeline")
                                                        .build()
                                                )
                                                .build(),
                                        "user-operation", Task.builder()
                                                .type(Type.USER_OPERATION)
                                                .input(UserOperationInput.builder()
                                                        .approvers(List.of(
                                                                "approver1",
                                                                "approver2"
                                                        ))
                                                        .summary("summary")
                                                        .expirationInDays(3)
                                                        .expiration(3)
                                                        .expirationUnit("DAYS")
                                                        .description("description")
                                                        .sendemail(true)
                                                        .build()
                                                )
                                                .endpoints(Map.of(
                                                        EndpointKeys.EMAIL_SERVER, "email"
                                                ))
                                                .preCondition("pre-condition")
                                                .ignoreFailure(false)
                                                .build(),
                                        "condition", Task.builder()
                                                .type(Type.CONDITION)
                                                .ignoreFailure(false)
                                                .preCondition("pre-condition")
                                                .input(ConditionInput.builder()
                                                        .condition("condition")
                                                        .build()
                                                )
                                                .build(),
                                        "poll", Task.builder()
                                                .preCondition("pre-condition")
                                                .type(Type.POLL)
                                                .input(PollInput.builder()
                                                        .exitCriteria(Map.of(
                                                                PollTask.EXIT_CRITERIA_FAILURE_KEY, "failure",
                                                                PollTask.EXIT_CRITERIA_SUCCESS_KEY, "success"
                                                        ))
                                                        .headers(Map.of(
                                                                "k1", "v1",
                                                                "k2", "v2"
                                                        ))
                                                        .ignoreFailure(false)
                                                        .pollCount(3)
                                                        .pollIntervalSeconds(5)
                                                        .url("url")
                                                        .build()
                                                )
                                                .endpoints(Map.of(
                                                        EndpointKeys.AGENT, "agent"
                                                ))
                                                .build()
                                ))
                                .taskOrder(List.of(
                                        "jenkins",
                                        "poll",
                                        "rest",
                                        "user-operation,condition",
                                        "pipeline"
                                ))
                                .build(),
                        "Stage1", Stage.builder()
                                .tasks(Map.of(
                                        "ssh1", Task.builder()
                                                .type(Type.SSH)
                                                .endpoints(Map.of(
                                                        EndpointKeys.AGENT, "agent"
                                                ))
                                                .input(SshInput.builder()
                                                        .username("username")
                                                        .password("password")
                                                        .environmentVariables(Map.of(
                                                                "k1", "v1",
                                                                "k2", "v2"
                                                        ))
                                                        .arguments(List.of(
                                                                "arg1",
                                                                "arg2"
                                                        ))
                                                        .workingDirectory("dir")
                                                        .script(sshScript1)
                                                        .host("host")
                                                        .build()
                                                )
                                                .preCondition("pre-condition")
                                                .ignoreFailure(false)
                                                .build(),
                                        "ssh2", Task.builder()
                                                .type(Type.SSH)
                                                .endpoints(Map.of(
                                                        EndpointKeys.AGENT, "agent"
                                                ))
                                                .input(SshInput.builder()
                                                        .username("username")
                                                        .privatekey("privateKey")
                                                        .passphrase("passphrase")
                                                        .environmentVariables(Map.of(
                                                                "k1", "v1",
                                                                "k2", "v2"
                                                        ))
                                                        .arguments(List.of(
                                                                "arg1",
                                                                "arg2"
                                                        ))
                                                        .workingDirectory("dir")
                                                        .script(sshScript2)
                                                        .host("host")
                                                        .build()
                                                )
                                                .preCondition("pre-condition")
                                                .ignoreFailure(false)
                                                .build()
                                ))
                                .taskOrder(List.of(
                                        "ssh1",
                                        "ssh2"
                                ))
                                .build()
                ))
                .notifications(Map.of(
                        Notification.Type.JIRA, List.of(
                                Notification.JiraNotification.builder()
                                        .action("action")
                                        .assignee("assignee")
                                        .description("description")
                                        .endpoint("endpoint")
                                        .event(Event.SUCCESS)
                                        .issuetype("issue-type")
                                        .project("project")
                                        .summary("summary")
                                        .build()
                        ),
                        Notification.Type.EMAIL, List.of(
                                Notification.EmailNotification.builder()
                                        .body("body")
                                        .endpoint("endpoint")
                                        .event(Event.FAILURE)
                                        .subject("subject")
                                        .to(List.of(
                                                "to1",
                                                "to2"
                                        ))
                                        .build()
                        ),
                        Notification.Type.WEBHOOK, List.of(
                                Notification.WebhookNotificaton.WebhookNotificaton.builder()
                                        .action(WebhookAction.POST)
                                        .cloudProxyId(cloudProxy.getId())
                                        .event(Event.STARTED)
                                        .headers(Map.of(
                                                "k1", "v1",
                                                "k2", "v2"
                                        ))
                                        .payload("payload")
                                        .url("url")
                                        .build()
                        )
                ))
                .build();

        VraExportedData vraExportedData = new VraExportedData();
        vraExportedData.setPipelines(List.of(
                pipeline
        ));
        vraExportedData.setCloudProxies(List.of(
                cloudProxy
        ));
        ReverseGenerationContext.getInstance().setVraExportedData(vraExportedData);
        ReverseGenerationContext.getInstance().setOutputDir(outputDir.getAbsolutePath());

        new Pipeline().dumpAll();

        String output = Utils
                .readFile(new File(outputDir, "test-pipeline.groovy").getAbsolutePath());
        String expectedOutput = Utils
                .readFile(
                        "tests/codestream/pipelineReverseGenerateTestOutput.test.groovy");
        Assert.assertEquals(expectedOutput, output);

        String sshScriptOutput = Utils
                .readFile(new File(outputDir, "test-ssh1-task-script.sh").getAbsolutePath());
        Assert.assertEquals(sshScript1, sshScriptOutput);

        sshScriptOutput = Utils
                .readFile(new File(outputDir, "test-ssh2-task-script.sh").getAbsolutePath());
        Assert.assertEquals(sshScript2, sshScriptOutput);
    }
}
