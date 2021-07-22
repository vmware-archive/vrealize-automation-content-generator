/*
 * Copyright 2021-2021 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.vmware.devops.servicebroker;

import java.io.IOException;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.vmware.devops.GenerationTestBase;
import com.vmware.devops.IdCache;
import com.vmware.devops.SerializationUtils;
import com.vmware.devops.SpecProcessor;
import com.vmware.devops.Utils;
import com.vmware.devops.model.servicebroker.LeasePolicy;

public class ServiceBrokerGenerationTest extends GenerationTestBase {

    @BeforeClass
    public static void setup() {
        // So we don't need client
        IdCache.PROJECT_ID_CACHE.getNameToId().put("testProjectName", "test-id");
    }

    @Test
    public void orgScopeLeasePolicyTest() throws IOException {
        SpecProcessor specProcessor = new SpecProcessor();
        LeasePolicy p = (LeasePolicy) specProcessor
                .process(Utils.readFile("tests/servicebroker/orgScopeLeasePolicyTest.groovy"));

        String output = SerializationUtils
                .prettifyJson(SerializationUtils.toPrettyJson(p.initializePolicy()));
        String expectedOutput = Utils
                .readFile("tests/servicebroker/orgScopeLeasePolicyTestOutput.json");
        Assert.assertEquals(expectedOutput, output);
    }

    @Test
    public void projectScopeLeasePolicyTest() throws IOException {
        SpecProcessor specProcessor = new SpecProcessor();
        LeasePolicy p = (LeasePolicy) specProcessor
                .process(Utils.readFile("tests/servicebroker/projectScopeLeasePolicyTest.groovy"));

        String output = SerializationUtils
                .prettifyJson(SerializationUtils.toPrettyJson(p.initializePolicy()));
        String expectedOutput = Utils
                .readFile("tests/servicebroker/projectScopeLeasePolicyTestOutput.json");
        Assert.assertEquals(expectedOutput, output);
    }
}
