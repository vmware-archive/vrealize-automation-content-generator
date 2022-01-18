/*
 * Copyright 2021-2021 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.vmware.devops.codestream;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.vmware.devops.ClientTestBase;
import com.vmware.devops.TestUtils;
import com.vmware.devops.client.Client;
import com.vmware.devops.client.cloudassembly.infrastructure.stubs.Project;
import com.vmware.devops.client.codestream.CodestreamClient;
import com.vmware.devops.client.codestream.stubs.CloudProxy;
import com.vmware.devops.client.codestream.stubs.Endpoint;
import com.vmware.devops.client.codestream.stubs.Entity;
import com.vmware.devops.client.codestream.stubs.GerritListener;
import com.vmware.devops.client.codestream.stubs.GerritTrigger;
import com.vmware.devops.client.codestream.stubs.Pipeline;
import com.vmware.devops.client.codestream.stubs.Stage;
import com.vmware.devops.client.codestream.stubs.Task;
import com.vmware.devops.client.codestream.stubs.Task.Type;
import com.vmware.devops.client.codestream.stubs.Task.UserOperationInput;
import com.vmware.devops.client.codestream.stubs.Variable;
import com.vmware.devops.client.codestream.stubs.Variable.VariableType;

public class CodestreamClientIT extends ClientTestBase {

    private static Project testProject;
    private List<Endpoint> endpoints = new ArrayList<>();
    private List<Pipeline> pipelines = new ArrayList<>();
    private List<Variable> variables = new ArrayList<>();
    private List<GerritTrigger> gerritTriggers = new ArrayList<>();

    @BeforeClass
    public static void setup() throws InterruptedException, IOException, URISyntaxException {
        testProject = getInfrastructureClient().createProject(Project.builder()
                .name("vrealize-automation-content-generator-test-" + System.currentTimeMillis())
                .build());
    }

    @Test
    public void loginTest() throws InterruptedException, IOException, URISyntaxException {
        CodestreamClient client = getCodestreamClient();
        String token = client.getAccessToken();
        Assert.assertNotNull(token);
        String newToken = client.getAccessToken();
        Assert.assertEquals(token, newToken); // Assert token is cached
    }

    @Test
    public void createDeletePipelineTest()
            throws InterruptedException, IOException, URISyntaxException {
        CodestreamClient client = getCodestreamClient();
        Pipeline p = client
                .createPipeline(getTestPipeline());
        Pipeline fetchedPipeline = client
                .fetchPipelineByProjectAndPipelineName(p.getProject(), p.getName());
        client.deletePipeline(fetchedPipeline.getId());
    }

    @Test
    public void createOrUpdatePipelineTest()
            throws InterruptedException, IOException, URISyntaxException {
        CodestreamClient client = getCodestreamClient();
        Pipeline p = getTestPipeline();
        p = client
                .createPipeline(p);
        p = client.createOrUpdatePipeline(p);
        Pipeline fetchedPipeline = client
                .fetchPipeline(p.getId());
        client.deletePipeline(fetchedPipeline.getId());
    }

    @Test
    public void createOrUpdateAndEditPipelineTest()
            throws InterruptedException, IOException, URISyntaxException {
        CodestreamClient client = getCodestreamClient();
        Pipeline pipelineToBeCreated = getTestPipeline();
        Pipeline p = client
                .createPipeline(pipelineToBeCreated);
        pipelineToBeCreated.setConcurrency(50);
        p = client.createOrUpdatePipeline(pipelineToBeCreated);
        Pipeline fetchedPipeline = client
                .fetchPipeline(p.getId());
        Assert.assertEquals(50, fetchedPipeline.getConcurrency());
        client.deletePipeline(fetchedPipeline.getId());
    }

    @Test
    public void findPipelineTest()
            throws InterruptedException, IOException, URISyntaxException {
        CodestreamClient client = getCodestreamClient();
        Pipeline p = client
                .createPipeline(getTestPipeline());
        Pipeline foundPipeline = client
                .findPipelineByName(p.getName());
        client.deletePipeline(foundPipeline.getId());
    }

    @Test
    public void createDisabledPipelineTest()
            throws InterruptedException, IOException, URISyntaxException {
        CodestreamClient client = getCodestreamClient();
        Pipeline p = client
                .createPipeline(getTestPipeline());
        Pipeline fetchedPipeline = client
                .fetchPipelineByProjectAndPipelineName(p.getProject(), p.getName());
        Assert.assertEquals(false, fetchedPipeline.isEnabled());
        client.deletePipeline(fetchedPipeline.getId());
    }

    @Test
    public void createEnabledPipelineTest()
            throws InterruptedException, IOException, URISyntaxException {
        CodestreamClient client = getCodestreamClient();
        Pipeline p = getValidTestPipeline();
        p.setEnabled(true);
        p = client.createPipeline(p);
        Pipeline fetchedPipeline = client.fetchPipeline(p.getId());
        Assert.assertEquals(true, fetchedPipeline.isEnabled());
        client.deletePipeline(fetchedPipeline.getId());
    }

    @Test
    public void createUpdateDeletePipelineTest()
            throws InterruptedException, IOException, URISyntaxException {
        CodestreamClient client = getCodestreamClient();
        Pipeline p = client
                .createPipeline(getTestPipeline());
        Pipeline fetchedPipeline = client.fetchPipeline(p.getId());
        fetchedPipeline.setConcurrency(10);
        Pipeline updatedPipeline = client.updatePipeline(fetchedPipeline);
        Assert.assertEquals(10, updatedPipeline.getConcurrency());
        client.deletePipeline(fetchedPipeline.getId());
    }

    @Test
    public void createEnableDeletePipelineTest()
            throws InterruptedException, IOException, URISyntaxException {
        CodestreamClient client = getCodestreamClient();
        Pipeline p = client.createPipeline(getValidTestPipeline());
        Pipeline fetchedPipeline = client
                .fetchPipelineByProjectAndPipelineName(p.getProject(), p.getName());
        fetchedPipeline.setEnabled(true);
        client.updatePipeline(fetchedPipeline);
        client.deletePipeline(fetchedPipeline.getId());
    }

    @Test
    public void testGetAllPipelines()
            throws IOException, InterruptedException, URISyntaxException {
        int pipelinesCount = 10;
        for (int i = 0; i < pipelinesCount; i++) {
            String name = "vrealize-automation-content-generator-test-" + i + "-" + System.currentTimeMillis();
            Pipeline pipeline = getCodestreamClient().createPipeline(Pipeline.builder()
                    .project(testProject.getName())
                    .name(name)
                    .enabled(false)
                    .build()
            );
            Assert.assertNotNull(pipeline.getId());
            pipelines.add(pipeline);
        }
        List<Pipeline> allPipelines = getCodestreamClient().getAllPipelines();
        Assert.assertTrue(allPipelines.size() >= pipelinesCount);
        List<String> expectedPipelineIds = pipelines.stream()
                .map(Entity::getId)
                .collect(Collectors.toList());
        List<Pipeline> expectedPipelines = allPipelines.stream()
                .filter(pipeline -> expectedPipelineIds.contains(pipeline.getId()))
                .collect(Collectors.toList());
        Assert.assertEquals(pipelinesCount, expectedPipelines.size());
    }

    @Test
    public void testGetAllVariables()
            throws IOException, InterruptedException, URISyntaxException {
        int variablesCount = 10;
        for (int i = 0; i < variablesCount; i++) {
            String name = "vrealize-automation-content-generator-test-" + i + "-" + System.currentTimeMillis();
            Variable variable = getCodestreamClient().createVariable(Variable.builder()
                    .project(testProject.getName())
                    .name(name)
                    .type(VariableType.REGULAR)
                    .value("test-value")
                    .build()
            );
            Assert.assertNotNull(variable.getId());
            variables.add(variable);
        }
        List<Variable> allVariables = getCodestreamClient().getAllVariables();
        Assert.assertTrue(allVariables.size() >= variablesCount);
        List<String> expectedVariableIds = variables.stream()
                .map(Entity::getId)
                .collect(Collectors.toList());
        List<Variable> expectedVariables = allVariables.stream()
                .filter(variable -> expectedVariableIds.contains(variable.getId()))
                .collect(Collectors.toList());
        Assert.assertEquals(variablesCount, expectedVariables.size());
    }

    @Test
    public void createDeleteGerritTriggerTest()
            throws InterruptedException, IOException, URISyntaxException {
        CodestreamClient client = getCodestreamClient();

        String endpointUrl = TestUtils.getProperty("gerrit.url");
        String fingerprint = getCodestreamClient().getEndpointCertificate(endpointUrl, null)
                .getCertificates().get(0).getFingerprints().get("SHA-256");
        String name = "vrealize-automation-content-generator-test-" + System.currentTimeMillis();
        Endpoint endpoint = Endpoint.builder()
                .name(name)
                .project(testProject.getName())
                .type(Endpoint.Type.GERRIT)
                .properties(Map.of(
                        "url", endpointUrl,
                        "username", TestUtils.getProperty("gerrit.username"),
                        "password", TestUtils.getProperty("gerrit.password"),
                        "privateKey", TestUtils.getProperty("gerrit.privateKey"),
                        "fingerprint", fingerprint
                ))
                .build();
        endpoint = getCodestreamClient().createEndpoint(endpoint);
        endpoints.add(endpoint);

        GerritListener l = GerritListener.builder()
                .project(testProject.getName())
                .name("vrealize-automation-content-generator-listener-" + System.currentTimeMillis())
                .endpoint(endpoint.getName())
                .apiToken(getClient().getRefreshToken())
                .connected(true)
                .build();
        l = client.createGerritListener(l);
        Assert.assertEquals(true, l.isConnected());
        GerritTrigger t = client
                .createGerritTrigger(GerritTrigger.builder()
                        .project(testProject.getName())
                        .name("vrealize-automation-content-generator-trigger-" + System.currentTimeMillis())
                        .listener(l.getName())
                        .gerritProject(TestUtils.getProperty("gerrit.project.name"))
                        .branch(TestUtils.getProperty("gerrit.project.branch"))
                        .enabled(true)
                        .build());
        Assert.assertTrue(t.isEnabled());
        client.deleteGerritTrigger(t.getId());
        client.connectGerritListener(l.getId(), false);
        client.deleteGerritListener(l.getId());
    }

    @Test
    public void createUpdateDeleteGerritTriggerTest()
            throws InterruptedException, IOException, URISyntaxException {
        CodestreamClient client = getCodestreamClient();
        GerritListener l = client
                .createGerritListener(getTestListener());
        GerritTrigger t = client
                .createGerritTrigger(GerritTrigger.builder()
                        .project(testProject.getName())
                        .name("vrealize-automation-content-generator-trigger-" + System.currentTimeMillis())
                        .listener(l.getName())
                        .gerritProject(TestUtils.getProperty("gerrit.project.name"))
                        .branch(TestUtils.getProperty("gerrit.project.branch"))
                        .build());
        GerritTrigger fetchedTrigger = client.fetchGerritTrigger(t.getId());
        fetchedTrigger.setBranch("saas/cas/next");
        GerritTrigger updatedTrigger = client.updateGerritTrigger(fetchedTrigger);
        Assert.assertEquals("saas/cas/next", updatedTrigger.getBranch());
        Assert.assertEquals(false, fetchedTrigger.isEnabled());
        client.deleteGerritTrigger(updatedTrigger.getId());
        client.deleteGerritListener(l.getId());
    }

    @Test
    public void createFindUpdateDeleteGerritTriggerTest()
            throws InterruptedException, IOException, URISyntaxException {
        CodestreamClient client = getCodestreamClient();
        GerritListener l = client
                .createGerritListener(getTestListener());
        GerritTrigger t = client
                .createGerritTrigger(GerritTrigger.builder()
                        .project(testProject.getName())
                        .name("vrealize-automation-content-generator-trigger-" + System.currentTimeMillis())
                        .listener(l.getName())
                        .gerritProject(TestUtils.getProperty("gerrit.project.name"))
                        .branch(TestUtils.getProperty("gerrit.project.branch"))
                        .build());
        GerritTrigger fetchedTrigger = client.findGerritTriggerrByName(t.getName());
        fetchedTrigger.setBranch("saas/cas/next");
        GerritTrigger updatedTrigger = client.updateGerritTrigger(fetchedTrigger);
        Assert.assertEquals("saas/cas/next", updatedTrigger.getBranch());
        Assert.assertEquals(false, fetchedTrigger.isEnabled());
        client.deleteGerritTrigger(updatedTrigger.getId());
        client.deleteGerritListener(l.getId());
    }

    @Test
    public void testGetAllGerritTriggers()
            throws InterruptedException, IOException, URISyntaxException {
        CodestreamClient client = getCodestreamClient();
        GerritListener l = client
                .createGerritListener(getTestListener());
        int gerritTriggersCount = 10;
        for (int i = 0; i < gerritTriggersCount; i++) {
            String name = "vrealize-automation-content-generator-trigger-" + i + "-" + System.currentTimeMillis();
            GerritTrigger gerritTrigger = client
                    .createGerritTrigger(GerritTrigger.builder()
                            .project(testProject.getName())
                            .name(name)
                            .listener(l.getName())
                            .gerritProject(TestUtils.getProperty("gerrit.project.name"))
                            .branch(TestUtils.getProperty("gerrit.project.branch"))
                            .build());
            Assert.assertNotNull(gerritTrigger.getId());
            gerritTriggers.add(gerritTrigger);
        }

        List<GerritTrigger> allGerritTriggers = getCodestreamClient().getAllGerritTriggers();
        Assert.assertTrue(allGerritTriggers.size() >= gerritTriggersCount);
        List<String> expectedGerritTriggersIds = gerritTriggers.stream()
                .map(GerritTrigger::getId)
                .collect(Collectors.toList());
        List<GerritTrigger> expectedGerritTriggers = allGerritTriggers.stream()
                .filter(gerritTrigger -> expectedGerritTriggersIds.contains(gerritTrigger.getId()))
                .collect(Collectors.toList());
        Assert.assertEquals(gerritTriggersCount, expectedGerritTriggers.size());
        for (GerritTrigger gerritTrigger : gerritTriggers) {
            getCodestreamClient().deleteGerritTrigger(gerritTrigger.getId());
        }
        gerritTriggers = new ArrayList<>();
        client.deleteGerritListener(l.getId());
    }

    @Test
    public void createDeleteVariableTest()
            throws InterruptedException, IOException, URISyntaxException {
        CodestreamClient client = getCodestreamClient();
        Variable v = client
                .createVariable(getTestVariable());
        client.deleteVariable(v.getId());
    }

    @Test
    public void createUpdateDeleteVariableTest()
            throws InterruptedException, IOException, URISyntaxException {
        CodestreamClient client = getCodestreamClient();
        Variable v = client
                .createVariable(getTestVariable());
        Assert.assertEquals("test-vrealize-automation-content-generator-var-value", v.getValue());
        v.setValue("new-value");
        Variable updatedVariable = client.updateVariable(v);
        Assert.assertEquals("new-value", updatedVariable.getValue());
        client.deleteVariable(v.getId());
    }

    @Test
    public void createDeleteGerritListenerTest()
            throws InterruptedException, IOException, URISyntaxException {
        CodestreamClient client = getCodestreamClient();
        GerritListener l = client
                .createGerritListener(getTestListener());
        client.deleteGerritListener(l.getId());
    }

    @Test
    public void createUpdateDeleteGerritListenerTest()
            throws InterruptedException, IOException, URISyntaxException {
        CodestreamClient client = getCodestreamClient();
        GerritListener l = client
                .createOrUpdateGerritListener(getTestListener());
        Assert.assertEquals("vrealize-automation-content-generator-endpoint", l.getEndpoint());
        Assert.assertEquals("vrealize-automation-content-generator-api-token", l.getApiToken());
        l.setEndpoint("vrealize-automation-content-generator-endpoint-updated");
        l.setApiToken("vrealize-automation-content-generator-api-token-updated");
        GerritListener updatedGerritListener = client.createOrUpdateGerritListener(l);
        Assert.assertEquals("vrealize-automation-content-generator-endpoint-updated", updatedGerritListener.getEndpoint());
        Assert.assertEquals("vrealize-automation-content-generator-api-token-updated", updatedGerritListener.getApiToken());
        l.setId(null);
        client.createOrUpdateGerritListener(l);
        client.deleteGerritListener(updatedGerritListener.getId());
    }

    @Test
    public void createEndpoint() throws InterruptedException, IOException, URISyntaxException {
        String endpointUrl = TestUtils.getProperty("jenkins.url");
        String fingerprint = getCodestreamClient().getEndpointCertificate(endpointUrl, null)
                .getCertificates().get(0).getFingerprints().get("SHA-256");
        String name = "vrealize-automation-content-generator-test-" + System.currentTimeMillis();
        Endpoint endpoint = new Endpoint()
                .builder()
                .name(name)
                .project(testProject.getName())
                .type(Endpoint.Type.JENKINS)
                .properties(Map.of(
                        "pollInterval", 15,
                        "retryCount", 5,
                        "retryWaitSeconds", 60,
                        "url", endpointUrl,
                        "username", "test",
                        "password", "xxxx",
                        "finderprint", fingerprint
                ))
                .build();
        endpoint = getCodestreamClient().createEndpoint(endpoint);
        endpoints.add(endpoint);

        Assert.assertNotNull(endpoint.getId());
        Assert.assertEquals(name, endpoint.getName());

        Endpoint found = getCodestreamClient().findEndpointByName(name);
        Assert.assertEquals(endpoint.getId(), found.getId());
    }

    @Test
    public void createOrUpdateEndpoint()
            throws InterruptedException, IOException, URISyntaxException {
        String endpointUrl = TestUtils.getProperty("jenkins.url");
        String fingerprint = getCodestreamClient().getEndpointCertificate(endpointUrl, null)
                .getCertificates().get(0).getFingerprints().get("SHA-256");
        String name = "vrealize-automation-content-generator-test-" + System.currentTimeMillis();
        Endpoint endpoint = Endpoint.builder()
                .name(name)
                .project(testProject.getName())
                .type(Endpoint.Type.JENKINS)
                .properties(Map.of(
                        "pollInterval", 15,
                        "retryCount", 5,
                        "retryWaitSeconds", 60,
                        "url", endpointUrl,
                        "username", "test",
                        "password", "xxxx",
                        "finderprint", fingerprint
                ))
                .build();
        endpoint = getCodestreamClient().createOrUpdateEndpoint(endpoint);
        endpoints.add(endpoint);

        String id = endpoint.getId();

        endpoint.getProperties().put("retryCount", 10);
        endpoint = getCodestreamClient().createOrUpdateEndpoint(endpoint);
        Assert.assertEquals(id, endpoint.getId());
        Assert.assertEquals(10, endpoint.getProperties().get("retryCount"));
    }

    @Test
    @Ignore("This can only run on the cloud and requires some infra")
    public void findCloudProxyByName()
            throws InterruptedException, IOException, URISyntaxException {
        CloudProxy cloudProxy = new Client("https://console.cloud.vmware.com",
                "https://api.mgmt.cloud.vmware.com",
                "xxxxx")
                .getCodestream()
                .findCloudProxyByName("ip-172-31-20-194");
        Assert.assertNotNull(cloudProxy.getId());
    }

    @Test
    @Ignore("This can only run on the cloud and requires some infra")
    public void findAllCloudProxies()
            throws InterruptedException, IOException, URISyntaxException {
        List<CloudProxy> cloudProxies = new Client("https://console.cloud.vmware.com",
                "https://api.mgmt.cloud.vmware.com",
                "xxxxx")
                .getCodestream()
                .getAllCloudProxies();
        Assert.assertTrue(cloudProxies.size() > 0);

        boolean found = false;
        for (CloudProxy c : cloudProxies) {
            if (CodestreamClient.getProxyName(c).equals("ip-172-31-20-194")) {
                found = true;
            }
        }
        Assert.assertTrue(found);
    }

    @Test
    public void testGetAllEndpoints()
            throws IOException, InterruptedException, URISyntaxException {
        String endpointUrl = TestUtils.getProperty("gerrit.url");
        String fingerprint = getCodestreamClient().getEndpointCertificate(endpointUrl, null)
                .getCertificates().get(0).getFingerprints().get("SHA-256");

        int endpointsCount = 10;
        for (int i = 0; i < endpointsCount; i++) {
            String name = "vrealize-automation-content-generator-test-" + i + "-" + System.currentTimeMillis();
            Endpoint endpoint = getCodestreamClient().createEndpoint(Endpoint.builder()
                    .name(name)
                    .project(testProject.getName())
                    .type(Endpoint.Type.GERRIT)
                    .properties(Map.of(
                            "url", endpointUrl,
                            "username", TestUtils.getProperty("gerrit.username"),
                            "password", TestUtils.getProperty("gerrit.password"),
                            "privateKey", TestUtils.getProperty("gerrit.privateKey"),
                            "fingerprint", fingerprint
                    ))
                    .build()
            );
            Assert.assertNotNull(endpoint.getId());
            endpoints.add(endpoint);
        }
        List<Endpoint> allEndpoints = getCodestreamClient().getAllEndpoints();
        Assert.assertTrue(allEndpoints.size() >= endpointsCount);
        List<String> expectedEndpointIds = endpoints.stream()
                .map(Endpoint::getId)
                .collect(Collectors.toList());
        List<Endpoint> expectedEndpoints = allEndpoints.stream()
                .filter(pipeline -> expectedEndpointIds.contains(pipeline.getId()))
                .collect(Collectors.toList());
        Assert.assertEquals(endpointsCount, expectedEndpoints.size());
    }

    @After
    public void clean() throws InterruptedException, IOException, URISyntaxException {
        if (!pipelines.isEmpty()) {
            for (Pipeline pipeline : pipelines) {
                getCodestreamClient().deletePipeline(pipeline.getId());
            }
            pipelines = new ArrayList<>();
        }
        if (!endpoints.isEmpty()) {
            for (Endpoint endpoint : endpoints) {
                getCodestreamClient().deleteEndpoint(endpoint.getId());
            }
            endpoints = new ArrayList<>();
        }
        if (!variables.isEmpty()) {
            for (Variable variable : variables) {
                getCodestreamClient().deleteVariable(variable.getId());
            }
            variables = new ArrayList<>();
        }
    }

    @AfterClass
    public static void cleanup() throws InterruptedException, IOException, URISyntaxException {
        getInfrastructureClient().deleteProject(testProject.getId());
    }

    private GerritListener getTestListener() {
        return GerritListener.builder()
                .project(testProject.getName())
                .name("vrealize-automation-content-generator-listener-" + System.currentTimeMillis())
                .endpoint("vrealize-automation-content-generator-endpoint")
                .apiToken("vrealize-automation-content-generator-api-token")
                .build();
    }

    private GerritTrigger getTestTrigger() {
        return GerritTrigger.builder()
                .project(testProject.getName())
                .name("vrealize-automation-content-generator-trigger-" + System.currentTimeMillis())
                .listener("test-gerrit")
                .gerritProject(TestUtils.getProperty("gerrit.project.name"))
                .branch(TestUtils.getProperty("gerrit.project.branch"))
                .build();
    }

    private Pipeline getTestPipeline() {
        return Pipeline.builder()
                .project(testProject.getName())
                .name("test-vrealize-automation-content-generator-" + System.currentTimeMillis())
                .enabled(false)
                .concurrency(20)
                .build();

    }

    private Variable getTestVariable() {
        return Variable.builder()
                .project(testProject.getName())
                .name("test-vrealize-automation-content-generator-var-" + System.currentTimeMillis())
                .value("test-vrealize-automation-content-generator-var-value")
                .type(Variable.VariableType.REGULAR)
                .build();
    }

    private Pipeline getValidTestPipeline() {
        return Pipeline.builder()
                .project(testProject.getName())
                .name("test-vrealize-automation-content-generator-" + System.currentTimeMillis())
                .enabled(false)
                .concurrency(20)
                .stageOrder(List.of("stage-1"))
                .stages(Map.of(
                        "stage-1", Stage.builder()
                                .tasks(Map.of(
                                        "task-1", Task.builder()
                                                .type(Type.USER_OPERATION)
                                                .ignoreFailure(false)
                                                .preCondition("true")
                                                .input(UserOperationInput.builder()
                                                        .summary("test")
                                                        .approvers(List.of("admin@vmware.com"))
                                                        .build())
                                                .build()
                                ))
                                .build()
                ))
                .build();

    }
}
