/*
 * Copyright 2021-2021 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.vmware.devops.model.codestream;

import java.util.HashMap;
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
 * GerritEndpoint represent entity which extends {@link com.vmware.devops.model.codestream.Endpoint}
 * and specify endpoint of type Gerrit
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@Slf4j
public class GerritEndpoint extends Endpoint
        implements ReverseGenerationEntity<com.vmware.devops.client.codestream.stubs.Endpoint> {

    public static final String LE_MANS_AGENT_PROPERTY_KEY = "leMansAgent";
    public static final String URL_PROPERTY_KEY = "url";
    public static final String USERNAME_PROPERTY_KEY = "username";
    public static final String PASSWORD_PROPERTY_KEY = "password";
    public static final String PRIVATE_KEY_PROPERTY_KEY = "privateKey";
    public static final String PASS_PHRASE_PROPERTY_KEY = "passPhrase";
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
     * The password of the endpoint
     */
    private String password;

    /**
     * The private key of the endpoint
     */
    private String privateKey;

    /**
     * The pass phrase for the private key
     */
    private String passPhrase;


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
        String fingerprint = Endpoint.ENDPOINT_FINGERPRINT_CACHE.getFingerprint(url, cloudProxyId);

        String passPhrasePlaceholder = null;
        if (passPhrase != null) {
            passPhrasePlaceholder = Variable.variableReference(getPassPhraseVariableName());
        }

        Map<String, Object> properties = new HashMap<>();
        properties.put(LE_MANS_AGENT_PROPERTY_KEY, cloudProxyId);
        properties.put(URL_PROPERTY_KEY, url);
        properties.put(USERNAME_PROPERTY_KEY, username);
        properties.put(
                PASSWORD_PROPERTY_KEY, Variable.variableReference(getPasswordVariableName()));
        properties.put(PRIVATE_KEY_PROPERTY_KEY, privateKey);
        properties.put(PASS_PHRASE_PROPERTY_KEY, passPhrasePlaceholder);
        properties.put(FINGERPRINT_PROPERTY_KEY, fingerprint);

        return com.vmware.devops.client.codestream.stubs.Endpoint.builder()
                .name(name)
                .project(project)
                .type(Type.GERRIT)
                .cloudProxyId(cloudProxyId)
                .properties(properties)
                .build();
    }

    public Variable initializePasswordVariable() {
        return Variable.builder()
                .name(getPasswordVariableName())
                .type(VariableType.SECRET)
                .value(password)
                .build();
    }

    public Variable initializePassPhaseVariable() {
        return Variable.builder()
                .name(getPassPhraseVariableName())
                .type(VariableType.SECRET)
                .value(passPhrase)
                .build();
    }

    public Variable initializeListenerApiTokenVariable() {
        return Variable.builder()
                .name(getListenerApiTokenVariableName())
                .type(VariableType.SECRET)
                .value(GenerationContext.getInstance().getEndpointConfiguration()
                        .getAuthenticationDetails().getRefreshToken())
                .build();
    }

    public com.vmware.devops.client.codestream.stubs.GerritListener initializeListener() {
        return com.vmware.devops.client.codestream.stubs.GerritListener.builder()
                .name(name)
                .project(project)
                .endpoint(name)
                .apiToken(Variable.variableReference(getListenerApiTokenVariableName()))
                .connected(true)
                .build();
    }

    @Override
    public void generate() throws Exception {
        Variable passwordVar = initializePasswordVariable();
        passwordVar.generate();

        if (passPhrase != null) {
            Variable passPhraseVar = initializePassPhaseVariable();
            passPhraseVar.generate();
        }

        super.generate();

        Variable apiTokenVariable = initializeListenerApiTokenVariable();
        apiTokenVariable.generate();

        com.vmware.devops.client.codestream.stubs.GerritListener listener = initializeListener();
        GenerationContext.getInstance().getEndpointConfiguration().getClient().getCodestream()
                .createOrUpdateGerritListener(listener);
    }

    private String getPasswordVariableName() {
        return name + "-endpoint-password";
    }

    private String getPassPhraseVariableName() {
        return name + "-endpoint-pass-phrase";
    }

    private String getListenerApiTokenVariableName() {
        return name + "-endpoint-listen-token";
    }

    @Override
    public void populateData(com.vmware.devops.client.codestream.stubs.Endpoint endpoint) {
        name = endpoint.getName();
        project = endpoint.getProject();
        url = (String) endpoint.getProperties().get(URL_PROPERTY_KEY);
        username = (String) endpoint.getProperties().get(USERNAME_PROPERTY_KEY);
        password = (String) endpoint.getProperties().get(PASSWORD_PROPERTY_KEY);
        privateKey = (String) endpoint.getProperties().get(PRIVATE_KEY_PROPERTY_KEY);
        passPhrase = (String) endpoint.getProperties().get(PASS_PHRASE_PROPERTY_KEY);

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
        return "templates/codestream/gerritEndpointReverseGenerate.groovy.peb";
    }

    @Override
    public void dumpAll() {
        boolean failed = false;
        for (com.vmware.devops.client.codestream.stubs.Endpoint endpoint : ReverseGenerationContext
                .getInstance().getVraExportedData().getCodestreamEndpoints()) {
            try {
                if (endpoint.getType().equals(Type.GERRIT)) {
                    dump(endpoint, ReverseGenerationContext.getInstance()
                            .newOutputDirFile(endpoint.getName() + "-gerrit-endpoint.groovy"));
                }
            } catch (Exception e) {
                failed = true;
                log.error("Failed to export gerrit endpoint " + endpoint.getName(), e);
            }
        }

        if (failed) {
            throw new RuntimeException("At least one gerrit endpoint export failed.");
        }
    }
}
