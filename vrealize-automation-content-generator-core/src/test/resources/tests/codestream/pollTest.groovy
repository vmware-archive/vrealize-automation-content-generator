/*
 * Copyright 2021-2021 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package tests.codestream

import com.vmware.devops.GenerationContext
import com.vmware.devops.client.codestream.stubs.Task
import com.vmware.devops.model.codestream.Output
import com.vmware.devops.model.codestream.Pipeline
import com.vmware.devops.model.codestream.PollTask
import com.vmware.devops.model.codestream.RestTask
import com.vmware.devops.model.codestream.Stage

GenerationContext context = context
context.globalConfiguration.defaultProject = "testProjectName"
context.codestreamConfiguration.defaultAgentEndpoint = "testAgent"


return Pipeline.builder()
        .name("poll-test")
        .stages([
                Stage.builder()
                        .name("stage-1")
                        .tasks([
                            PollTask.builder()
                                .name("poll-task-1")
                                .agent("agent")
                                .url("https://vmware.com")
                                .successCriteria("")
                                .failureCriteria("")
                                .build(),
                            PollTask.builder()
                                .name("poll-task-2")
                                .agent("testAgent")
                                .url("https://vmware.com")
                                .successCriteria("true == true")
                                .failureCriteria("false == false")
                                .pollCount(10000)
                                .pollIntervalSeconds(120)
                                .ignoreIntermittentPollFailure(true)
                                .outputs([
                                    new Output("localKey", "globalKey")
                                ])
                                .build()
                        ])
                        .build()
        ])
        .build()