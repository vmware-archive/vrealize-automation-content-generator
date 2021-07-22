/*
 * Copyright 2021-2021 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

import com.vmware.devops.client.servicebroker.stubs.Policy
import com.vmware.devops.model.servicebroker.LeasePolicy

return LeasePolicy.builder()
        .name("test-policy")
        .project("testProjectName")
        .enforcementType(Policy.EnforcementType.SOFT)
        .maxTotalLease(128)
        .gracePeriod(3)
        .build()