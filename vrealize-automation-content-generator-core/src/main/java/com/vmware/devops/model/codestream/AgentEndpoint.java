/*
 * Copyright 2021-2021 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.vmware.devops.model.codestream;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.vmware.devops.GenerationContext;
import com.vmware.devops.IdCache;
import com.vmware.devops.ReverseGenerationContext;
import com.vmware.devops.client.codestream.CodestreamClient;
import com.vmware.devops.client.codestream.stubs.Endpoint.Type;
import com.vmware.devops.model.ReverseGenerationEntity;

/**
 * AgentEndpoint represent entity which extends {@link com.vmware.devops.model.codestream.Endpoint}
 * and specify endpoint of type Agent
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@Slf4j
public class AgentEndpoint extends Endpoint implements
        ReverseGenerationEntity<com.vmware.devops.client.codestream.stubs.Endpoint> {

    /**
     * The name of the endpoint
     */
    private String name;

    /**
     * The project of the endpoint.
     * <p>
     * Defaults to the one specified in global configuration.
     * In case the project is specified explicitly, it will be associated to the endpoint.
     */
    @Builder.Default
    private String project = GenerationContext.getInstance().getGlobalConfiguration()
            .getDefaultProject();

    /**
     * The cloud proxy of the endpoint.
     * <p>
     * By default vRealize Automation Generator will use the one specified in global configuration.
     * In case the cloud proxy is specified explicitly, it will be associated to the endpoint.
     */
    @Builder.Default
    private String cloudProxy = GenerationContext.getInstance().getGlobalConfiguration()
            .getDefaultCloudProxy();

    @Override
    public com.vmware.devops.client.codestream.stubs.Endpoint initializeEndpoint()
            throws Exception {
        String cloudProxyId = null;
        if (cloudProxy != null) {
            cloudProxyId = IdCache.CODESTREAM_CLOUD_PROXY_ID_CACHE.getId(cloudProxy);
        }

        return com.vmware.devops.client.codestream.stubs.Endpoint.builder()
                .name(name)
                .project(project)
                .type(Type.AGENT)
                .cloudProxyId(cloudProxyId)
                .properties(Map.of(
                        "leMansAgent", cloudProxyId
                ))
                .build();
    }

    @Override
    public void populateData(
            com.vmware.devops.client.codestream.stubs.Endpoint endpoint) {
        name = endpoint.getName();
        project = endpoint.getProject();
        if (endpoint.getCloudProxyId() != null) {
            cloudProxy = CodestreamClient
                    .getProxyName(ReverseGenerationContext.getInstance().getVraExportedData()
                            .getCloudProxies().stream()
                            .filter(c -> c.getId().equals(endpoint.getCloudProxyId())).findFirst()
                            .get());
        }
    }

    @Override
    public String getTemplatePath() {
        return "templates/codestream/agentEndpointReverseGenerate.groovy.peb";
    }

    @Override
    public void dumpAll() {
        boolean failed = false;
        for (com.vmware.devops.client.codestream.stubs.Endpoint endpoint : ReverseGenerationContext
                .getInstance().getVraExportedData().getCodestreamEndpoints()) {
            try {
                if (endpoint.getType().equals(Type.AGENT)) {
                    dump(endpoint, ReverseGenerationContext.getInstance()
                            .newOutputDirFile(endpoint.getName() + "-agent-endpoint.groovy"));
                }
            } catch (Exception e) {
                failed = true;
                log.error("Failed to export agent endpoint " + endpoint.getName(), e);
            }
        }

        if (failed) {
            throw new RuntimeException("At least one agent endpoint export failed.");
        }
    }
}
