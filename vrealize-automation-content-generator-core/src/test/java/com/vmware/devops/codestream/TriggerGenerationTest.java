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
import com.vmware.devops.model.codestream.GerritTrigger;

public class TriggerGenerationTest extends GenerationTestBase {

    @Test
    public void triggerTest() throws IOException {
        SpecProcessor specProcessor = new SpecProcessor();
        GerritTrigger t = (GerritTrigger) specProcessor
                .process(Utils.readFile("tests/codestream/triggerTest.groovy"));

        String output = SerializationUtils.toPrettyJson(t.initializeTrigger());
        String expectedOutput = Utils.readFile(
                "tests/codestream/triggerTestOutput.json");
        Assert.assertEquals(expectedOutput, output);
    }
}
