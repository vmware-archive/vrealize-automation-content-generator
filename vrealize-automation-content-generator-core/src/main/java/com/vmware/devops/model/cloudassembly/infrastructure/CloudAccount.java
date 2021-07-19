/*
 * Copyright 2021-2021 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.vmware.devops.model.cloudassembly.infrastructure;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.vmware.devops.GenerationContext;
import com.vmware.devops.client.Client;
import com.vmware.devops.client.cloudassembly.infrastructure.stubs.Endpoint;
import com.vmware.devops.client.cloudassembly.infrastructure.stubs.EndpointRegions;
import com.vmware.devops.client.cloudassembly.infrastructure.stubs.RegionInfo;
import com.vmware.devops.model.GenerationEntity;

public abstract class CloudAccount
        implements GenerationEntity {

    public static final String PRIVATE_KEY_ID_ENDPOINT_PROPERTY_KEY = "privateKeyId";
    public static final String PRIVATE_KEY_ENDPOINT_PROPERTY_KEY = "privateKey";

    @Override
    public void generate() throws Exception {
        Endpoint endpoint = getEndpoint();

        Client client = GenerationContext.getInstance()
                .getEndpointConfiguration().getClient();
        endpoint = client.getCloudAssembly().getInfrastructure()
                .createOrUpdateEndpoint(endpoint);

        EndpointRegions regions = getEndpointRegions();
        regions.setEndpointLink(endpoint.getDocumentSelfLink());
        client.getCloudAssembly().getInfrastructure()
                .updateEndpointRegions(regions);
    }

    public List<RegionInfo> filterRegions(List<String> regionNames) {
        try {
            List<RegionInfo> regions = GenerationContext.getInstance().getEndpointConfiguration()
                    .getClient()
                    .getCloudAssembly().getInfrastructure().fetchRegionsForEndpoint(getEndpoint());
            Set<String> notFould = new HashSet<>(regionNames);
            List<RegionInfo> result = new ArrayList<>();
            for (RegionInfo r : regions) {
                String name = r.getName();
                if (notFould.remove(name)) {
                    result.add(r);
                }
            }

            if (!notFould.isEmpty()) {
                throw new IllegalStateException("Not all regions found: " + notFould);
            }

            return result;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public EndpointRegions getEndpointRegions() {
        List<RegionInfo> enabledRegions = filterRegions(getRegions());

        return EndpointRegions.builder()
                .enabledRegionIds(enabledRegions.stream().map(RegionInfo::getRegionId).collect(
                        Collectors.toList()))
                .enabledRegions(enabledRegions)
                .createDefaultZones(true)
                .build();
    }

    public abstract Endpoint getEndpoint();

    protected abstract List<String> getRegions();
}
