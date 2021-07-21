/*
 * Copyright 2021-2021 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.vmware.devops.catalog;

import java.io.IOException;
import java.net.URISyntaxException;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import com.vmware.devops.ClientTestBase;
import com.vmware.devops.client.catalog.CatalogClient;
import com.vmware.devops.client.catalog.stubs.Policy;
import com.vmware.devops.client.catalog.stubs.Policy.LeaseDefinition;
import com.vmware.devops.client.catalog.stubs.Policy.Type;

public class CatalogClientIT extends ClientTestBase {

    private Policy policy;

    @Test
    public void createUpdatePolicyTest()
            throws InterruptedException, IOException, URISyntaxException {
        CatalogClient client = getCatalogClient();
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
        CatalogClient client = getCatalogClient();
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
        String id = policy.getId();
        Assert.assertNotNull(id);

        String name = "test-" + System.currentTimeMillis();
        policy.setName(name);

        policy = client.createOrUpdatePolicy(policy);
        Assert.assertEquals(name, policy.getName());
        Assert.assertEquals(id, policy.getId());
    }

    @After
    public void clean() throws InterruptedException, IOException, URISyntaxException {
        if (policy != null) {
            getCatalogClient().deletePolicy(policy.getId());
            policy = null;
        }
    }
}
