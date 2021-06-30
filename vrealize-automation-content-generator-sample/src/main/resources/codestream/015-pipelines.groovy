/*
 * Copyright 2021-2021 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

import com.vmware.devops.model.codestream.Input
import com.vmware.devops.model.codestream.JenkinsTask
import com.vmware.devops.model.codestream.Pipeline
import com.vmware.devops.model.codestream.Stage

return Pipeline.builder()
        .name("sample-pipeline")
        .stages([
                Stage.builder()
                        .name("Stage-1")
                        .tasks([
                                JenkinsTask.builder()
                                        .name("Task-1")
                                        .job("experimental-resumable-a")
                                        .inputs([
                                                new Input("changeA", "Hello")
                                        ])
                                        .build()
                        ])
                        .build()
        ])
        .build()