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
import com.vmware.devops.client.codestream.stubs.Endpoint.EmailEncryptionMethod;
import com.vmware.devops.client.codestream.stubs.Endpoint.EmailProtocol;
import com.vmware.devops.client.codestream.stubs.Endpoint.Type;
import com.vmware.devops.client.codestream.stubs.Variable.VariableType;
import com.vmware.devops.model.ReverseGenerationEntity;

/**
 * EmailEndpoint represent entity which extends {@link com.vmware.devops.model.codestream.Endpoint}
 * and specify endpoint of type Email
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@Slf4j
public class EmailEndpoint extends Endpoint implements
        ReverseGenerationEntity<com.vmware.devops.client.codestream.stubs.Endpoint> {
    public static final String SENDER_ADDRESS_PROPERTY_KEY = "senderAddress";
    public static final String ENCRYPTION_METHOD_PROPERTY_KEY = "encryptionMethod";
    public static final String OUTBOUND_HOST_PROPERTY_KEY = "outboundHost";
    public static final String OUTBOUND_PORT_PROPERTY_KEY = "outboundPort";
    public static final String OUTBOUND_PROTOCOL_PROPERTY_KEY = "outboundProtocol";
    public static final String OUTBOUND_USERNAME_PROPERTY_KEY = "outboundUsername";
    public static final String OUTBOUND_PASSWORD_PROPERTY_KEY = "outboundPassword";

    /**
     * The name of the endpoint.
     */
    private String name;

    /**
     * The address of the sender.
     */
    private String senderAddress;

    /**
     * The outbound host of the endpoint.
     */
    private String outboundHost;

    /**
     * The outbound username of the endpoint.
     */
    private String outboundUsername;

    /**
     * The outbound password of the endpoint.
     */
    private String outboundPassword;

    /**
     * The encryption method.
     * <p>
     * By default TLS encryption method is used.
     * See {@link com.vmware.devops.client.codestream.stubs.Endpoint.EmailEncryptionMethod}
     */
    @Builder.Default
    private EmailEncryptionMethod encryptionMethod = EmailEncryptionMethod.TLS;

    /**
     * The outbound port.
     * <p>
     * By default the vaule is 25.
     */
    @Builder.Default
    private int outboundPort = 25;

    /**
     * The outbound protocol of the endpoint.
     * <p>
     * By default SMTP outbound protocol is used.
     * See {@link com.vmware.devops.client.codestream.stubs.Endpoint.EmailProtocol}
     */
    @Builder.Default
    private EmailProtocol outboundProtocol = EmailProtocol.SMTP;

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

        com.vmware.devops.client.codestream.stubs.Endpoint result = com.vmware.devops.client.codestream.stubs.Endpoint
                .builder()
                .name(name)
                .project(project)
                .type(Type.EMAIL)
                .cloudProxyId(cloudProxyId)
                .properties(new HashMap<>(Map.of(
                        SENDER_ADDRESS_PROPERTY_KEY, senderAddress,
                        ENCRYPTION_METHOD_PROPERTY_KEY, encryptionMethod,
                        OUTBOUND_HOST_PROPERTY_KEY, outboundHost,
                        OUTBOUND_PORT_PROPERTY_KEY, outboundPort,
                        OUTBOUND_PROTOCOL_PROPERTY_KEY, outboundProtocol
                )))
                .build();

        result.getProperties().put(OUTBOUND_USERNAME_PROPERTY_KEY, outboundUsername);
        if (outboundPassword != null) {
            result.getProperties()
                    .put(OUTBOUND_PASSWORD_PROPERTY_KEY,
                            Variable.variableReference(getPasswordVariableName()));
        }
        return result;
    }

    public Variable initializePasswordVariable() {
        return Variable.builder()
                .name(getPasswordVariableName())
                .type(VariableType.SECRET)
                .value(outboundPassword)
                .build();
    }

    @Override
    public void generate() throws Exception {
        if (outboundPassword != null) {
            Variable passwordVar = initializePasswordVariable();
            passwordVar.generate();
        }

        super.generate();
    }

    private String getPasswordVariableName() {
        return name + "-endpoint-password";
    }

    @Override
    public void populateData(
            com.vmware.devops.client.codestream.stubs.Endpoint endpoint) {
        name = endpoint.getName();
        project = endpoint.getProject();
        senderAddress = (String) endpoint.getProperties().get(SENDER_ADDRESS_PROPERTY_KEY);
        outboundHost = (String) endpoint.getProperties().get(OUTBOUND_HOST_PROPERTY_KEY);
        outboundUsername = (String) endpoint.getProperties().get(OUTBOUND_USERNAME_PROPERTY_KEY);
        outboundPassword = (String) endpoint.getProperties().get(OUTBOUND_PASSWORD_PROPERTY_KEY);
        encryptionMethod = EmailEncryptionMethod
                .valueOf((String) endpoint.getProperties().get(ENCRYPTION_METHOD_PROPERTY_KEY));
        outboundPort = (Integer) endpoint.getProperties().get(OUTBOUND_PORT_PROPERTY_KEY);
        outboundProtocol = EmailProtocol
                .valueOf((String) endpoint.getProperties().get(OUTBOUND_PROTOCOL_PROPERTY_KEY));

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
        return "templates/codestream/emailEndpointReverseGenerate.groovy.peb";
    }

    @Override
    public void dumpAll() {
        boolean failed = false;
        for (com.vmware.devops.client.codestream.stubs.Endpoint endpoint : ReverseGenerationContext
                .getInstance().getVraExportedData().getCodestreamEndpoints()) {
            try {
                if (endpoint.getType().equals(Type.EMAIL)) {
                    dump(endpoint, ReverseGenerationContext.getInstance()
                            .newOutputDirFile(endpoint.getName() + "-email-endpoint.groovy"));
                }
            } catch (Exception e) {
                failed = true;
                log.error("Failed to export email endpoint " + endpoint.getName(), e);
            }
        }

        if (failed) {
            throw new RuntimeException("At least one email endpoint export failed.");
        }
    }
}
