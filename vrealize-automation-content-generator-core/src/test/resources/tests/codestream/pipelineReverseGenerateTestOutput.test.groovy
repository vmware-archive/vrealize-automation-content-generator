import com.vmware.devops.client.codestream.stubs.Notification.Event
import com.vmware.devops.model.codestream.*

return Pipeline.builder()
        .enabled(false)
        .project("test-project")
        .name("test")
        .description("description")
        .inputs([
                "k1": "v1",
                "k2": "v2",
        ])
        .outputs([
                "k1": "v1",
                "k2": "v2",
        ])
        .concurrency(3)
        .stages([
                Stage.builder()
                        .name("Stage1")
                        .tasks([
                                SshTask.builder()
                                        .name("ssh1")
                                        .preCondition("pre-condition")
                                        .ignoreFailure(false)
                                        .authentication(
                                                new SshTask.SshTaskAuthentication("username", "password")
                                        )
                                        .environmentVariables([
                                                "k1": "v1",
                                                "k2": "v2",
                                        ])
                                        .arguments([
                                                "arg1",
                                                "arg2",
                                        ])
                                        .workingDirectory("dir")
                                        .scriptPath("test-ssh1-task-script.sh")
                                        .host("host")
                                        .build(),
                                SshTask.builder()
                                        .name("ssh2")
                                        .preCondition("pre-condition")
                                        .ignoreFailure(false)
                                        .authentication(
                                                new SshTask.SshTaskAuthentication(
                                                        "username",
                                                        """privateKey""",
                                                        "passphrase"
                                                )
                                        )
                                        .environmentVariables([
                                                "k1": "v1",
                                                "k2": "v2",
                                        ])
                                        .arguments([
                                                "arg1",
                                                "arg2",
                                        ])
                                        .workingDirectory("dir")
                                        .scriptPath("test-ssh2-task-script.sh")
                                        .host("host")
                                        .build(),
                        ])
                        .build(),
                Stage.builder()
                        .name("Stage2")
                        .tasks([
                                JenkinsTask.builder()
                                        .name("jenkins")
                                        .preCondition("pre-condition")
                                        .ignoreFailure(false)
                                        .job("job")
                                        .jobFolder("job-folder")
                                        .inputs([
                                                new Input("k1", "v1"),
                                                new Input("k2", "v2"),
                                        ])
                                        .endpoint("jenkins")
                                        .build(),
                                PollTask.builder()
                                        .name("poll")
                                        .preCondition("pre-condition")
                                        .ignoreFailure(false)
                                        .url("url")
                                        .headers([
                                                "k1": "v1",
                                                "k2": "v2",
                                        ])
                                        .agent("agent")
                                        .successCriteria("success")
                                        .failureCriteria("failure")
                                        .pollCount(3)
                                        .pollIntervalSeconds(5)
                                        .ignoreIntermittentPollFailure(false)
                                        .build(),
                                RestTask.builder()
                                        .name("rest")
                                        .preCondition("pre-condition")
                                        .ignoreFailure(true)
                                        .url("url")
                                        .payload("payload")
                                        .headers([
                                                "k1": "v1",
                                                "k2": "v2",
                                        ])
                                        .action(com.vmware.devops.client.codestream.stubs.Task.RestInput.RestActions.POST)
                                        .agent("agent")
                                        .build(),
                                new ParallelTask([
                                UserOperationTask.builder()
                                        .name("user-operation")
                                        .preCondition("pre-condition")
                                        .ignoreFailure(false)
                                        .summary("summary")
                                        .description("description")
                                        .approvers([
                                                "approver1",
                                                "approver2",
                                        ])
                                        .expirationInDays(3)
                                        .sendEmail(true)
                                        .endpoint("email")
                                        .build(),
                                ConditionTask.builder()
                                        .name("condition")
                                        .preCondition("pre-condition")
                                        .ignoreFailure(false)
                                        .condition("condition")
                                        .build(),
                                ]),
                                new PipelineTask().builder()
                                        .name("pipeline")
                                        .preCondition("pre-condition")
                                        .ignoreFailure(true)
                                        .inputs([
                                                new Input("k1", "v1"),
                                                new Input("k2", "v2"),
                                        ])
                                        .build(),
                        ])
                        .build(),
        ])
        .notifications([
                JiraNotification.builder()
                        .summary("summary")
                        .action("action")
                        .assignee("assignee")
                        .description("description")
                        .event(Event.SUCCESS)
                        .issuetype("issue-type")
                        .project("project")
                        .build(),
                EmailNotification.builder()
                        .subject("subject")
                        .body("body")
                        .emailTo([
                                "to1",
                                "to2",
                        ])
                        .event(Event.FAILURE)
                        .endpoint("endpoint")
                        .build(),
                WebhookNotification.builder()
                        .url("url")
                        .action(com.vmware.devops.client.codestream.stubs.Notification.WebhookNotificaton.WebhookAction.POST)
                        .payload("payload")
                        .cloudProxy("cloudProxyName")
                        .headers([
                                "k1": "v1",
                                "k2": "v2",
                        ])
                        .event(Event.STARTED)
                        .build(),
        ])
        .build()