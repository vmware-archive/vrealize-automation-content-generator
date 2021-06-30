/*
 * Copyright 2021-2021 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

import com.vmware.devops.GenerationContext
import com.vmware.devops.model.cloudassembly.extensibility.Action

GenerationContext context = context
context.globalConfiguration.defaultProject = "testProjectName"

return Action.builder()
        .name("test-flow")
        .flow(Action.Flow.builder()
                .steps([
                        "first" : Action.Flow.ActionStep.builder()
                                .action("predefined")
                                .build(),
                        "second": Action.Flow.SwithcStep.builder()
                                .conditions([
                                        '${x == "y"}': "third"
                                ])
                                .build(),
                        "third" : Action.Flow.ActionStep.builder()
                                .action(
                                        Action.builder()
                                                .entrypoint("handle")
                                                .shared(true)
                                                .contentPath(System.getProperty("java.io.tmpdir") + "/testContent.zip")
                                                .build()
                                )
                                .build()
                ])
                .build())
        .build()