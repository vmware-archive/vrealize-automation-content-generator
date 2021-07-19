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
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.vmware.devops.ReverseGenerationContext;
import com.vmware.devops.client.cloudassembly.infrastructure.stubs.Endpoint;
import com.vmware.devops.client.cloudassembly.infrastructure.stubs.Endpoint.EndpointType;
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
    private List<AwsRegion> enabledRegions;

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
    public void populateData(Endpoint endpoint) {
        name = endpoint.getName();
        accessKeyId = (String) endpoint.getEndpointProperties()
                .get(PRIVATE_KEY_ID_ENDPOINT_PROPERTY_KEY);
        secretAccessKey = "SET_ME";

        enabledRegions = ReverseGenerationContext.getInstance().getVraExportedData().getRegions()
                .stream().filter(r -> r.getEndpoint().getDocumentSelfLink()
                        .equals(endpoint.getDocumentSelfLink()))
                .map(r -> AwsRegion.fromId(r.getRegionId()))
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

    @AllArgsConstructor
    public enum AwsRegion implements Region {
        AF_SOUTH_1("af-south-1"),
        AP_EAST_1("ap-east-1"),
        AP_NORTHEAST_1("ap-northeast-1"),
        AP_NORTHEAST_2("ap-northeast-2"),
        AP_NORTHEAST_3("ap-northeast-3"),
        AP_SOUTH_1("ap-south-1"),
        AP_SOUTHEAST_1("ap-southeast-1"),
        AP_SOUTHEAST_2("ap-southeast-2"),
        CA_CENTRAL_1("ca-central-1"),
        EU_CENTRAL_1("eu-central-1"),
        EU_NORTH_1("eu-north-1"),
        EU_SOUTH_1("eu-south-1"),
        EU_WEST_1("eu-west-1"),
        EU_WEST_2("eu-west-2"),
        EU_WEST_3("eu-west-3"),
        ME_SOUTH_1("me-south-1"),
        SA_EAST_1("sa-east-1"),
        US_EAST_1("us-east-1"),
        US_EAST_2("us-east-2"),
        US_WEST_1("us-west-1"),
        US_WEST_2("us-west-2");

        @Getter
        private String id;

        public static AwsRegion fromId(String id) {
            for (AwsRegion r : AwsRegion.values()) {
                if (r.getId().equals(id)) {
                    return r;
                }
            }

            throw new IllegalArgumentException("Unknown region with id " + id);
        }

        @Override
        public String getRegionName() {
            return id;
        }
    }
}
