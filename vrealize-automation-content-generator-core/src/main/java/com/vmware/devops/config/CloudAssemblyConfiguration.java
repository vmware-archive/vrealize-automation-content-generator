/*
 * Copyright 2021-2021 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.vmware.devops.config;

import lombok.Data;

import com.vmware.devops.client.cloudassembly.infrastructure.stubs.Project.PlacementPolicy;

@Data
public class CloudAssemblyConfiguration {
    private String defaultFlavor;
    private PlacementPolicy defaultProjectPlacementPolicy = PlacementPolicy.DEFAULT;
    private String defaultCloudAccount;
}