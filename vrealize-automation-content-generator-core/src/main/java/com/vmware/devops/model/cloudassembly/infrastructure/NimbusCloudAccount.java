/*
 * Copyright 2021-2021 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.vmware.devops.model.cloudassembly.infrastructure;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.vmware.devops.ReverseGenerationContext;
import com.vmware.devops.client.cloudassembly.infrastructure.stubs.Endpoint;
import com.vmware.devops.client.cloudassembly.infrastructure.stubs.Endpoint.EndpointType;
import com.vmware.devops.client.cloudassembly.infrastructure.stubs.InstanceTypeInfo;
import com.vmware.devops.model.ReverseGenerationEntity;

@Builder
@Data
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
@NoArgsConstructor
@Slf4j

/**
 * VMware internal. Not for public use.
 */
public class NimbusCloudAccount extends CloudAccount
        implements ReverseGenerationEntity<Endpoint> {

    public static final String NIMBUS_CLOUD_ACCOUNT_NAME = "nimbus";

    @Builder.Default
    private List<NimbusRegion> enabledRegions = Collections.emptyList();

    @Override
    public Endpoint getEndpoint() {
        return Endpoint.builder()
                .name(NIMBUS_CLOUD_ACCOUNT_NAME)
                .endpointType(EndpointType.NIMBUS)
                .endpointProperties(Map.of(
                        "dcId", "onprem"
                ))
                .customProperties(Map.of(
                        "isExternal", "false"
                ))
                .build();
    }

    @Override
    public void populateData(Endpoint endpoint)
            throws InterruptedException, IOException, URISyntaxException {
        enabledRegions = ReverseGenerationContext.getInstance().getEndpointConfiguration()
                .getClient().getCloudAssembly().getInfrastructure()
                .fetchRegionsForEndpoint(endpoint).stream()
                .map(r -> NimbusRegion.fromId(r.getRegionId()))
                .collect(Collectors.toList());
    }

    @Override
    public String getTemplatePath() {
        return "templates/cloudassembly/infrastructure/nimbusCloudAccountReverseGenerate.groovy.peb";
    }

    @Override
    public void dumpAll() {
        boolean failed = false;
        for (Endpoint endpoint : ReverseGenerationContext.getInstance().getVraExportedData()
                .getEndpoints()) {
            try {
                if (endpoint.getEndpointType().equals(EndpointType.NIMBUS)) {
                    dump(endpoint, ReverseGenerationContext.getInstance()
                            .newOutputDirFile(
                                    "010-" + endpoint.getName() + "-nimbus-cloud-account.groovy"));
                }
            } catch (Exception e) {
                failed = true;
                log.error("Failed to export Nimbus cloud account " + endpoint.getName(), e);
            }
        }

        if (failed) {
            throw new RuntimeException("At least one Nimbus cloud account export failed.");
        }
    }

    @AllArgsConstructor
    public enum NimbusRegion implements Region {
        SC("sc", "Santa Clara"),
        WDC("wdc", "Washington DC"),
        SOF("sof", "Sofia");

        @Getter
        private String id;

        @Getter
        private String regionName;

        public static NimbusRegion fromId(String id) {
            return NimbusRegion.valueOf(id.toUpperCase());
        }
    }

    @AllArgsConstructor
    public enum NimbusFlavor implements Flavor {
        DEFAULT("Default");

        private String id;

        @Override
        public InstanceTypeInfo initializeInstanceTypeInfo() {
            return InstanceTypeInfo.builder().instanceType(id).build();
        }
    }
}
