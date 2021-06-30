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

return Pipeline.builder()
        .name("test")
        .enabled(true)
        .concurrency(20)
        .stages([
                Stage.builder()
                        .name("stage-1")
                        .tasks([
                                PipelineTask.builder()
                                        .name("task-1")
                                        .inputs([
                                                new Input("localInput", "defaultValue"),
                                                new Input("pipelineInput", "defaultValue", true)
                                        ])
                                        .outputs([
                                                new Output("localKey", "globalKey")
                                        ])
                                        .build(),
                                new ParallelTask([
                                        PipelineTask.builder()
                                                .name("task-2")
                                                .inputs([
                                                        new Input("localInput", "defaultValue"),
                                                        new Input("pipelineInput", "defaultValue", true)
                                                ])
                                                .build(),
                                        RestTask.builder()
                                                .name("task-3")
                                                .agent("agent")
                                                .url("https://vmware.com")
                                                .payload("hello")
                                                .build()
                                ]),
                                RestTask.builder()
                                        .name("task-4")
                                        .agent("agent")
                                        .url("https://vmware.com")
                                        .payload("hello")
                                        .build()
                        ])
                        .build()
        ])
        .build()