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
                Optional.of(Stage.builder()
                        .name("stage-1")
                        .tasks([
                                Optional.of((Task) JenkinsTask.builder()
                                        .name("task-1")
                                        .job("job-1")
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
                                ).filter(t -> true).orElse(NoOpTask.INSTANCE),
                                new ParallelTask([
                                        Optional.of((Task) JenkinsTask.builder()
                                                .name("task-1-1")
                                                .job("job-1")
                                                .build()
                                        ).filter(t -> true).orElse(NoOpTask.INSTANCE),
                                        Optional.of((Task) JenkinsTask.builder()
                                                .name("task-2-2")
                                                .job("job-2")
                                                .build()
                                        ).filter(t -> false).orElse(NoOpTask.INSTANCE)
                                ])
                        ])
                        .build()).filter(t -> true).orElse(NoOpStage.INSTANCE),
                Optional.of(Stage.builder()
                        .name("stage-2")
                        .tasks([
                                JenkinsTask.builder()
                                        .name("task-3")
                                        .job("job-3")
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
                        .build()).filter(t -> false).orElse(NoOpStage.INSTANCE)
        ])
        .build()