/*
 * Copyright 2021-2021 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.vmware.devops.model.cloudassembly.infrastructure;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.vmware.devops.GenerationContext;
import com.vmware.devops.IdCache;
import com.vmware.devops.ReverseGenerationContext;
import com.vmware.devops.client.cloudassembly.infrastructure.stubs.Endpoint;
import com.vmware.devops.client.cloudassembly.infrastructure.stubs.Endpoint.EndpointType;
import com.vmware.devops.client.cloudassembly.infrastructure.stubs.InstanceName;
import com.vmware.devops.client.cloudassembly.infrastructure.stubs.Region;
import com.vmware.devops.model.GenerationEntity;
import com.vmware.devops.model.ReverseGenerationEntity;
import com.vmware.devops.model.cloudassembly.infrastructure.NimbusCloudAccount.NimbusFlavor;
import com.vmware.devops.model.cloudassembly.infrastructure.VsphereCloudAccount.VsphereFlavor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@Slf4j
public class FlavorMapping
        implements GenerationEntity, ReverseGenerationEntity<InstanceName> {
    /**
     * Flavor mapping name
     */
    private String name;

    /**
     * Flavor mapping
     */
    private Map<String, Flavor> flavorMapping;

    public InstanceName initializeInstanceName() {
        return InstanceName.builder()
                .name(name)
                .instanceTypeMapping(
                        flavorMapping.entrySet().stream().collect(Collectors.toMap(
                                e -> {
                                    try {
                                        String region = e.getKey();
                                        if (!region.contains("/")) {
                                            region = GenerationContext.getInstance()
                                                    .getCloudAssemblyConfiguration()
                                                    .getDefaultCloudAccount() + "/" + region;
                                        }

                                        return IdCache.REGION_LINK_CACHE.getId(region);
                                    } catch (Exception ex) {
                                        throw new RuntimeException(ex);
                                    }
                                },
                                e -> e.getValue().initializeInstanceTypeInfo()
                        ))
                )
                .build();
    }

    @Override
    public void generate() throws Exception {
        InstanceName instanceName = initializeInstanceName();
        GenerationContext.getInstance().getEndpointConfiguration().getClient()
                .getCloudAssembly().getInfrastructure().createOrUpdateInstanceName(instanceName);
    }

    @Override
    public void populateData(InstanceName instanceName) {
        name = instanceName.getName();
        flavorMapping = new HashMap<>();
        instanceName.getInstanceTypeMapping().forEach((regionLink, value) -> {
            Region region = ReverseGenerationContext.getInstance().getVraExportedData()
                    .getRegions().stream()
                    .filter(r -> r.getDocumentSelfLink().equals(regionLink))
                    .findFirst().get();
            String fullRegionName = region.getEndpoint().getName() + " / " + region.getRegionName();
            Endpoint endpoint = region.getEndpoint();
            if (EndpointType.NIMBUS == endpoint.getEndpointType()) {
                flavorMapping.put(fullRegionName, NimbusFlavor.DEFAULT);
            } else if (EndpointType.VSPHERE == endpoint.getEndpointType()) {
                flavorMapping.put(fullRegionName,
                        VsphereFlavor.builder()
                                .cpuCount(value.getCpuCount())
                                .memoryMb(value.getMemoryMb())
                                .build());
            } else {
                throw new IllegalArgumentException(
                        String.format("Unsupported endpoint type '%s'",
                                endpoint.getEndpointType()));
            }
        });
    }

    @Override
    public String getTemplatePath() {
        return "templates/cloudassembly/infrastructure/flavorMappingReverseGenerate.groovy.peb";
    }

    @Override
    public void dumpAll() {
        boolean failed = false;
        for (InstanceName instanceName : ReverseGenerationContext.getInstance().getVraExportedData()
                .getInstanceNames()) {
            try {
                dump(instanceName, ReverseGenerationContext.getInstance()
                        .newOutputDirFile(instanceName.getName() + "-flavor-mapping.groovy"));
            } catch (Exception e) {
                failed = true;
                log.error("Failed to export flavor mapping " + instanceName.getName(), e);
            }
        }

        if (failed) {
            throw new RuntimeException("At least one flavor mapping export failed.");
        }
    }
}
