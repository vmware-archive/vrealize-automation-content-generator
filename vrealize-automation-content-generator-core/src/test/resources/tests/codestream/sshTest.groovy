/*
 * Copyright 2021-2021 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package tests.codestream

import com.vmware.devops.GenerationContext
import com.vmware.devops.model.codestream.Output
import com.vmware.devops.model.codestream.ParallelTask
import com.vmware.devops.model.codestream.Pipeline
import com.vmware.devops.model.codestream.SshTask
import com.vmware.devops.model.codestream.Stage

GenerationContext context = context
context.globalConfiguration.defaultProject = "testProjectName"
context.codestreamConfiguration.defaultAgentEndpoint = "testAgent"

String sshHost = "1.2.3.4"

return Pipeline.builder()
        .name("test")
        .stages([
                Stage.builder()
                        .name("stage-1")
                        .tasks([
                                SshTask.builder()
                                        .name("task-1")
                                        .host(sshHost)
                                        .authentication(new SshTask.SshTaskAuthentication("username", "password"))
                                        .scriptPath("tests/codestream/sshTestScript.sh")
                                        .agent("agent")
                                        .arguments([
                                                "\${WORLD}"
                                        ])
                                        .environmentVariables([
                                                "WORLD": "world"
                                        ])
                                        .outputs([
                                                new Output("localKey", "globalKey")
                                        ])
                                        .build(),
                                SshTask.builder()
                                        .name("task-2")
                                        .host(sshHost)
                                        .authentication(new SshTask.SshTaskAuthentication("username", "pkey", "passphrase"))
                                        .scriptPath("tests/codestream/sshTestScript.sh")
                                        .workingDirectory("test")
                                        .build(),
                                new ParallelTask([
                                        SshTask.builder()
                                                .name("task-3")
                                                .host(sshHost)
                                                .authentication(new SshTask.SshTaskAuthentication("username", "pkey", "passphrase1"))
                                                .scriptPath("tests/codestream/sshTestScript.sh")
                                                .workingDirectory("test")
                                                .build(),
                                        SshTask.builder()
                                                .name("task-4")
                                                .host(sshHost)
                                                .authentication(new SshTask.SshTaskAuthentication("username", "pkey", "passphrase2"))
                                                .scriptPath("tests/codestream/sshTestScript.sh")
                                                .workingDirectory("test")
                                                .build(),
                                ])
                        ])
                        .build()
        ])
        .build()