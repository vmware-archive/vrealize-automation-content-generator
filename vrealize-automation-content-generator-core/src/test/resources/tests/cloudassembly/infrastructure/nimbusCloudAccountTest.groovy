/*
 * Copyright 2021-2021 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

import com.vmware.devops.model.cloudassembly.infrastructure.NimbusCloudAccount

return NimbusCloudAccount.builder()
        .enabledRegions([NimbusCloudAccount.NimbusRegion.SC, NimbusCloudAccount.NimbusRegion.WDC])
        .build()