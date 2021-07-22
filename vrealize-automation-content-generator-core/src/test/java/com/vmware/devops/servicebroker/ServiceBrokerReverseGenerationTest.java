/*
 * Copyright 2021-2021 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.vmware.devops.servicebroker;

import java.io.File;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.vmware.devops.ReverseGenerationContext;
import com.vmware.devops.TestUtils;
import com.vmware.devops.Utils;
import com.vmware.devops.client.cloudassembly.infrastructure.stubs.Project;
import com.vmware.devops.client.servicebroker.stubs.Policy;
import com.vmware.devops.client.servicebroker.stubs.Policy.EnforcementType;
import com.vmware.devops.client.servicebroker.stubs.Policy.LeaseDefinition;
import com.vmware.devops.client.servicebroker.stubs.Policy.Type;
import com.vmware.devops.model.VraExportedData;
import com.vmware.devops.model.servicebroker.LeasePolicy;

public class ServiceBrokerReverseGenerationTest {

    @Test
    public void orgScopeLeasePolicyTest() throws Exception {
        File outputDir = TestUtils.createTempDir();

        VraExportedData data = new VraExportedData();
        data.setPolicies(List.of(
                Policy.builder()
                        .name("test")
                        .enforcementType(EnforcementType.HARD)
                        .typeId(Type.LEASE)
                        .definition(
                                LeaseDefinition.builder()
                                        .leaseTermMax(1)
                                        .leaseTotalTermMax(2)
                                        .leaseGrace(3)
                                        .build()
                        )
                        .build(),
                Policy.builder()
                        .name("test-2")
                        .typeId(Type.QUOTA)
                        .build()
        ));

        ReverseGenerationContext.getInstance().setVraExportedData(data);
        ReverseGenerationContext.getInstance().setOutputDir(outputDir.getAbsolutePath());

        new LeasePolicy().dumpAll();
        String output = Utils
                .readFile(new File(outputDir, "test-lease-policy.groovy").getAbsolutePath());
        String expectedOutput = Utils
                .readFile(
                        "tests/servicebroker/orgScopeLeasePolicyReverseGenerateTestOutput.test.groovy");
        Assert.assertEquals(expectedOutput, output);
    }

    @Test
    public void projectScopeLeasePolicyTest() throws Exception {
        File outputDir = TestUtils.createTempDir();

        VraExportedData data = new VraExportedData();
        data.setPolicies(List.of(
                Policy.builder()
                        .name("test")
                        .enforcementType(EnforcementType.SOFT)
                        .typeId(Type.LEASE)
                        .projectId("project-id")
                        .definition(
                                LeaseDefinition.builder()
                                        .leaseTermMax(1)
                                        .leaseTotalTermMax(2)
                                        .leaseGrace(3)
                                        .build()
                        )
                        .build()
        ));

        data.setProjects(List.of(
                Project.builder()
                        .id("project-id")
                        .name("project")
                        .build()
        ));

        ReverseGenerationContext.getInstance().setVraExportedData(data);
        ReverseGenerationContext.getInstance().setOutputDir(outputDir.getAbsolutePath());

        new LeasePolicy().dumpAll();
        String output = Utils
                .readFile(new File(outputDir, "test-lease-policy.groovy").getAbsolutePath());
        String expectedOutput = Utils
                .readFile(
                        "tests/servicebroker/projectScopeLeasePolicyReverseGenerateTestOutput.test.groovy");
        Assert.assertEquals(expectedOutput, output);
    }
}
