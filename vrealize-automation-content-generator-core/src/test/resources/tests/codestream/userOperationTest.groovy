/*
 * Copyright 2021-2021 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package tests.codestream

import com.vmware.devops.GenerationContext
import com.vmware.devops.model.codestream.Pipeline
import com.vmware.devops.model.codestream.Stage
import com.vmware.devops.model.codestream.UserOperationTask

GenerationContext context = context
context.globalConfiguration.defaultProject = "testProjectName"
context.codestreamConfiguration.defaultJenkinsEndpoint = "my-jenkins-endpoint"
context.codestreamConfiguration.defaultEmailEndpoint = "my-email-endpoint"

return Pipeline.builder()
        .name("test")
        .stages([
                Stage.builder()
                        .name("stage-1")
                        .tasks([
                                UserOperationTask.builder()
                                        .name("my-approval")
                                        .sendEmail(true)
                                        .approvers([
                                                "koko",
                                                "koko-1"
                                        ])
                                        .summary("my-summary")
                                        .description("my-description")
                                        .build()
                        ])
                        .build()
        ])
        .build()
