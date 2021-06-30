/*
 * Copyright 2021-2021 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

import com.vmware.devops.model.cloudassembly.infrastructure.VsphereCloudAccount

return VsphereCloudAccount.builder()
        .hostname("my-vc-url")
        .username("administrator@vsphere.local")
        .password("password")
        .datacenters(["Datacenter"])
        .build()