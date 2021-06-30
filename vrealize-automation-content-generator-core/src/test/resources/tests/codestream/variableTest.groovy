/*
 * Copyright 2021-2021 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package tests.codestream

import com.vmware.devops.GenerationContext
import com.vmware.devops.model.codestream.Variable

GenerationContext context = context
context.globalConfiguration.defaultProject = "testProjectName"

return Variable.builder()
    .name("test-vrealize-automation-content-generator-var")
    .value("test-vrealize-automation-content-generator-var-value")
    .build()