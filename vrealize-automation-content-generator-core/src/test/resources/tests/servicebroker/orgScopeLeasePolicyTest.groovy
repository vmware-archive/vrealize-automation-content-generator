/*
 * Copyright 2021-2021 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

import com.vmware.devops.model.servicebroker.LeasePolicy

return LeasePolicy.builder()
        .name("test-policy")
        .maxLease(14)
        .build()