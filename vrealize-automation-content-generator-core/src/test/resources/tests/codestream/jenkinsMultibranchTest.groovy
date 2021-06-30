/*
 * Copyright 2021-2021 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */
package tests.codestream

import com.vmware.devops.GenerationContext
import com.vmware.devops.model.codestream.*

GenerationContext context = context
context.globalConfiguration.defaultProject = "testProjectName"
context.codestreamConfiguration.defaultJenkinsEndpoint = "my-jenkins-endpoint"
context.codestreamConfiguration.defaultEmailEndpoint = "my-email-endpoint"

return Pipeline.builder()
        .name("test")
        .enabled(true)
        .concurrency(20)
        .stages([
                Stage.builder()
                        .name("stage-1")
                        .tasks([
                                JenkinsTask.builder()
                                        .name("task-1")
                                        .job("job-1")
                                        .jobFolder("job-folder-1")
                                        .inputs([
                                                new Input("localInput", "defaultValue"),
                                                new Input("pipelineInput", "defaultValue", true),
                                                new Input("pipelineInput2", Input.builder()
                                                        .value("defaultValue2")
                                                        .build()),
                                                new Input("anotherPipelineInput", new Input("globalPipelineInput", "globalPipelineInputValue"))
                                        ])
                                        .outputs([
                                                new Output("localKey", "globalKey")
                                        ])
                                        .build()
                        ])
                        .build()
        ])
        .build()