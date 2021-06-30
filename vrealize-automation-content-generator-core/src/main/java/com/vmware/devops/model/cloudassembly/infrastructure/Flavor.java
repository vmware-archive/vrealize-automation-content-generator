/*
 * Copyright 2021-2021 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.vmware.devops.model.cloudassembly.infrastructure;

import com.vmware.devops.client.cloudassembly.infrastructure.stubs.InstanceTypeInfo;

public interface Flavor {
    InstanceTypeInfo initializeInstanceTypeInfo();
}
