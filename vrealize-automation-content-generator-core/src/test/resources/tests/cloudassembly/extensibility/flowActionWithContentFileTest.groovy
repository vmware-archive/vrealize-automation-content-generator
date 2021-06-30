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
        .contentPath("tests/cloudassembly/extensibility/basicFlowContent.yaml")
        .build()