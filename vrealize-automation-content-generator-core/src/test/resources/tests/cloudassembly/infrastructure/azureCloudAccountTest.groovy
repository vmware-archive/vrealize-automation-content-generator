/*
 * Copyright 2021-2021 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */


import com.vmware.devops.model.cloudassembly.infrastructure.AzureCloudAccount
import com.vmware.devops.model.cloudassembly.infrastructure.AzureCloudAccount.AzureRegion

return AzureCloudAccount.builder()
        .name("azure-test")
        .subscriptionId("subscription-id")
        .tenantId("tenant-id")
        .clientApplicationId("id")
        .clientApplicationSecretKey("secret")
        .enabledRegions([AzureRegion.EAST_US])
        .build()