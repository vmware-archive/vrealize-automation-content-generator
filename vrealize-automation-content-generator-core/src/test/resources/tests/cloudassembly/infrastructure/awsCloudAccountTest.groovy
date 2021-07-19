/*
 * Copyright 2021-2021 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */


import com.vmware.devops.model.cloudassembly.infrastructure.AwsCloudAccount

return AwsCloudAccount.builder()
        .name("aws-test")
        .accessKeyId("key")
        .secretAccessKey("secret")
        .enabledRegions([AwsCloudAccount.AwsRegion.EU_WEST_3])
        .build()