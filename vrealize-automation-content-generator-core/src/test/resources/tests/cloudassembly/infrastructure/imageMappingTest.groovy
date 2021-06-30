/*
 * Copyright 2021-2021 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

import com.vmware.devops.GenerationContext
import com.vmware.devops.model.cloudassembly.infrastructure.ImageMapping

GenerationContext context = context
context.cloudAssemblyConfiguration.defaultCloudAccount = "nimbus"

return ImageMapping.builder()
        .name("test")
        .imageMapping([
                "sc": "imageName"
        ])
        .build()