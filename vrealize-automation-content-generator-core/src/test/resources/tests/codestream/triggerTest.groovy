/*
 * Copyright 2021-2021 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package tests.codestream

import com.vmware.devops.GenerationContext
import com.vmware.devops.model.codestream.GerritTrigger
import com.vmware.devops.model.codestream.GerritTrigger.Configuration
import com.vmware.devops.model.codestream.GerritTrigger.Pattern
import com.vmware.devops.client.codestream.stubs.GerritTrigger.Pattern.PatternType

GenerationContext context = context
context.globalConfiguration.defaultProject = "testProjectName"
context.codestreamConfiguration.defaultGerritListener = "my-gerrit-listener"

return GerritTrigger.builder()
        .name("test")
        .gerritProject("test-project")
        .branch("master")
        .configurations([
                Configuration.builder()
                        .pipeline("test-pipeline")
                        .input([
                                "localInput": "defaultValue"
                        ])
                        .failureComment("Pipeline Failed")
                        .successComment("Pipeline Success")
                        .build()
        ])
        .inclusions([
                Pattern.builder()
                        .value("cicd/.*")
                        .type(PatternType.REGEX)
                        .build()
        ])
        .exclusions([
                Pattern.builder()
                        .value("pipelines")
                        .type(PatternType.PLAIN)
                        .build()
        ])
        .prioritizeExclusion(true)
        .build()
