/*
 * Copyright 2021-2021 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

import com.vmware.devops.GenerationContext
import com.vmware.devops.Utils
import com.vmware.devops.model.cloudassembly.infrastructure.Project

GenerationContext context = context
context.globalConfiguration.defaultProject = "Generator-Sample"

Properties properties = Utils.readProperties("configuration.properties")

Project.builder()
        .name(context.globalConfiguration.defaultProject)
        .cloudZones([
                properties.get("vc.datacenter").toString()
        ])
        .build()