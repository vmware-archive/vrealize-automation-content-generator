/*
 * Copyright 2021-2021 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

import com.vmware.devops.GenerationContext
import com.vmware.devops.client.cloudassembly.infrastructure.stubs.ProjectPrincipal
import com.vmware.devops.model.cloudassembly.infrastructure.Project

GenerationContext context = context
context.cloudAssemblyConfiguration.defaultCloudAccount = "nimbus"
context.cloudAssemblyConfiguration.defaultProjectPlacementPolicy =
        com.vmware.devops.client.cloudassembly.infrastructure.stubs.Project.PlacementPolicy.SPREAD

return Project.builder()
        .name("test")
        .users([
                Project.UserGroup.builder()
                        .email("test@vmware.com")
                        .role(ProjectPrincipal.Role.ADMINISTRATOR)
                        .build()
        ])
        .groups([
                Project.UserGroup.builder()
                        .email(Project.UserGroup.ALL_USERS_GROUP)
                        .role(ProjectPrincipal.Role.VIEWER)
                        .build()
        ])
        .cloudZones([
                "sc"
        ])
        .build()