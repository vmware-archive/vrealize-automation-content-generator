/*
 * Copyright 2021-2021 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

import com.vmware.devops.GenerationContext
import com.vmware.devops.model.cloudassembly.infrastructure.FlavorMapping
import com.vmware.devops.model.cloudassembly.infrastructure.NimbusCloudAccount
import com.vmware.devops.model.cloudassembly.infrastructure.VsphereCloudAccount

GenerationContext context = context
context.cloudAssemblyConfiguration.defaultCloudAccount = "nimbus"

return FlavorMapping.builder()
        .name("test")
        .flavorMapping([
                (NimbusCloudAccount.NimbusRegion.SC.id): NimbusCloudAccount.NimbusFlavor.DEFAULT,
                "vc6/Datacenter": VsphereCloudAccount.VsphereFlavor.builder()
                        .cpuCount(1)
                        .memoryMb(1000)
                        .build()
        ])
        .build()