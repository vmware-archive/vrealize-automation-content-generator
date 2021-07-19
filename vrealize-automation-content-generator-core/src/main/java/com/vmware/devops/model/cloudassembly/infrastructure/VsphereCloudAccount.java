/*
 * Copyright 2021-2021 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.vmware.devops.model.cloudassembly.infrastructure;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import com.vmware.devops.GenerationContext;
import com.vmware.devops.IdCache;
import com.vmware.devops.ReverseGenerationContext;
import com.vmware.devops.Utils;
import com.vmware.devops.client.cloudassembly.infrastructure.stubs.Endpoint;
import com.vmware.devops.client.cloudassembly.infrastructure.stubs.Endpoint.EndpointType;
import com.vmware.devops.client.cloudassembly.infrastructure.stubs.InstanceTypeInfo;
import com.vmware.devops.model.ReverseGenerationEntity;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@Slf4j
public class VsphereCloudAccount extends CloudAccount
        implements ReverseGenerationEntity<Endpoint> {

    private static final String ONPREM_CLOUD_PROXY_ID = "onprem";

    public static final String HOST_NAME_ENDPOINT_PROPERTY_KEY = "hostName";
    public static final String DC_ID_ENDPOINT_PROPERTY_KEY = "dcId";

    /**
     * Cloud account names
     */
    private String name;

    /**
     * vSphere hostname
     */
    private String hostname;

    /**
     * Username for authentication
     */
    private String username;

    /**
     * Password for authentication
     */
    private String password;

    /**
     * Enabled data centers
     */
    private List<String> datacenters;

    /**
     * Cloud proxy name to use when connecting to the server. Required only when using VMware Cloud
     */
    private String cloudProxy;

    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private String certificate;

    @Builder
    public VsphereCloudAccount(String name, String hostname, String username, String password,
            List<String> datacenters, String cloudProxy) {
        this.name = name;
        this.hostname = hostname;
        this.username = username;
        this.password = password;
        this.datacenters = Optional.ofNullable(datacenters).orElse(Collections.emptyList());
        this.cloudProxy = Optional.ofNullable(cloudProxy)
                .orElse(GenerationContext.getInstance().getGlobalConfiguration()
                        .getDefaultCloudProxy());
    }

    @Override
    public Endpoint getEndpoint() {
        if (certificate == null) { // We invoke this method multiple times
            certificate = fetchCertificate();
        }

        String cloudProxyId = ONPREM_CLOUD_PROXY_ID;
        if (cloudProxy != null) {
            try {
                cloudProxyId = IdCache.INFRASTRUCTURE_CLOUD_PROXY_ID_CACHE.getId(cloudProxy);
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        }

        return Endpoint.builder()
                .name(name)
                .endpointType(EndpointType.VSPHERE)
                .endpointProperties(Map.of(
                        HOST_NAME_ENDPOINT_PROPERTY_KEY, hostname,
                        DC_ID_ENDPOINT_PROPERTY_KEY, cloudProxyId,
                        PRIVATE_KEY_ID_ENDPOINT_PROPERTY_KEY, username,
                        PRIVATE_KEY_ENDPOINT_PROPERTY_KEY, password,
                        "acceptSelfSignedCertificate", true,
                        "certificate", certificate
                ))
                .build();
    }

    @Override
    protected List<Datacenter> getEnabledRegions() {
        return getDatacenters().stream().map(Datacenter::new)
                .collect(Collectors.toList());
    }

    public String fetchCertificate() {
        try {
            return Utils.getCertificateFromUrl("https://" + hostname);
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class VsphereFlavor implements Flavor {
        private int cpuCount;
        private int memoryMb;

        @Override
        public InstanceTypeInfo initializeInstanceTypeInfo() {
            return InstanceTypeInfo.builder()
                    .cpuCount(cpuCount)
                    .memoryMb(memoryMb)
                    .build();
        }
    }

    @Override
    public void populateData(Endpoint endpoint) {
        name = endpoint.getName();
        hostname = (String) endpoint.getEndpointProperties()
                .get(HOST_NAME_ENDPOINT_PROPERTY_KEY);
        username = (String) endpoint.getEndpointProperties()
                .get(PRIVATE_KEY_ID_ENDPOINT_PROPERTY_KEY);
        password = "SET_ME";

        datacenters = ReverseGenerationContext.getInstance().getVraExportedData().getRegions()
                .stream().filter(r -> r.getEndpoint().getDocumentSelfLink()
                        .equals(endpoint.getDocumentSelfLink()))
                .map(com.vmware.devops.client.cloudassembly.infrastructure.stubs.Region::getRegionName)
                .collect(Collectors.toList());
        String cloudProxyId = (String) endpoint.getEndpointProperties()
                .get(DC_ID_ENDPOINT_PROPERTY_KEY);
        if (cloudProxyId != null && !cloudProxyId.equals(ONPREM_CLOUD_PROXY_ID)) {
            cloudProxy = ReverseGenerationContext.getInstance().getVraExportedData()
                    .getDataCollectors().stream().filter(dc -> dc.getProxyId().equals(cloudProxyId))
                    .findFirst().get().getName();
        }
    }

    @Override
    public String getTemplatePath() {
        return "templates/cloudassembly/infrastructure/vsphereCloudAccountReverseGenerate.groovy.peb";
    }

    @Override
    public void dumpAll() {
        boolean failed = false;
        for (Endpoint endpoint : ReverseGenerationContext.getInstance().getVraExportedData()
                .getEndpoints()) {
            try {
                if (endpoint.getEndpointType().equals(EndpointType.VSPHERE)) {
                    dump(endpoint, ReverseGenerationContext.getInstance()
                            .newOutputDirFile(
                                    "010-" + endpoint.getName() + "-vsphere-cloud-account.groovy"));
                }
            } catch (Exception e) {
                failed = true;
                log.error("Failed to export vSphere cloud account " + endpoint.getName(), e);
            }
        }

        if (failed) {
            throw new RuntimeException("At least one vSphere cloud account export failed.");
        }
    }

    // Just a helper class so this conforms to the abstractions
    @AllArgsConstructor
    private static class Datacenter implements Region {
        @Getter
        private String regionName;

        @Override
        public String getId() {
            return regionName;
        }
    }
}
