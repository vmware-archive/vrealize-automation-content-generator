/*
 * Copyright 2021-2021 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package tests.codestream

import com.vmware.devops.GenerationContext
import com.vmware.devops.client.codestream.stubs.Task
import com.vmware.devops.model.codestream.Output
import com.vmware.devops.model.codestream.Pipeline
import com.vmware.devops.model.codestream.RestTask
import com.vmware.devops.model.codestream.Stage

GenerationContext context = context
context.globalConfiguration.defaultProject = "testProjectName"
context.codestreamConfiguration.defaultAgentEndpoint = "testAgent"


return Pipeline.builder()
        .name("test")
        .stages([
                Stage.builder()
                        .name("stage-1")
                        .tasks([
                                RestTask.builder()
                                        .name("task-1")
                                        .agent("agent")
                                        .url("https://vmware.com")
                                        .payload("hello")
                                        .build(),
                                RestTask.builder()
                                        .name("task-2")
                                        .url("https://vmware.com")
                                        .action(Task.RestInput.RestActions.POST)
                                        .payload([
                                                k1: "v1",
                                                k2: [
                                                        k21: "v1",
                                                        k22: "v2"
                                                ]
                                        ])
                                        .outputs([
                                                new Output("localKey", "globalKey")
                                        ])
                                        .build()
                        ])
                        .build()
        ])
        .build()