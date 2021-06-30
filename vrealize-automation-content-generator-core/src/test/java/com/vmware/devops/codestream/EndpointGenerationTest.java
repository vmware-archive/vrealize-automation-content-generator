/*
 * Copyright 2021-2021 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.vmware.devops.codestream;

import java.io.IOException;
import java.net.URISyntaxException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vmware.devops.GenerationContext;
import com.vmware.devops.GenerationTestBase;
import com.vmware.devops.IdCache;
import com.vmware.devops.SerializationUtils;
import com.vmware.devops.SpecProcessor;
import com.vmware.devops.Utils;
import com.vmware.devops.config.EndpointConfiguration.AuthenticationDetails;
import com.vmware.devops.model.codestream.AgentEndpoint;
import com.vmware.devops.model.codestream.EmailEndpoint;
import com.vmware.devops.model.codestream.Endpoint;
import com.vmware.devops.model.codestream.GerritEndpoint;
import com.vmware.devops.model.codestream.JenkinsEndpoint;
import com.vmware.devops.model.codestream.JiraEndpoint;
import com.vmware.devops.model.codestream.Variable;

public class EndpointGenerationTest extends GenerationTestBase {
    @Before
    public void setup() {
        Endpoint.ENDPOINT_FINGERPRINT_CACHE.getUrlToFingerprint()
                .put("https://dummy.eng.vmware.com", "fakeFingerprint");
        IdCache.CODESTREAM_CLOUD_PROXY_ID_CACHE.getNameToId().put("testProxyName", "fakeId");
        GenerationContext.getInstance().getEndpointConfiguration()
                .setAuthenticationDetails(new AuthenticationDetails("fakeToken"));
    }

    @Test
    public void jenkinsEndpointTest() throws IOException, URISyntaxException, InterruptedException {
        SpecProcessor specProcessor = new SpecProcessor();
        JenkinsEndpoint endpoint = (JenkinsEndpoint) specProcessor
                .process(
                        Utils.readFile("tests/codestream/jenkinsEndpointTest.groovy"));

        String output = SerializationUtils
                .prettifyJson(SerializationUtils.toPrettyJson(endpoint.initializeEndpoint()));
        String expectedOutput = Utils
                .readFile("tests/codestream/jenkinsEndpointTestOutput.json");
        Assert.assertEquals(expectedOutput, output);

        Variable variable = endpoint.initializePasswordVariable();
        Assert.assertEquals("test-endpoint-password", variable.getName());
        Assert.assertEquals("world", variable.getValue());
    }

    @Test
    public void jiraEndpointTest() throws Exception {
        SpecProcessor specProcessor = new SpecProcessor();
        JiraEndpoint endpoint = (JiraEndpoint) specProcessor
                .process(
                        Utils.readFile("tests/codestream/jiraEndpointTest.groovy"));

        String output = SerializationUtils
                .prettifyJson(SerializationUtils.toPrettyJson(endpoint.initializeEndpoint()));
        String expectedOutput = Utils
                .readFile("tests/codestream/jiraEndpointTestOutput.json");
        Assert.assertEquals(expectedOutput, output);

        Variable variable = endpoint.initializePasswordVariable();
        Assert.assertEquals("test-endpoint-password", variable.getName());
        Assert.assertEquals("pass", variable.getValue());
    }

    @Test
    public void jenkinsNonSecureEndpointTest() throws IOException, URISyntaxException, InterruptedException {
        SpecProcessor specProcessor = new SpecProcessor();
        JenkinsEndpoint endpoint = (JenkinsEndpoint) specProcessor
                .process(
                        Utils.readFile("tests/codestream/jenkinsNonSecureEndpointTest.groovy"));

        String output = SerializationUtils
                .prettifyJson(SerializationUtils.toPrettyJson(endpoint.initializeEndpoint()));
        String expectedOutput = Utils
                .readFile("tests/codestream/jenkinsNonSecureEndpointTestOutput.json");
        Assert.assertEquals(expectedOutput, output);

        Variable variable = endpoint.initializePasswordVariable();
        Assert.assertEquals("test-endpoint-password", variable.getName());
        Assert.assertEquals("world", variable.getValue());
    }

    @Test
    public void gerritEndpointTest() throws Exception {
        SpecProcessor specProcessor = new SpecProcessor();
        GerritEndpoint endpoint = (GerritEndpoint) specProcessor
                .process(
                        Utils.readFile("tests/codestream/gerritEndpointTest.groovy"));

        String output = SerializationUtils
                .prettifyJson(SerializationUtils.toPrettyJson(endpoint.initializeEndpoint()));
        String expectedOutput = Utils
                .readFile("tests/codestream/gerritEndpointTestOutput.json");
        Assert.assertEquals(expectedOutput, output);

        Variable passVariable = endpoint.initializePasswordVariable();
        Assert.assertEquals("test-endpoint-password", passVariable.getName());
        Assert.assertEquals("world", passVariable.getValue());

        Variable phraseVariable = endpoint.initializePassPhaseVariable();
        Assert.assertEquals("test-endpoint-pass-phrase", phraseVariable.getName());
        Assert.assertEquals("passphrase", phraseVariable.getValue());

        Variable apiTokenVariable = endpoint.initializeListenerApiTokenVariable();
        Assert.assertEquals("test-endpoint-listen-token", apiTokenVariable.getName());
        Assert.assertEquals("fakeToken", apiTokenVariable.getValue());

        output = SerializationUtils
                .prettifyJson(SerializationUtils.toPrettyJson(endpoint.initializeListener()));
        expectedOutput = Utils
                .readFile("tests/codestream/gerritEndpointListenerTestOutput.json");
        Assert.assertEquals(expectedOutput, output);
    }

    @Test
    public void agentEndpointTest() throws Exception {
        SpecProcessor specProcessor = new SpecProcessor();
        AgentEndpoint endpoint = (AgentEndpoint) specProcessor
                .process(
                        Utils.readFile("tests/codestream/agentEndpointTest.groovy"));

        String output = SerializationUtils
                .prettifyJson(SerializationUtils.toPrettyJson(endpoint.initializeEndpoint()));
        String expectedOutput = Utils
                .readFile("tests/codestream/agentEndpointTestOutput.json");
        Assert.assertEquals(expectedOutput, output);
    }

    @Test
    public void emailEndpointTest() throws Exception {
        SpecProcessor specProcessor = new SpecProcessor();
        EmailEndpoint endpoint = (EmailEndpoint) specProcessor
                .process(
                        Utils.readFile("tests/codestream/emailEndpointTest.groovy"));

        String output = SerializationUtils
                .prettifyJson(SerializationUtils.toPrettyJson(endpoint.initializeEndpoint()));
        String expectedOutput = Utils
                .readFile("tests/codestream/emailEndpointTestOutput.json");

        Variable variable = endpoint.initializePasswordVariable();
        Assert.assertEquals("test-endpoint-password", variable.getName());
        Assert.assertEquals("password", variable.getValue());
        Assert.assertEquals(expectedOutput, output);
    }
}
