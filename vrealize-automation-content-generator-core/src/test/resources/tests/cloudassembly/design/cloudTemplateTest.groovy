/*
 * Copyright 2021-2021 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

import com.vmware.devops.GenerationContext
import com.vmware.devops.model.cloudassembly.design.CloudTemplate
import com.vmware.devops.model.cloudassembly.design.CloudTemplate.BlueprintContent
import com.vmware.devops.model.cloudassembly.design.CloudTemplate.BlueprintContent.CloudMachine
import com.vmware.devops.model.cloudassembly.design.CloudTemplate.BlueprintContent.StringInput

GenerationContext context = context
context.globalConfiguration.defaultProject = "testProjectNameDefault"
context.cloudAssemblyConfiguration.defaultFlavor = "default"

return CloudTemplate.builder()
        .name("vrealize-automation-content-generator-test")
        .project("testProjectName")
        .content(BlueprintContent.builder()
                .inputs("hello": StringInput.builder()
                        .defaultValue("world")
                        .build())
                .resources("machine": CloudMachine.builder()
                        .image("test-image")
                        .properties([
                                "test:x": StringInput.builder()
                                        .defaultValue("y")
                                        .build()
                        ])
                        .build())
                .build())
        .build()