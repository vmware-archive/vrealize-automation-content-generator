/*
 * Copyright 2021-2021 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package tests.codestream

import com.vmware.devops.GenerationContext
import com.vmware.devops.model.codestream.*

GenerationContext context = context
context.globalConfiguration.defaultProject = "testProjectName"
context.globalConfiguration.defaultCloudProxy = "testCloudProxy"
context.codestreamConfiguration.defaultEmailEndpoint = "my-email-endpoint"
context.codestreamConfiguration.defaultJiraEndpoint = "my-jira-endpoint"

return Pipeline.builder()
        .name("test")
        .stages([
                Stage.builder()
                        .name("stage-1")
                        .tasks([
                                PipelineTask.builder()
                                        .name("task-1")
                                        .pipeline("test")
                                        .build()
                        ])
                        .build()
        ])
        .notifications([
                EmailNotification.builder()
                        .body("Test Body")
                        .subject("Test Subject")
                        .emailTo(["test@vmware.com", "test1@vmware.com"])
                        .build(),
                JiraNotification.builder()
                        .event(com.vmware.devops.client.codestream.stubs.Notification.Event.FAILURE)
                        .project("VDEVOPS")
                        .description("testDescription")
                        .assignee("user@vmware.com")
                        .issuetype("bug")
                        .summary("testSummary")
                        .build(),
                WebhookNotification.builder()
                        .payload("testPayload")
                        .url("https://test.com")
                        .build()

        ])
        .build()