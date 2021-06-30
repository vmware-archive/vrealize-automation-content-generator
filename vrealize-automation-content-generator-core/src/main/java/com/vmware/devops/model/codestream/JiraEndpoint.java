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
import com.vmware.devops.client.codestream.stubs.Variable.VariableType;
import com.vmware.devops.model.ReverseGenerationEntity;

/**
 * JiraEndpoint represent entity which extends {@link com.vmware.devops.model.codestream.Endpoint}
 * and specify endpoint of type Jira
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@Slf4j
public class JiraEndpoint extends Endpoint
        implements ReverseGenerationEntity<com.vmware.devops.client.codestream.stubs.Endpoint> {

    public static final String URL_PROPERTY_KEY = "url";
    public static final String USERNAME_PROPERTY_KEY = "username";
    public static final String PASSWORD_PROPERTY_KEY = "password";
    public static final String FINGERPRINT_PROPERTY_KEY = "fingerprint";

    /**
     * The name of the endpoint.
     */
    private String name;

    /**
     * The url of the endpoint.
     */
    private String url;

    /**
     * The username of the endpoint.
     */
    private String username;

    /**
     * THe password of the endpoint.
     */
    private String password;

    /**
     * The description of the endpoint.
     */
    @Builder.Default
    private String description = "";

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

    /**
     * Property specify whether to accept the certificate or not.
     * <p>
     * By default accept certificate is false.
     */
    @Builder.Default
    private Boolean acceptCertificate = false;

    @Override
    public com.vmware.devops.client.codestream.stubs.Endpoint initializeEndpoint()
            throws Exception {
        String cloudProxyId = null;
        if (cloudProxy != null) {
            cloudProxyId = IdCache.CODESTREAM_CLOUD_PROXY_ID_CACHE.getId(cloudProxy);
        }
        String fingerprint = "";
        if (acceptCertificate) {
            fingerprint = Endpoint.ENDPOINT_FINGERPRINT_CACHE.getFingerprint(url, cloudProxyId);
        }
        return com.vmware.devops.client.codestream.stubs.Endpoint.builder()
                .name(name)
                .project(project)
                .type(Type.JIRA)
                .cloudProxyId(cloudProxyId)
                .properties(Map.of(
                        URL_PROPERTY_KEY, url,
                        USERNAME_PROPERTY_KEY, username,
                        PASSWORD_PROPERTY_KEY,
                        Variable.variableReference(getPasswordVariableName()),
                        FINGERPRINT_PROPERTY_KEY, fingerprint
                ))
                .build();
    }

    public Variable initializePasswordVariable() {
        return Variable.builder()
                .name(getPasswordVariableName())
                .type(VariableType.SECRET)
                .value(password)
                .build();
    }

    @Override
    public void generate() throws Exception {
        Variable passwordVar = initializePasswordVariable();
        passwordVar.generate();

        super.generate();
    }

    private String getPasswordVariableName() {
        return name + "-endpoint-password";
    }

    @Override
    public void populateData(com.vmware.devops.client.codestream.stubs.Endpoint endpoint) {
        name = endpoint.getName();
        description = endpoint.getDesciption();
        project = endpoint.getProject();
        url = (String) endpoint.getProperties().get(URL_PROPERTY_KEY);
        username = (String) endpoint.getProperties().get(USERNAME_PROPERTY_KEY);
        password = (String) endpoint.getProperties().get(PASSWORD_PROPERTY_KEY);

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
        return "templates/codestream/jiraEndpointReverseGenerate.groovy.peb";
    }

    @Override
    public void dumpAll() {
        boolean failed = false;
        for (com.vmware.devops.client.codestream.stubs.Endpoint endpoint : ReverseGenerationContext
                .getInstance().getVraExportedData().getCodestreamEndpoints()) {
            try {
                if (endpoint.getType().equals(Type.JIRA)) {
                    dump(endpoint, ReverseGenerationContext.getInstance()
                            .newOutputDirFile(endpoint.getName() + "-jira-endpoint.groovy"));
                }
            } catch (Exception e) {
                failed = true;
                log.error("Failed to export jira endpoint " + endpoint.getName(), e);
            }
        }

        if (failed) {
            throw new RuntimeException("At least one jira endpoint export failed.");
        }
    }
}
