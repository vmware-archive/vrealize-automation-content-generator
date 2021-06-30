/*
 * Copyright 2021-2021 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package tests.codestream

import com.vmware.devops.GenerationContext
import com.vmware.devops.client.codestream.stubs.GerritTrigger.Configuration.EventType
import com.vmware.devops.model.codestream.*
import com.vmware.devops.model.codestream.GerritTrigger.Configuration

GenerationContext context = context
context.globalConfiguration.defaultProject = "testProjectName"
context.codestreamConfiguration.defaultJenkinsEndpoint = "my-jenkins-endpoint"
context.codestreamConfiguration.defaultEmailEndpoint = "my-email-endpoint"

return Pipeline.builder()
        .name("test")
        .enabled(true)
        .concurrency(20)
        .inputs(Input.inputListToMap([
                new Input("a", "b")
        ]))
        .stages([
                Stage.builder()
                        .name("stage-1")
                        .tasks([
                                JenkinsTask.builder()
                                        .name("task-1")
                                        .job("job-1")
                                        .inputs([
                                                new Input("localInput", "defaultValue"),
                                                new Input("pipelineInput", "defaultValue", true),
                                                Input.GerritTriggerInput.gerritBranch()
                                        ])
                                        .outputs([
                                                new Output("localKey", "globalKey")
                                        ])
                                        .build()
                        ])
                        .build()
        ])
        .triggers([
                GerritTrigger.builder()
                        .gerritProject("dummy")
                        .configurations([
                                Configuration.builder()
                                        .eventType(EventType.CHANGE_MERGED)
                                        .build(),
                                Configuration.builder()
                                        .eventType(EventType.CHANGE_MERGED)
                                        .build()
                        ])
                        .build(),
                GerritTrigger.builder()
                        .name("custom-name")
                        .branch("custom-branch")
                        .gerritProject("dummy")
                        .configurations([
                                Configuration.builder()
                                        .eventType(EventType.CHANGE_MERGED)
                                        .build()
                        ])
                        .build()
        ])
        .build()