/*
 * Copyright 2021-2021 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package tests.codestream

import com.vmware.devops.GenerationContext
import com.vmware.devops.model.codestream.*

GenerationContext context = context
context.globalConfiguration.defaultProject = "testProjectName"

return Pipeline.builder()
        .name("test")
        .stages([
                Stage.builder()
                        .name("stage-1")
                        .tasks([
                                PipelineTask.builder()
                                        .name("task-1")
                                        .pipeline("test")
                                        .inputs([
                                                new Input("localInput", "defaultValue"),
                                                new Input("pipelineInput", "defaultValue", true)
                                        ])
                                        .outputs([
                                                new Output("localKey", "globalKey")
                                        ])
                                        .build()
                        ])
                        .build()
        ])
        .build()