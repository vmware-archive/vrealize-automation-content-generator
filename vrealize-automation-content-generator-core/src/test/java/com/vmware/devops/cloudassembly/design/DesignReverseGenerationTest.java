/*
 * Copyright 2021-2021 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.vmware.devops.cloudassembly.design;

import java.io.File;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.vmware.devops.ReverseGenerationContext;
import com.vmware.devops.TestUtils;
import com.vmware.devops.Utils;
import com.vmware.devops.client.cloudassembly.design.stubs.Blueprint;
import com.vmware.devops.client.cloudassembly.infrastructure.stubs.Project;
import com.vmware.devops.model.VraExportedData;
import com.vmware.devops.model.cloudassembly.design.CloudTemplate;

public class DesignReverseGenerationTest {

    @Test
    public void cloudTemplateTest() throws Exception {
        File outputDir = TestUtils.createTempDir();
        String content = Utils.readFile("tests/cloudassembly/design/basicBlueprintContent.yaml");


        VraExportedData data = new VraExportedData();
        data.setBlueprints(List.of(
                Blueprint.builder()
                        .name("test-1")
                        .projectId("project-id")
                        .requestScopeOrg(true)
                        .content(content)
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

        new CloudTemplate().dumpAll();
        String output = Utils
                .readFile(new File(outputDir, "test-1-cloud-template.groovy").getAbsolutePath());
        String expectedOutput = Utils
                .readFile(
                        "tests/cloudassembly/design/cloudTemplateReverseGenerateTestOutput.test.groovy");
        Assert.assertEquals(expectedOutput, output);

        File script = new File(outputDir, "test-1-cloud-template-content.yaml");
        Assert.assertEquals(content, Utils.readFile(script.getAbsolutePath()));
    }
}
