/*
 * Copyright 2021-2021 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.vmware.devops.codestream;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import com.vmware.devops.GenerationTestBase;
import com.vmware.devops.SerializationUtils;
import com.vmware.devops.SpecProcessor;
import com.vmware.devops.Utils;
import com.vmware.devops.model.codestream.Variable;

public class VariableGenerationTest extends GenerationTestBase {

    @Test
    public void variableTest() throws IOException {
        SpecProcessor specProcessor = new SpecProcessor();
        Variable v = (Variable) specProcessor
                .process(Utils.readFile("tests/codestream/variableTest.groovy"));

        String output = SerializationUtils.toPrettyJson(v.initializeVariable());
        String expectedOutput = Utils.readFile(
                "tests/codestream/variableTestOutput.json");
        Assert.assertEquals(expectedOutput, output);
    }
}
