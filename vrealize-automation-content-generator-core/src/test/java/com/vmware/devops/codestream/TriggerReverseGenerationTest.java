/*
 * Copyright 2021-2021 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.vmware.devops.codestream;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import com.vmware.devops.ReverseGenerationContext;
import com.vmware.devops.TestUtils;
import com.vmware.devops.Utils;
import com.vmware.devops.client.codestream.stubs.GerritTrigger;
import com.vmware.devops.client.codestream.stubs.GerritTrigger.Configuration;
import com.vmware.devops.client.codestream.stubs.GerritTrigger.Configuration.EventType;
import com.vmware.devops.client.codestream.stubs.GerritTrigger.Pattern;
import com.vmware.devops.client.codestream.stubs.GerritTrigger.Pattern.PatternType;
import com.vmware.devops.model.VraExportedData;

public class TriggerReverseGenerationTest {
    @Test
    public void test() throws IOException {
        File outputDir = TestUtils.createTempDir();
        GerritTrigger gerritTrigger = GerritTrigger.builder()
                .name("test")
                .project("test-project")
                .gerritProject("test-gerrit-project")
                .branch("master")
                .configurations(List.of(
                        Configuration.builder()
                                .pipeline("pipeline")
                                .eventType(EventType.CHANGE_MERGED)
                                .input(Map.of(
                                        "k1", "v1",
                                        "k2", "v2"
                                ))
                                .failureComment("Pipeline failed")
                                .successComment("Pipeline success").build()
                ))
                .inclusions(List.of(
                        Pattern.builder()
                                .type(PatternType.PLAIN)
                                .value("inclusion-value")
                                .build(),
                        Pattern.builder()
                                .type(PatternType.REGEX)
                                .value("value*")
                                .build()
                ))
                .exclusions(List.of(Pattern.builder()
                        .type(PatternType.PLAIN)
                        .value("exclusion-value")
                        .build()
                ))
                .prioritizeExclusion(false)
                .enabled(false)
                .listener("listener").build();

        VraExportedData vraExportedData = new VraExportedData();
        vraExportedData.setGerritTriggers(List.of(
                gerritTrigger
        ));
        ReverseGenerationContext.getInstance().setVraExportedData(vraExportedData);
        ReverseGenerationContext.getInstance().setOutputDir(outputDir.getAbsolutePath());

        new com.vmware.devops.model.codestream.GerritTrigger().dumpAll();

        String output = Utils
                .readFile(new File(outputDir, "600-test-gerrit-trigger.groovy").getAbsolutePath());
        String expectedOutput = Utils
                .readFile(
                        "tests/codestream/gerritTriggerReverseGenerateTestOutput.test.groovy");
        Assert.assertEquals(expectedOutput, output);
    }
}
