/*
 * Copyright 2021-2021 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.vmware.devops.codestream;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import com.vmware.devops.ReverseGenerationContext;
import com.vmware.devops.TestUtils;
import com.vmware.devops.Utils;
import com.vmware.devops.client.codestream.stubs.CloudProxy;
import com.vmware.devops.client.codestream.stubs.Endpoint;
import com.vmware.devops.client.codestream.stubs.Endpoint.Type;
import com.vmware.devops.model.VraExportedData;
import com.vmware.devops.model.codestream.AgentEndpoint;
import com.vmware.devops.model.codestream.EmailEndpoint;
import com.vmware.devops.model.codestream.GerritEndpoint;
import com.vmware.devops.model.codestream.JenkinsEndpoint;
import com.vmware.devops.model.codestream.JiraEndpoint;

public class EndpointReverseGenerationTest {

    @Test
    public void testAgent() throws IOException {
        File outputDir = TestUtils.createTempDir();

        CloudProxy cloudProxy = CloudProxy.builder()
                .id("proxy-id")
                .customProperties(Map.of(
                        CloudProxy.PROXY_NAME_KEY, "proxy")
                )
                .build();

        Endpoint endpoint = Endpoint.builder()
                .name("test")
                .type(Type.AGENT)
                .project("project")
                .cloudProxyId("proxy-id")
                .build();

        VraExportedData vraExportedData = new VraExportedData();
        vraExportedData.setCodestreamEndpoints(List.of(
                endpoint
        ));
        vraExportedData.setCloudProxies(List.of(
                cloudProxy
        ));

        ReverseGenerationContext.getInstance().setVraExportedData(vraExportedData);
        ReverseGenerationContext.getInstance().setOutputDir(outputDir.getAbsolutePath());

        new AgentEndpoint().dumpAll();

        String output = Utils
                .readFile(new File(outputDir, "test-agent-endpoint.groovy").getAbsolutePath());
        String expectedOutput = Utils
                .readFile(
                        "tests/codestream/agentEndpointReverseGenerateTestOutput.test.groovy");
        Assert.assertEquals(expectedOutput, output);
    }

    @Test
    public void testJenkins() throws IOException {
        File outputDir = TestUtils.createTempDir();

        CloudProxy cloudProxy = CloudProxy.builder()
                .id("proxy-id")
                .customProperties(Map.of(
                        CloudProxy.PROXY_NAME_KEY, "proxy")
                )
                .build();

        Endpoint endpoint = Endpoint.builder()
                .name("test")
                .type(Type.JENKINS)
                .project("project")
                .cloudProxyId("proxy-id")
                .properties(Map.of(
                        JenkinsEndpoint.URL_PROPERTY_KEY, "url",
                        JenkinsEndpoint.USERNAME_PROPERTY_KEY, "username",
                        JenkinsEndpoint.PASSWORD_PROPERTY_KEY, "password",
                        JenkinsEndpoint.POLL_INTERVAL_PROPERTY_KEY, 3,
                        JenkinsEndpoint.RETRY_COUNT_PROPERTY_KEY, 4,
                        JenkinsEndpoint.RETRY_WAIT_SECONDS_PROPERTY_KEY, 5
                ))
                .build();

        VraExportedData vraExportedData = new VraExportedData();
        vraExportedData.setCodestreamEndpoints(List.of(
                endpoint
        ));
        vraExportedData.setCloudProxies(List.of(
                cloudProxy
        ));

        ReverseGenerationContext.getInstance().setVraExportedData(vraExportedData);
        ReverseGenerationContext.getInstance().setOutputDir(outputDir.getAbsolutePath());

        new JenkinsEndpoint().dumpAll();

        String output = Utils
                .readFile(new File(outputDir, "test-jenkins-endpoint.groovy").getAbsolutePath());
        String expectedOutput = Utils
                .readFile(
                        "tests/codestream/jenkinsEndpointReverseGenerateTestOutput.test.groovy");
        Assert.assertEquals(expectedOutput, output);
    }

    @Test
    public void testJira() throws IOException {
        File outputDir = TestUtils.createTempDir();

        CloudProxy cloudProxy = CloudProxy.builder()
                .id("proxy-id")
                .customProperties(Map.of(
                        CloudProxy.PROXY_NAME_KEY, "proxy")
                )
                .build();

        Endpoint endpoint = Endpoint.builder()
                .name("test")
                .type(Type.JIRA)
                .project("project")
                .cloudProxyId("proxy-id")
                .properties(Map.of(
                        JiraEndpoint.URL_PROPERTY_KEY, "url",
                        JiraEndpoint.USERNAME_PROPERTY_KEY, "username",
                        JiraEndpoint.PASSWORD_PROPERTY_KEY, "password"
                ))
                .build();

        VraExportedData vraExportedData = new VraExportedData();
        vraExportedData.setCodestreamEndpoints(List.of(
                endpoint
        ));
        vraExportedData.setCloudProxies(List.of(
                cloudProxy
        ));

        ReverseGenerationContext.getInstance().setVraExportedData(vraExportedData);
        ReverseGenerationContext.getInstance().setOutputDir(outputDir.getAbsolutePath());

        new JiraEndpoint().dumpAll();

        String output = Utils
                .readFile(new File(outputDir, "test-jira-endpoint.groovy").getAbsolutePath());
        String expectedOutput = Utils
                .readFile(
                        "tests/codestream/jiraEndpointReverseGenerateTestOutput.test.groovy");
        Assert.assertEquals(expectedOutput, output);
    }

    @Test
    public void testGerrit() throws IOException {
        File outputDir = TestUtils.createTempDir();

        CloudProxy cloudProxy = CloudProxy.builder()
                .id("proxy-id")
                .customProperties(Map.of(
                        CloudProxy.PROXY_NAME_KEY, "proxy")
                )
                .build();

        Endpoint endpoint = Endpoint.builder()
                .name("test")
                .type(Type.GERRIT)
                .project("project")
                .cloudProxyId("proxy-id")
                .properties(Map.of(
                        GerritEndpoint.URL_PROPERTY_KEY, "url",
                        GerritEndpoint.USERNAME_PROPERTY_KEY, "username",
                        GerritEndpoint.PASSWORD_PROPERTY_KEY, "password",
                        GerritEndpoint.PASS_PHRASE_PROPERTY_KEY, "passphrase",
                        GerritEndpoint.PRIVATE_KEY_PROPERTY_KEY, "privateKey"
                ))
                .build();

        VraExportedData vraExportedData = new VraExportedData();
        vraExportedData.setCodestreamEndpoints(List.of(
                endpoint
        ));
        vraExportedData.setCloudProxies(List.of(
                cloudProxy
        ));

        ReverseGenerationContext.getInstance().setVraExportedData(vraExportedData);
        ReverseGenerationContext.getInstance().setOutputDir(outputDir.getAbsolutePath());

        new GerritEndpoint().dumpAll();

        String output = Utils
                .readFile(new File(outputDir, "test-gerrit-endpoint.groovy").getAbsolutePath());
        String expectedOutput = Utils
                .readFile(
                        "tests/codestream/gerritEndpointReverseGenerateTestOutput.test.groovy");
        Assert.assertEquals(expectedOutput, output);
    }

    @Test
    public void testEmail() throws IOException {
        File outputDir = TestUtils.createTempDir();

        CloudProxy cloudProxy = CloudProxy.builder()
                .id("proxy-id")
                .customProperties(Map.of(
                        CloudProxy.PROXY_NAME_KEY, "proxy")
                )
                .build();

        Endpoint endpoint = Endpoint.builder()
                .name("test")
                .type(Type.EMAIL)
                .project("project")
                .cloudProxyId("proxy-id")
                .properties(Map.of(
                        EmailEndpoint.SENDER_ADDRESS_PROPERTY_KEY, "senderAddress",
                        EmailEndpoint.ENCRYPTION_METHOD_PROPERTY_KEY, "TLS",
                        EmailEndpoint.OUTBOUND_HOST_PROPERTY_KEY, "outboundHost",
                        EmailEndpoint.OUTBOUND_PORT_PROPERTY_KEY, 25,
                        EmailEndpoint.OUTBOUND_PROTOCOL_PROPERTY_KEY, "SMTP",
                        EmailEndpoint.OUTBOUND_USERNAME_PROPERTY_KEY, "outboundUsername",
                        EmailEndpoint.OUTBOUND_PASSWORD_PROPERTY_KEY, "outboundPassword"
                ))
                .build();

        VraExportedData vraExportedData = new VraExportedData();
        vraExportedData.setCodestreamEndpoints(List.of(
                endpoint
        ));
        vraExportedData.setCloudProxies(List.of(
                cloudProxy
        ));

        ReverseGenerationContext.getInstance().setVraExportedData(vraExportedData);
        ReverseGenerationContext.getInstance().setOutputDir(outputDir.getAbsolutePath());

        new EmailEndpoint().dumpAll();

        String output = Utils
                .readFile(new File(outputDir, "test-email-endpoint.groovy").getAbsolutePath());
        String expectedOutput = Utils
                .readFile(
                        "tests/codestream/emailEndpointReverseGenerateTestOutput.test.groovy");
        Assert.assertEquals(expectedOutput, output);
    }
}
