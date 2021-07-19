/*
 * Copyright 2021-2021 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.vmware.devops.model.cloudassembly.infrastructure;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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

    public List<RegionInfo> filterRegions(List<? extends Region> regions) {
        try {
            List<RegionInfo> regionInfos = GenerationContext.getInstance()
                    .getEndpointConfiguration()
                    .getClient()
                    .getCloudAssembly().getInfrastructure().fetchRegionsForEndpoint(getEndpoint());
            Map<String, Region> notFould = regions.stream()
                    .collect(Collectors.toMap(Region::getId, r -> r));
            List<RegionInfo> result = new ArrayList<>();
            for (RegionInfo r : regionInfos) {
                if (notFould.remove(r.getRegionId()) != null ||
                        notFould.remove(r.getName()) != null) { // Try names as well because of VC
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
        List<RegionInfo> enabledRegions = filterRegions(getEnabledRegions());

        return EndpointRegions.builder()
                .enabledRegionIds(enabledRegions.stream().map(RegionInfo::getRegionId).collect(
                        Collectors.toList()))
                .enabledRegions(enabledRegions)
                .createDefaultZones(true)
                .build();
    }

    public abstract Endpoint getEndpoint();

    protected abstract List<? extends Region> getEnabledRegions();

    public interface Region {
        String getRegionName();

        String getId();
    }
}
