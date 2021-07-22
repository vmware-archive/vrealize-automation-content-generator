/*
 * Copyright 2021-2021 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.vmware.devops.servicebroker;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import com.vmware.devops.ClientTestBase;
import com.vmware.devops.client.servicebroker.ServiceBrokerClient;
import com.vmware.devops.client.servicebroker.stubs.Policy;
import com.vmware.devops.client.servicebroker.stubs.Policy.EnforcementType;
import com.vmware.devops.client.servicebroker.stubs.Policy.LeaseDefinition;
import com.vmware.devops.client.servicebroker.stubs.Policy.Type;

public class ServiceBrokerClientIT extends ClientTestBase {

    private List<Policy> policies = new ArrayList<>();

    @Test
    public void createUpdatePolicyTest()
            throws InterruptedException, IOException, URISyntaxException {
        ServiceBrokerClient client = getServiceBrokerClient();
        String name = "test-" + System.currentTimeMillis();
        Policy policy = Policy.builder()
                .name(name)
                .typeId(Type.LEASE)
                .definition(LeaseDefinition.builder()
                        .leaseGrace(1)
                        .leaseTotalTermMax(3)
                        .leaseTermMax(5)
                        .build()
                )
                .build();
        policy = client.createPolicy(policy);
        policies.add(policy);
        Assert.assertNotNull(policy.getId());
        Assert.assertEquals(3, ((LeaseDefinition) policy.getDefinition()).getLeaseTotalTermMax());

        Assert.assertNotNull(client.findPolicyByName(name));

        ((LeaseDefinition) policy.getDefinition()).setLeaseGrace(4);
        policy = client.updatePolicy(policy);
        Assert.assertEquals(4, ((LeaseDefinition) policy.getDefinition()).getLeaseGrace());
    }

    @Test
    public void createOrUpdatePolicyTest()
            throws InterruptedException, IOException, URISyntaxException {
        ServiceBrokerClient client = getServiceBrokerClient();
        Policy policy = Policy.builder()
                .name("test-" + System.currentTimeMillis())
                .typeId(Type.LEASE)
                .definition(LeaseDefinition.builder()
                        .leaseGrace(1)
                        .leaseTotalTermMax(3)
                        .leaseTermMax(5)
                        .build()
                )
                .build();
        policy = client.createOrUpdatePolicy(policy);
        policies.add(policy);
        String id = policy.getId();
        Assert.assertNotNull(id);

        String name = "test-" + System.currentTimeMillis();
        policy.setName(name);

        policy = client.createOrUpdatePolicy(policy);
        Assert.assertEquals(name, policy.getName());
        Assert.assertEquals(id, policy.getId());
    }

    @Test
    public void testGetAllPolicies()
            throws IOException, InterruptedException, URISyntaxException {
        int policiesCount = 3;
        for (int i = 0; i < policiesCount; i++) {
            String name = "test-" + i + "-" + System.currentTimeMillis();
            Policy policy = getServiceBrokerClient().createPolicy(Policy.builder()
                    .name(name)
                    .definition(LeaseDefinition.builder()
                            .leaseTermMax(1)
                            .leaseTotalTermMax(1)
                            .build()
                    )
                    .enforcementType(EnforcementType.HARD)
                    .typeId(Type.LEASE)
                    .build());
            Assert.assertNotNull(policy.getId());
            policies.add(policy);
        }
        List<Policy> allPolicies = getServiceBrokerClient().getAllPolicies();
        Assert.assertTrue(allPolicies.size() >= policiesCount);
        Assert.assertNotNull(allPolicies.get(0).getDefinition());

        List<String> expectedPolicyIds = policies.stream()
                .map(policy -> policy.getId())
                .collect(Collectors.toList());
        List<Policy> expectedPolicies = allPolicies.stream()
                .filter(policy -> expectedPolicyIds.contains(policy.getId()))
                .collect(Collectors.toList());
        Assert.assertEquals(policiesCount, expectedPolicies.size());
    }

    @After
    public void clean() throws InterruptedException, IOException, URISyntaxException {
        for (Policy policy : policies) {
            getServiceBrokerClient().deletePolicy(policy.getId());
        }

        policies = new ArrayList<>();
    }
}
