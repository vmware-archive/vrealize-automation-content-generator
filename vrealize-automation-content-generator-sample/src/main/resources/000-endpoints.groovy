/*
 * Copyright 2021-2021 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

import com.vmware.devops.GenerationContext
import com.vmware.devops.Utils
import com.vmware.devops.model.cloudassembly.infrastructure.FlavorMapping
import com.vmware.devops.model.cloudassembly.infrastructure.VsphereCloudAccount

GenerationContext context = context
context.cloudAssemblyConfiguration.defaultCloudAccount = "sample-vc"
context.cloudAssemblyConfiguration.defaultFlavor = "sample-flavor"

Properties properties = Utils.readProperties("configuration.properties")

String datacenter = properties.get("vc.datacenter")

return [
        VsphereCloudAccount.builder()
                .name(context.cloudAssemblyConfiguration.defaultCloudAccount)
                .hostname(properties.get("vc.hostname").toString())
                .username(properties.get("vc.username").toString())
                .password(properties.get("vc.password").toString())
                .datacenters([
                        datacenter
                ])
                .build(),
        FlavorMapping.builder()
                .name(context.cloudAssemblyConfiguration.defaultFlavor)
                .flavorMapping([
                        (datacenter): VsphereCloudAccount.VsphereFlavor.builder()
                                .cpuCount(4)
                                .memoryMb(4000)
                                .build()
                ])
                .build()
]