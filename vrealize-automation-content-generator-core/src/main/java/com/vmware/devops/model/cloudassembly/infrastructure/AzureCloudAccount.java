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
import com.vmware.devops.client.cloudassembly.infrastructure.stubs.Region;
import com.vmware.devops.model.ReverseGenerationEntity;

@Builder
@Data
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
@NoArgsConstructor
@Slf4j
public class AzureCloudAccount extends CloudAccount
        implements ReverseGenerationEntity<Endpoint> {

    public static final String SUBSCIPTION_ID_ENDPOINT_PROPERTY_KEY = "userLink";
    public static final String TENANT_ID_ENDPOINT_PROPERTY_KEY = "azureTenantId";
    /**
     * Name of the endpoint
     */
    private String name;

    /**
     * Subscription ID
     */
    private String subscriptionId;

    /**
     * Subscription ID
     */
    private String tenantId;

    /**
     * Client application ID
     */
    private String clientApplicationId;

    /**
     * Secret key for the client application
     */
    private String clientApplicationSecretKey;

    /**
     * Enabled regions
     */
    private List<AzureRegion> enabledRegions;

    @Override
    public Endpoint getEndpoint() {
        return Endpoint.builder()
                .name(name)
                .endpointType(EndpointType.AZURE)
                .endpointProperties(Map.of(
                        SUBSCIPTION_ID_ENDPOINT_PROPERTY_KEY, subscriptionId,
                        TENANT_ID_ENDPOINT_PROPERTY_KEY, tenantId,
                        PRIVATE_KEY_ID_ENDPOINT_PROPERTY_KEY, clientApplicationId,
                        PRIVATE_KEY_ENDPOINT_PROPERTY_KEY, clientApplicationSecretKey
                ))
                .build();
    }

    @Override
    public void populateData(Endpoint endpoint) {
        name = endpoint.getName();
        subscriptionId = (String) endpoint.getEndpointProperties().get(SUBSCIPTION_ID_ENDPOINT_PROPERTY_KEY);
        tenantId = (String) endpoint.getEndpointProperties().get(TENANT_ID_ENDPOINT_PROPERTY_KEY);
        clientApplicationId = (String) endpoint.getEndpointProperties()
                .get(PRIVATE_KEY_ID_ENDPOINT_PROPERTY_KEY);
        clientApplicationSecretKey = "SET_ME";

        enabledRegions = ReverseGenerationContext.getInstance().getVraExportedData().getRegions()
                .stream().filter(r -> r.getEndpoint().getDocumentSelfLink()
                        .equals(endpoint.getDocumentSelfLink()))
                .map(r -> AzureRegion.fromId(r.getRegionId()))
                .collect(Collectors.toList());
    }

    @Override
    public String getTemplatePath() {
        return "templates/cloudassembly/infrastructure/azureCloudAccountReverseGenerate.groovy.peb";
    }

    @Override
    public void dumpAll() {
        boolean failed = false;
        for (Endpoint endpoint : ReverseGenerationContext.getInstance().getVraExportedData()
                .getEndpoints()) {
            try {
                if (endpoint.getEndpointType().equals(EndpointType.AZURE)) {
                    dump(endpoint, ReverseGenerationContext.getInstance()
                            .newOutputDirFile(
                                    "010-" + endpoint.getName() + "-azure-cloud-account.groovy"));
                }
            } catch (Exception e) {
                failed = true;
                log.error("Failed to export Azure cloud account " + endpoint.getName(), e);
            }
        }

        if (failed) {
            throw new RuntimeException("At least one Azure cloud account export failed.");
        }
    }

    @AllArgsConstructor
    public enum AzureRegion implements Region {
        ASIA_PACIFIC("Asia Pacific", "asiapacific"),
        ASIA("Asia", "asia"),
        AUSTRALIA_CENTRAL_2("Australia Central 2", "australiacentral2"),
        AUSTRALIA_CENTRAL("Australia Central", "australiacentral"),
        AUSTRALIA_EAST("Australia East", "australiaeast"),
        AUSTRALIA_SOUTHEAST("Australia Southeast", "australiasoutheast"),
        AUSTRALIA("Australia", "australia"),
        BRAZIL_SOUTH("Brazil South", "brazilsouth"),
        BRAZIL_SOUTHEAST("Brazil Southeast", "brazilsoutheast"),
        BRAZIL("Brazil", "brazil"),
        CANADA_CENTRAL("Canada Central", "canadacentral"),
        CANADA_EAST("Canada East", "canadaeast"),
        CANADA("Canada", "canada"),
        CENTRAL_INDIA("Central India", "centralindia"),
        CENTRAL_US_EUAP("Central US EUAP", "centraluseuap"),
        CENTRAL_US_STAGE("Central US (Stage)", "centralusstage"),
        CENTRAL_US("Central US", "centralus"),
        EAST_ASIA_STAGE("East Asia (Stage)", "eastasiastage"),
        EAST_ASIA("East Asia", "eastasia"),
        EAST_US_2_EUAP("East US 2 EUAP", "eastus2euap"),
        EAST_US_2_STAGE("East US 2 (Stage)", "eastus2stage"),
        EAST_US_2("East US 2", "eastus2"),
        EAST_US_STAGE("East US (Stage)", "eastusstage"),
        EAST_US("East US", "eastus"),
        EUROPE("Europe", "europe"),
        FRANCE_CENTRAL("France Central", "francecentral"),
        FRANCE_SOUTH("France South", "francesouth"),
        GERMANY_NORTH("Germany North", "germanynorth"),
        GERMANY_WEST_CENTRAL("Germany West Central", "germanywestcentral"),
        GLOBAL("Global", "global"),
        INDIA("India", "india"),
        JAPAN_EAST("Japan East", "japaneast"),
        JAPAN_WEST("Japan West", "japanwest"),
        JAPAN("Japan", "japan"),
        JIO_INDIA_CENTRAL("Jio India Central", "jioindiacentral"),
        JIO_INDIA_WEST("Jio India West", "jioindiawest"),
        KOREA_CENTRAL("Korea Central", "koreacentral"),
        KOREA_SOUTH("Korea South", "koreasouth"),
        NORTH_CENTRAL_US_STAGE("North Central US (Stage)", "northcentralusstage"),
        NORTH_CENTRAL_US("North Central US", "northcentralus"),
        NORTH_EUROPE("North Europe", "northeurope"),
        NORWAY_EAST("Norway East", "norwayeast"),
        NORWAY_WEST("Norway West", "norwaywest"),
        SOUTH_AFRICA_NORTH("South Africa North", "southafricanorth"),
        SOUTH_AFRICA_WEST("South Africa West", "southafricawest"),
        SOUTH_CENTRAL_US_STAGE("South Central US (Stage)", "southcentralusstage"),
        SOUTH_CENTRAL_US("South Central US", "southcentralus"),
        SOUTH_INDIA("South India", "southindia"),
        SOUTHEAST_ASIA_STAGE("Southeast Asia (Stage)", "southeastasiastage"),
        SOUTHEAST_ASIA("Southeast Asia", "southeastasia"),
        SWEDEN_CENTRAL("Sweden Central", "swedencentral"),
        SWEDEN_SOUTH("Sweden South", "swedensouth"),
        SWITZERLAND_NORTH("Switzerland North", "switzerlandnorth"),
        SWITZERLAND_WEST("Switzerland West", "switzerlandwest"),
        UAE_CENTRAL("UAE Central", "uaecentral"),
        UAE_North("UAE North", "uaenorth"),
        UK_SOUTH("UK South", "uksouth"),
        UK_WEST("UK West", "ukwest"),
        UNITED_KINGDOM("United Kingdom", "uk"),
        UNITED_STATES("United States", "unitedstates"),
        WEST_CENTRAL_US("West Central US", "westcentralus"),
        WEST_EUROPE("West Europe", "westeurope"),
        WEST_INDIA("West India", "westindia"),
        WEST_US_2_STAGE("West US 2 (Stage)", "westus2stage"),
        WEST_US_2("West US 2", "westus2"),
        WEST_US_3("West US 3", "westus3"),
        WEST_US_STAGE("West US (Stage)", "westusstage"),
        WEST_US("West US", "westus");

        @Getter
        private String regionName;

        @Getter
        private String id;

        public static AzureRegion fromId(String id) {
            for (AzureRegion r : AzureRegion.values()) {
                if (r.getId().equals(id)) {
                    return r;
                }
            }

            throw new IllegalArgumentException("Unknown region with id " + id);
        }
    }
}
