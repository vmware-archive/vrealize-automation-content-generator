/*
 * Copyright 2021-2021 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

import com.vmware.devops.GenerationContext
import com.vmware.devops.client.cloudassembly.extensibility.stubs.Action.Runtime
import com.vmware.devops.model.cloudassembly.extensibility.Action

GenerationContext context = context
context.globalConfiguration.defaultProject = "testProjectName"

return Action.builder()
        .name("test-action")
        .runtime(Runtime.PYTHON)
        .entrypoint("handle")
        .shared(true)
        .contentPath("tests/cloudassembly/extensibility/basicActionScript.py")
        .timeout(60)
        .inputs([
                s: "Hello",
                i: 1,
                b: true
        ])
        .dependencies([
                Action.Dependency.builder()
                        .name("a")
                        .version("1.0.0")
                        .build(),
                Action.Dependency.builder()
                        .name("b")
                        .operation(Action.Dependency.Operation.GREATER_OR_EQUALS)
                        .version("1.1.0")
                        .build()
        ])
        .build()