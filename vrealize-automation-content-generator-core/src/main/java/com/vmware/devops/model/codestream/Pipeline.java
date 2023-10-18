/*
 * Copyright 2021-2021 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.vmware.devops.model.codestream;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.vmware.devops.GenerationContext;
import com.vmware.devops.ReverseGenerationContext;
import com.vmware.devops.client.codestream.stubs.Notification.Type;
import com.vmware.devops.model.GenerationEntity;
import com.vmware.devops.model.ReverseGenerationEntity;

/**
 * Pipeline is the main entity we will be using to construct CodeStream Pipelines
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@Slf4j
public class Pipeline implements GenerationEntity,
        ReverseGenerationEntity<com.vmware.devops.client.codestream.stubs.Pipeline> {
    /**
     * The name of the pipeline.
     */
    private String name;
    /**
     * The description of the pipeline.
     */
    private String description;
    /**
     * The list of all stages represent in the pipeline.
     */
    private List<Stage> stages;

    /**
     * The list of all triggers connected to this pipeline.
     * <p>
     * By default the value is empty list
     * which mean that the pipeline will be not referenced by any trigger at the time of creation.
     * If triggers list is not empty, vRealize Automation Content Generator will create triggers
     * specified in the list and referenced this pipeline to them.
     */
    @Builder.Default
    private List<Trigger> triggers = Collections.emptyList();

    /**
     * The state of the pipeline.
     * <p>
     * If true the pipeline will be enabled after generation, otherwise the pipeline will be
     * disabled after generation.
     */
    @Builder.Default
    private Boolean enabled = true;

    /**
     * The icon of the pipeline
     */
    @Builder.Default
    private String icon = null;

    /**
     * The concurrency of the pipeline.
     * <p>
     * By default the concurrency of the generated pipeline will be 20. Represent how many parallel
     * execution of the pipeline are possible at certain time.
     */
    @Builder.Default
    private Integer concurrency = 20;

    /**
     * The project of the pipeline.
     * <p>
     * Defaults to the one specified in global configuration.
     * In case the project is specified explicitly, it will be associated to the pipeline.
     */
    @Builder.Default
    private String project = GenerationContext.getInstance().getGlobalConfiguration()
            .getDefaultProject();

    /**
     * Inputs of the pipeline.
     * <p>
     * Key-value pairs of inputs of the pipeline.
     */
    @Builder.Default
    private Map<String, String> inputs = Collections.emptyMap();

    /**
     * Outputs of the pipeline.
     * <p>
     * Key-value pairs of outputs of the pipeline.
     */
    @Builder.Default
    private Map<String, String> outputs = Collections.emptyMap();

    /**
     * List of notification which to be associated to the pipeline.
     * <p>
     * See {@link com.vmware.devops.model.codestream.Notification},
     * {@link com.vmware.devops.model.codestream.EmailNotification},
     * {@link com.vmware.devops.model.codestream.JiraNotification},
     * {@link com.vmware.devops.model.codestream.WebhookNotification}
     */
    @Builder.Default
    private List<Notification> notifications = new ArrayList<>();

    public com.vmware.devops.client.codestream.stubs.Pipeline initializePipeline() {
        return com.vmware.devops.client.codestream.stubs.Pipeline.builder()
                .name(name)
                .project(project)
                .description(description)
                .icon(icon)
                .enabled(enabled)
                .concurrency(concurrency)
                .input(processInputs())
                .stages(stages.stream().filter(s -> !(s instanceof NoOpStage))
                        .collect(Collectors.toMap(Stage::getName, Stage::initializeStage)))
                .stageOrder(
                        stages.stream().filter(s -> !(s instanceof NoOpStage)).map(Stage::getName)
                                .collect(Collectors.toList()))
                .output(processOutputs())
                .notifications(
                        processNotifications()
                )
                .build();
    }

    public void expandTriggers(com.vmware.devops.client.codestream.stubs.Pipeline pipeline) {
        triggers.stream().forEach(t -> {
            if (t instanceof GerritTrigger) {
                GerritTrigger gerritTrigger = (GerritTrigger) t;
                gerritTrigger.setName(
                        Optional.ofNullable(gerritTrigger.getName()).orElse("gerrit-trigger"));
                gerritTrigger.setName(
                        String.format("%s-%s", pipeline.getName(), gerritTrigger.getName()));
                gerritTrigger.setProject(pipeline.getProject());
                gerritTrigger.getConfigurations().stream().forEach(
                        c -> {
                            c.setPipeline(pipeline.getName());
                            c.setInput(pipeline.getInput());
                        }
                );
            } else {
                throw new UnsupportedOperationException("Trigger not supported: " + t.getClass());
            }
        });
    }

    @Override
    public void generate() throws Exception {
        for (Variable v : processInPlaceTaskVariables()) {
            v.generate();
        }

        com.vmware.devops.client.codestream.stubs.Pipeline pipeline = initializePipeline();
        int retryCount = Integer.getInteger("pipeline.createOrUpdate.retryCount", 1);

        do {
            retryCount--;
            try {
                pipeline = GenerationContext.getInstance().getEndpointConfiguration().getClient()
                        .getCodestream()
                        .createOrUpdatePipeline(pipeline);
            } catch (Exception e) {
                if (retryCount > 0) {
                    log.error("Failed to create or update pipeline, will retry", e);
                } else {
                    throw e;
                }
            }
        } while (retryCount > 0);

        expandTriggers(pipeline);
        for (Trigger t : triggers) {
            t.generate();
        }
    }

    private Map<String, String> processInputs() {
        List<Input> inlineInputs = stages.stream().flatMap(s -> s.getTasks().stream())
                .flatMap(t -> {
                    if (t instanceof ParallelTask) {
                        return ((ParallelTask) t).getTasks().stream()
                                .filter(WithInputs.class::isInstance)
                                .flatMap(subT -> ((WithInputs) subT).getInputs().stream());
                    } else if (t instanceof WithInputs) {
                        WithInputs withInputs = (WithInputs) t;
                        if (withInputs.getInputs() != null) {
                            return withInputs.getInputs().stream();
                        }
                    }

                    return Stream.empty();
                })
                .flatMap(i -> {
                    if (i.getValue() instanceof Input) {
                        Input originalValue = (Input) i.getValue();
                        if (originalValue.getKey() == null) {
                            originalValue.setKey(i.getKey());
                        }
                        i.setValue(Input.globalInputReference(originalValue.getKey()));
                        return Stream.of(originalValue);
                    }

                    if (i.isGlobal()) {
                        Object originalValue = i.getValue();
                        i.setValue(Input.globalInputReference(i.getKey()));
                        // In case we come here twice for one object
                        i.setGlobal(false);
                        return Stream.of(new Input(i.getKey(), originalValue));
                    }
                    return Stream.empty();
                })
                .collect(Collectors.toList());

        Map<String, String> result = new HashMap<>(inlineInputs.stream().collect(Collectors.toMap(
                Input::getKey, i -> i.getValue().toString(), (first, second) -> second)));
        result.putAll(inputs);

        return result;
    }

    private Map<Type, List<com.vmware.devops.client.codestream.stubs.Notification>> processNotifications() {
        return Map.of(
                Type.EMAIL,
                notifications.stream().filter(n -> n.getType() == Type.EMAIL)
                        .map(Notification::initializeNotification).collect(Collectors.toList()),
                Type.JIRA,
                notifications.stream().filter(n -> n.getType() == Type.JIRA)
                        .map(Notification::initializeNotification).collect(Collectors.toList()),
                Type.WEBHOOK,
                notifications.stream().filter(n -> n.getType() == Type.WEBHOOK)
                        .map(Notification::initializeNotification).collect(Collectors.toList())
        );
    }

    public List<Variable> processInPlaceTaskVariables() {
        List<Variable> result = new ArrayList<>();
        for (Stage s : getStages()) {
            result.addAll(s.getTasks().stream()
                    .flatMap(t -> {
                        if (t instanceof ParallelTask) {
                            return ((ParallelTask) t).getTasks().stream()
                                    .filter(SshTask.class::isInstance)
                                    .flatMap(innerTask -> Stream.of(
                                            initializeSshTaskVariable(s, (SshTask) innerTask)));
                        } else if (t instanceof SshTask) {
                            return Stream.of(initializeSshTaskVariable(s, (SshTask) t));
                        }

                        return Stream.empty();
                    }).collect(Collectors.toList()));
        }

        return result;
    }

    public Variable initializeSshTaskVariable(Stage stage, SshTask sshTask) {
        sshTask.setVariablePrefix(String.format("%s-%s", name, stage.getName()));

        if (sshTask.getAuthentication().isUsernamePassword()) {
            return sshTask.initializePasswordVariable();
        } else if (sshTask.getAuthentication().getPassphrase() != null) {
            return sshTask.initializePassPhaseVariable();
        }
        return null;
    }

    private Map<String, String> processOutputs() {
        Map<String, String> result = new HashMap<>();

        for (Stage stage : stages) {
            for (Task task : stage.getTasks()) {
                if (task instanceof ParallelTask) {
                    for (CodestreamTask subTask : ((ParallelTask) task).getTasks()) {
                        if (subTask instanceof WithOutputs) {
                            WithOutputs taskWithOutputs = ((WithOutputs) subTask);
                            if (taskWithOutputs.getOutputs() != null) {
                                taskWithOutputs.getOutputs().forEach(o -> result.put(
                                        o.getGlobalKey(),
                                        Output.taskOutputReference(stage.getName(),
                                                subTask.getName(),
                                                o.getLocalKey())
                                ));
                            }
                        }
                    }
                } else if (task instanceof WithOutputs && task instanceof CodestreamTask) {
                    WithOutputs taskWithOutputs = ((WithOutputs) task);
                    if (taskWithOutputs.getOutputs() != null) {
                        taskWithOutputs.getOutputs().stream().forEach(o ->
                                result.put(
                                        o.getGlobalKey(),
                                        Output.taskOutputReference(stage.getName(),
                                                ((CodestreamTask) task).getName(),
                                                o.getLocalKey())
                                )
                        );
                    }
                }
            }
        }

        result.putAll(outputs);
        return result;
    }

    @Override
    public void populateData(com.vmware.devops.client.codestream.stubs.Pipeline pipeline)
            throws Exception {
        name = pipeline.getName();
        description = pipeline.getDescription();
        enabled = pipeline.isEnabled();
        concurrency = pipeline.getConcurrency();
        project = pipeline.getProject();

        stages = new ArrayList<>();
        for (String stageName : pipeline.getStageOrder()) {
            com.vmware.devops.client.codestream.stubs.Stage stage = pipeline.getStages()
                    .get(stageName);

            List<Task> tasks = new ArrayList<>();

            for (String task : stage.getTaskOrder()) {
                String[] subTasks = task.split(",");
                if (subTasks.length > 1) {
                    List<CodestreamTask> parallelSubtasks = new ArrayList<>();
                    for (String taskName : subTasks) {
                        parallelSubtasks.add(populateTaskData(taskName,
                                stage.getTasks().get(taskName)));
                    }
                    tasks.add(new ParallelTask(parallelSubtasks));
                } else {
                    String taskName = subTasks[0];
                    tasks.add((Task) populateTaskData(taskName, stage.getTasks().get(taskName)));
                }
            }

            stages.add(Stage.builder()
                    .name(stageName)
                    .tasks(tasks)
                    .build()
            );
        }

        inputs = pipeline.getInput();
        outputs = pipeline.getOutput();

        notifications = pipeline.getNotifications().entrySet().stream().flatMap(e ->
                e.getValue().stream()
                        .flatMap(n -> Stream.of(populateNotificationData(n))
                        )).collect(Collectors.toList());
    }

    public CodestreamTask populateTaskData(String name,
            com.vmware.devops.client.codestream.stubs.Task task) throws Exception {
        switch (task.getType()) {
        case SSH:
            SshTask sshTask = new SshTask();
            sshTask.populateData(this, name, task);
            return sshTask;
        case POLL:
            PollTask pollTask = new PollTask();
            pollTask.populateData(this, name, task);
            return pollTask;
        case REST:
            RestTask restTask = new RestTask();
            restTask.populateData(this, name, task);
            return restTask;
        case JENKINS:
            JenkinsTask jenkinsTask = new JenkinsTask();
            jenkinsTask.populateData(this, name, task);
            return jenkinsTask;
        case PIPELINE:
            PipelineTask pipelineTask = new PipelineTask();
            pipelineTask.populateData(this, name, task);
            return pipelineTask;
        case CONDITION:
            ConditionTask conditionTask = new ConditionTask();
            conditionTask.populateData(this, name, task);
            return conditionTask;
        case USER_OPERATION:
            UserOperationTask userOperationTask = new UserOperationTask();
            userOperationTask.populateData(this, name, task);
            return userOperationTask;
        default:
            throw new IllegalArgumentException("Unknown type task " + task.getType());
        }
    }

    public Notification populateNotificationData(
            com.vmware.devops.client.codestream.stubs.Notification notification) {
        if (notification instanceof com.vmware.devops.client.codestream.stubs.Notification.JiraNotification) {
            JiraNotification jiraNotification = new JiraNotification();
            jiraNotification.populateData(
                    (com.vmware.devops.client.codestream.stubs.Notification.JiraNotification) notification);
            return jiraNotification;
        }
        if (notification instanceof com.vmware.devops.client.codestream.stubs.Notification.EmailNotification) {
            EmailNotification emailNotification = new EmailNotification();
            emailNotification.populateData(
                    (com.vmware.devops.client.codestream.stubs.Notification.EmailNotification) notification);
            return emailNotification;
        }
        if (notification instanceof com.vmware.devops.client.codestream.stubs.Notification.WebhookNotificaton) {
            WebhookNotification webhookNotification = new WebhookNotification();
            webhookNotification.populateData(
                    (com.vmware.devops.client.codestream.stubs.Notification.WebhookNotificaton) notification);
            return webhookNotification;
        }

        throw new IllegalArgumentException("Unknown notification type " + notification.getClass());
    }

    @Override
    public String getTemplatePath() {
        return "templates/codestream/pipelineReverseGenerate.groovy.peb";
    }

    @Override
    public void dump(com.vmware.devops.client.codestream.stubs.Pipeline root, File output)
            throws Exception {
        ReverseGenerationEntity.super.dump(root, output);
        for (Stage stage : stages) {
            for (Task task : stage.getTasks()) {
                if (task instanceof ParallelTask) {
                    for (CodestreamTask subTask : ((ParallelTask) task).getTasks()) {
                        subTask.dump(this, subTask.getName(),
                                root.getStages().get(stage.getName()).getTasks()
                                        .get(subTask.getName()));
                    }
                } else {
                    CodestreamTask codestreamTask = ((CodestreamTask) task);
                    ((CodestreamTask) task).dump(this, codestreamTask.getName(),
                            root.getStages().get(stage.getName()).getTasks()
                                    .get(codestreamTask.getName()));
                }
            }
        }
    }

    @Override
    public void dumpAll() {
        boolean failed = false;
        for (com.vmware.devops.client.codestream.stubs.Pipeline pipeline : ReverseGenerationContext
                .getInstance().getVraExportedData().getPipelines()) {
            try {
                dump(pipeline, ReverseGenerationContext.getInstance()
                        .newOutputDirFile(pipeline.getName() + "-pipeline.groovy"));
            } catch (Exception e) {
                failed = true;
                log.error("Failed to export pipeline " + pipeline.getName(), e);
            }
        }

        if (failed) {
            throw new RuntimeException("At least one pipeline export failed.");
        }
    }
}
