/*
 * Copyright 2021-2021 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.vmware.devops.codestream;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.vmware.devops.ReverseGenerationContext;
import com.vmware.devops.TestUtils;
import com.vmware.devops.Utils;
import com.vmware.devops.client.codestream.stubs.Variable;
import com.vmware.devops.client.codestream.stubs.Variable.VariableType;
import com.vmware.devops.model.VraExportedData;

public class VariableReverseGenerationTest {

    @Test
    public void test() throws IOException {
        File outputDir = TestUtils.createTempDir();
        Variable variable = Variable.builder()
                .name("test")
                .description("description")
                .type(VariableType.REGULAR)
                .value("value")
                .build();
        VraExportedData vraExportedData = new VraExportedData();
        vraExportedData.setVariables(List.of(
                variable
        ));

        ReverseGenerationContext.getInstance().setVraExportedData(vraExportedData);
        ReverseGenerationContext.getInstance().setOutputDir(outputDir.getAbsolutePath());

        new com.vmware.devops.model.codestream.Variable().dumpAll();

        String output = Utils
                .readFile(new File(outputDir, "010-test-variable.groovy").getAbsolutePath());
        String expectedOutput = Utils
                .readFile(
                        "tests/codestream/variableReverseGenerateTestOutput.test.groovy");
        Assert.assertEquals(expectedOutput, output);
    }
}
