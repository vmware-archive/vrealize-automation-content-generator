/*
 * Copyright 2021-2021 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.vmware.devops.model.codestream;

import java.io.IOException;
import java.net.URISyntaxException;
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
 * JenkinsEndpoint represent entity which extends {@link com.vmware.devops.model.codestream.Endpoint}
 * and specify endpoint of type Jenkins
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@Slf4j
public class JenkinsEndpoint extends Endpoint
        implements ReverseGenerationEntity<com.vmware.devops.client.codestream.stubs.Endpoint> {

    public static final String URL_PROPERTY_KEY = "url";
    public static final String USERNAME_PROPERTY_KEY = "username";
    public static final String PASSWORD_PROPERTY_KEY = "password";
    public static final String POLL_INTERVAL_PROPERTY_KEY = "pollInterval";
    public static final String RETRY_COUNT_PROPERTY_KEY = "retryCount";
    public static final String RETRY_WAIT_SECONDS_PROPERTY_KEY = "retryWaitSeconds";
    public static final String FINGERPRINT_PROPERTY_KEY = "fingerprint";

    /**
     * The name of the endpoint
     */
    private String name;

    /**
     * The url of the endpoint
     */
    private String url;

    /**
     * The username of the endpoint
     */
    private String username;

    /**
     * THe password of the endpoint
     */
    private String password;

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
     * The poll interval of the endpoint.
     * <p>
     * By default the value is 15 seconds. This mean that the endpoint will be polled on every 15
     * seconds
     */
    @Builder.Default
    private int pollInterval = 15;

    /**
     * The retry count of request for polling the endpoint.
     * <p>
     * By default the value is 5. Which mean that the poll request will retry 5 type in case of
     * intermittent failure
     */
    @Builder.Default
    private int retryCount = 5;

    /**
     * The retry interval between each retry.
     * <p>
     * By default the retry interval is 60 seconds.
     */
    @Builder.Default
    private int retryWaitSeconds = 60;

    @Override
    public com.vmware.devops.client.codestream.stubs.Endpoint initializeEndpoint()
            throws InterruptedException, IOException, URISyntaxException {
        String cloudProxyId = null;
        if (cloudProxy != null) {
            cloudProxyId = IdCache.CODESTREAM_CLOUD_PROXY_ID_CACHE.getId(cloudProxy);
        }
        String fingerprint = "";
        if (url.contains("https")) {
            fingerprint = Endpoint.ENDPOINT_FINGERPRINT_CACHE.getFingerprint(url, cloudProxyId);
        }
        return com.vmware.devops.client.codestream.stubs.Endpoint.builder()
                .name(name)
                .project(project)
                .type(Type.JENKINS)
                .cloudProxyId(cloudProxyId)
                .properties(Map.of(
                        URL_PROPERTY_KEY, url,
                        USERNAME_PROPERTY_KEY, username,
                        PASSWORD_PROPERTY_KEY,
                        Variable.variableReference(getPasswordVariableName()),
                        POLL_INTERVAL_PROPERTY_KEY, pollInterval,
                        RETRY_COUNT_PROPERTY_KEY, retryCount,
                        RETRY_WAIT_SECONDS_PROPERTY_KEY, retryWaitSeconds,
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
        project = endpoint.getProject();
        url = (String) endpoint.getProperties().get(URL_PROPERTY_KEY);
        username = (String) endpoint.getProperties().get(USERNAME_PROPERTY_KEY);
        password = (String) endpoint.getProperties().get(PASSWORD_PROPERTY_KEY);
        pollInterval = (Integer) endpoint.getProperties().get(POLL_INTERVAL_PROPERTY_KEY);
        retryCount = (Integer) endpoint.getProperties().get(RETRY_COUNT_PROPERTY_KEY);
        retryWaitSeconds = (Integer) endpoint.getProperties().get(RETRY_WAIT_SECONDS_PROPERTY_KEY);

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
        return "templates/codestream/jenkinsEndpointReverseGenerate.groovy.peb";
    }

    @Override
    public void dumpAll() {
        boolean failed = false;
        for (com.vmware.devops.client.codestream.stubs.Endpoint endpoint : ReverseGenerationContext
                .getInstance().getVraExportedData().getCodestreamEndpoints()) {
            try {
                if (endpoint.getType().equals(Type.JENKINS)) {
                    dump(endpoint, ReverseGenerationContext.getInstance()
                            .newOutputDirFile(endpoint.getName() + "-jenkins-endpoint.groovy"));
                }
            } catch (Exception e) {
                failed = true;
                log.error("Failed to export jenkins endpoint " + endpoint.getName(), e);
            }
        }

        if (failed) {
            throw new RuntimeException("At least one jenkins endpoint export failed.");
        }
    }
}
