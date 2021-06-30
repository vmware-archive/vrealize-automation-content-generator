/*
 * Copyright 2021-2021 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.vmware.devops.config;

import lombok.Data;

import com.vmware.devops.client.cloudassembly.extensibility.stubs.Action.Runtime;

@Data
public class ExtensibilityConfiguration {
    private Runtime defaultActionRuntime = Runtime.PYTHON;
    private int defaultSubscriptionTimeout = 10;
    private int defaultSubscriptionPriority = 10;
}
