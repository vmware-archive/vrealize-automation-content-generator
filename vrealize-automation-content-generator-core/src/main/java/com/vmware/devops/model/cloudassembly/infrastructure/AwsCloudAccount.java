/*
 * Copyright 2021-2021 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.vmware.devops.model.cloudassembly.infrastructure;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.vmware.devops.ReverseGenerationContext;
import com.vmware.devops.client.cloudassembly.infrastructure.stubs.Endpoint;
import com.vmware.devops.client.cloudassembly.infrastructure.stubs.Endpoint.EndpointType;
import com.vmware.devops.client.cloudassembly.infrastructure.stubs.Region;
import com.vmware.devops.model.ReverseGenerationEntity;

@Builder
@Data
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
@NoArgsConstructor
@Slf4j
public class AwsCloudAccount extends CloudAccount
        implements ReverseGenerationEntity<Endpoint> {

    /**
     * Name of the endpoint
     */
    private String name;

    /**
     * Access key id for AWS account
     */
    private String accessKeyId;

    /**
     * Secret access key for AWS account
     */
    private String secretAccessKey;

    /**
     * Enabled regions
     */
    private List<String> enabledRegions;

    @Override
    public Endpoint getEndpoint() {
        return Endpoint.builder()
                .name(name)
                .endpointType(EndpointType.AWS)
                .endpointProperties(Map.of(
                        PRIVATE_KEY_ID_ENDPOINT_PROPERTY_KEY, accessKeyId,
                        PRIVATE_KEY_ENDPOINT_PROPERTY_KEY, secretAccessKey
                ))
                .build();
    }

    @Override
    protected List<String> getRegions() {
        return enabledRegions;
    }

    @Override
    public void populateData(Endpoint endpoint) {
        name = endpoint.getName();
        accessKeyId = (String) endpoint.getEndpointProperties()
                .get(PRIVATE_KEY_ID_ENDPOINT_PROPERTY_KEY);
        secretAccessKey = "SET_ME";

        enabledRegions = ReverseGenerationContext.getInstance().getVraExportedData().getRegions()
                .stream().filter(r -> r.getEndpoint().getDocumentSelfLink()
                        .equals(endpoint.getDocumentSelfLink()))
                .map(Region::getRegionName)
                .collect(Collectors.toList());
    }

    @Override
    public String getTemplatePath() {
        return "templates/cloudassembly/infrastructure/awsCloudAccountReverseGenerate.groovy.peb";
    }

    @Override
    public void dumpAll() {
        boolean failed = false;
        for (Endpoint endpoint : ReverseGenerationContext.getInstance().getVraExportedData()
                .getEndpoints()) {
            try {
                if (endpoint.getEndpointType().equals(EndpointType.AWS)) {
                    dump(endpoint, ReverseGenerationContext.getInstance()
                            .newOutputDirFile(
                                    "010-" + endpoint.getName() + "-aws-cloud-account.groovy"));
                }
            } catch (Exception e) {
                failed = true;
                log.error("Failed to export AWS cloud account " + endpoint.getName(), e);
            }
        }

        if (failed) {
            throw new RuntimeException("At least one AWS cloud account export failed.");
        }
    }
}
