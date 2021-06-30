/*
 * Copyright 2021-2021 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.vmware.devops;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import lombok.Getter;

public abstract class IdCache {

    public static final ProjectIdCache PROJECT_ID_CACHE = new ProjectIdCache();
    public static final CloudZoneLinkCache CLOUD_ZONE_LINK_CACHE = new CloudZoneLinkCache();
    public static final RegionLinkCache REGION_LINK_CACHE = new RegionLinkCache();
    public static final ActionIdCache ACTION_ID_CACHE = new ActionIdCache();
    public static final BlueprintIdCache BLUEPRINT_ID_CACHE = new BlueprintIdCache();
    public static final CodestreamCloudProxyIdCache CODESTREAM_CLOUD_PROXY_ID_CACHE = new CodestreamCloudProxyIdCache();
    public static final InfrastructureCloudProxyIdCache INFRASTRUCTURE_CLOUD_PROXY_ID_CACHE = new InfrastructureCloudProxyIdCache();

    @Getter
    private final Map<String, String> nameToId = new ConcurrentHashMap<>();

    private IdCache() {
    }

    public abstract String getId(String name) throws Exception;

    public static class ProjectIdCache extends IdCache {

        private ProjectIdCache() {
        }

        @Override
        public String getId(String name)
                throws InterruptedException, IOException, URISyntaxException {
            if (getNameToId().get(name) == null) {
                getNameToId().put(name,
                        GenerationContext.getInstance().getEndpointConfiguration().getClient()
                                .getCloudAssembly().getInfrastructure().findProjectByName(name)
                                .getId());
            }

            return getNameToId().get(name);
        }
    }

    public static class CloudZoneLinkCache extends IdCache {

        private CloudZoneLinkCache() {
        }

        @Override
        public String getId(String name)
                throws InterruptedException, IOException, URISyntaxException {
            String[] splitted = name.split("/");
            name = splitted[0].trim() + " / " + splitted[1]
                    .trim();  // make sure whitespace does not matter

            if (getNameToId().get(name) == null) {
                getNameToId().put(name,
                        GenerationContext.getInstance().getEndpointConfiguration().getClient()
                                .getCloudAssembly().getInfrastructure().findCloudZoneByName(name)
                                .getDocumentSelfLink());
            }

            return getNameToId().get(name);
        }
    }

    public static class RegionLinkCache extends IdCache {

        private RegionLinkCache() {
        }

        @Override
        public String getId(String name)
                throws InterruptedException, IOException, URISyntaxException {
            // The name here is "endpointName / regionId" format
            String[] splitted = name.split("/");
            String endpointName = splitted[0].trim();
            String regionName = splitted[1].trim();
            name = endpointName + " / " + regionName; // make sure it's proper format

            if (getNameToId().get(name) == null) {
                getNameToId().put(name,
                        GenerationContext.getInstance().getEndpointConfiguration().getClient()
                                .getCloudAssembly().getInfrastructure()
                                .findRegionByEndpointAndRegionName(endpointName, regionName)
                                .getDocumentSelfLink());
            }

            return getNameToId().get(name);
        }
    }

    public static class ActionIdCache extends IdCache {

        private ActionIdCache() {
        }

        @Override
        public String getId(String name)
                throws InterruptedException, IOException, URISyntaxException {
            if (getNameToId().get(name) == null) {
                getNameToId().put(name,
                        GenerationContext.getInstance().getEndpointConfiguration().getClient()
                                .getCloudAssembly().getExtensibility().findActionByName(name)
                                .getId());
            }

            return getNameToId().get(name);
        }
    }

    public static class BlueprintIdCache extends IdCache {

        private BlueprintIdCache() {
        }

        @Override
        public String getId(String name)
                throws InterruptedException, IOException, URISyntaxException {
            if (getNameToId().get(name) == null) {
                getNameToId().put(name,
                        GenerationContext.getInstance().getEndpointConfiguration().getClient()
                                .getCloudAssembly().getDesign().findBlueprintByName(name)
                                .getId());
            }

            return getNameToId().get(name);
        }
    }

    public static class CodestreamCloudProxyIdCache extends IdCache {

        private CodestreamCloudProxyIdCache() {
        }

        @Override
        public String getId(String name)
                throws InterruptedException, IOException, URISyntaxException {
            if (getNameToId().get(name) == null) {
                getNameToId().put(name,
                        GenerationContext.getInstance().getEndpointConfiguration().getClient()
                                .getCodestream().findCloudProxyByName(name).getId());
            }

            return getNameToId().get(name);
        }
    }

    public static class InfrastructureCloudProxyIdCache extends IdCache {

        private InfrastructureCloudProxyIdCache() {
        }

        @Override
        public String getId(String name)
                throws InterruptedException, IOException, URISyntaxException {
            if (getNameToId().get(name) == null) {
                getNameToId().put(name,
                        GenerationContext.getInstance().getEndpointConfiguration().getClient()
                                .getCloudAssembly().getInfrastructure()
                                .findDataCollectorByName(name).getProxyId());
            }

            return getNameToId().get(name);
        }
    }
}
