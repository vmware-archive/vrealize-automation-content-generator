/*
 * Copyright 2021-2021 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.vmware.devops;

import java.io.IOException;
import java.net.URISyntaxException;

import com.vmware.devops.client.Client;
import com.vmware.devops.client.cloudassembly.design.DesignClient;
import com.vmware.devops.client.cloudassembly.extensibility.ExtensibilityClient;
import com.vmware.devops.client.cloudassembly.infrastructure.InfrastructureClient;
import com.vmware.devops.client.codestream.CodestreamClient;

public class ClientTestBase {
    private static Client client;

    protected static synchronized Client getClient()
            throws IOException, URISyntaxException, InterruptedException {
        if (client == null) {
            String instance = TestUtils.getProperty("vra.instance");
            String loginInstance = TestUtils.getProperty("vra.loginInstance");
            String username = TestUtils.getProperty("vra.username");
            String password = TestUtils.getProperty("vra.password");
            String refreshToken = TestUtils.getProperty("vra.refreshToken");

            if (refreshToken != null && !refreshToken.trim().equals("")) {
                client = new Client(loginInstance, instance, refreshToken);
            } else {
                client = new Client(loginInstance, instance, username, password);
            }
        }

        return client;
    }

    protected static CodestreamClient getCodestreamClient()
            throws InterruptedException, IOException, URISyntaxException {
        return getClient().getCodestream();
    }

    protected static DesignClient getDesignClient()
            throws InterruptedException, IOException, URISyntaxException {
        return getClient().getCloudAssembly().getDesign();
    }

    protected static InfrastructureClient getInfrastructureClient()
            throws InterruptedException, IOException, URISyntaxException {
        return getClient().getCloudAssembly().getInfrastructure();
    }

    protected static ExtensibilityClient getExtensibilityClient()
            throws InterruptedException, IOException, URISyntaxException {
        return getClient().getCloudAssembly().getExtensibility();
    }
}
