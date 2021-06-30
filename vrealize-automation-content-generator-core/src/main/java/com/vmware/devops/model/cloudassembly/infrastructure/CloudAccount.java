/*
 * Copyright 2021-2021 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.vmware.devops.model.cloudassembly.infrastructure;

import com.vmware.devops.GenerationContext;
import com.vmware.devops.client.Client;
import com.vmware.devops.client.cloudassembly.infrastructure.stubs.Endpoint;
import com.vmware.devops.client.cloudassembly.infrastructure.stubs.EndpointRegions;
import com.vmware.devops.model.GenerationEntity;

public abstract class CloudAccount
        implements GenerationEntity {
    @Override
    public void generate() throws Exception {
        Endpoint endpoint = getEndpoint();
        EndpointRegions regions = getEndpointRegions();

        Client client = GenerationContext.getInstance()
                .getEndpointConfiguration().getClient();
        endpoint = client.getCloudAssembly().getInfrastructure()
                .createOrUpdateEndpoint(endpoint);
        regions.setEndpointLink(endpoint.getDocumentSelfLink());
        client.getCloudAssembly().getInfrastructure()
                .updateEndpointRegions(regions);
    }

    public abstract Endpoint getEndpoint();

    public abstract EndpointRegions getEndpointRegions();
}
