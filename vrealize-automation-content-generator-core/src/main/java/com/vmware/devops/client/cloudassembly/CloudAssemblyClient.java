/*
 * Copyright 2021-2021 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.vmware.devops.client.cloudassembly;

import lombok.Getter;

import com.vmware.devops.client.cloudassembly.design.DesignClient;
import com.vmware.devops.client.cloudassembly.extensibility.ExtensibilityClient;
import com.vmware.devops.client.cloudassembly.infrastructure.InfrastructureClient;

public class CloudAssemblyClient {

    @Getter
    private DesignClient design;

    @Getter
    private InfrastructureClient infrastructure;

    @Getter
    private ExtensibilityClient extensibility;

    public CloudAssemblyClient(String instance, String accessToken) {
        design = new DesignClient(instance, accessToken);
        infrastructure = new InfrastructureClient(instance, accessToken);
        extensibility = new ExtensibilityClient(instance, accessToken);
    }
}
