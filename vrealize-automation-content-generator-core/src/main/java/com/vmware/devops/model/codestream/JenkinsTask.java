/*
 * Copyright 2021-2021 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.vmware.devops.model.codestream;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.vmware.devops.GenerationContext;
import com.vmware.devops.client.codestream.stubs.Task.EndpointKeys;
import com.vmware.devops.client.codestream.stubs.Task.JenkinsInput;
import com.vmware.devops.client.codestream.stubs.Task.Type;

/**
 * JenkinsTask implements {@link com.vmware.devops.model.codestream.Task} and can be included in
 * CodeStream Stage. It represent CodeStream Task of type Jenkins.
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class JenkinsTask implements Task, CodestreamTask, WithInputs, WithOutputs {
    /**
     * The name of the task.
     */
    private String name;

    /**
     * The precondition of the task.
     * <p>
     * String representation of Boolean expression which will be used to decide whether or not the
     * task to be performed.
     */
    private String preCondition;

    /**
     * The Jenkins job name.
     */
    private String job;

    /**
     * The Jenkins job folder name.
     */
    private String jobFolder;

    /**
     * List of the inputs of the task.
     */
    @Builder.Default
    private List<Input> inputs = Collections.emptyList();

    /**
     * List of the outputs of the task.
     */
    @Builder.Default
    private List<Output> outputs = Collections.emptyList();

    /**
     * The endpoint of the task.
     * <p>
     * This represent Jenkins endpoint entity.
     * See {@link com.vmware.devops.model.codestream.Endpoint},
     * {@link com.vmware.devops.model.codestream.JenkinsEndpoint}
     */
    @Builder.Default
    private String endpoint = GenerationContext.getInstance().getCodestreamConfiguration()
            .getDefaultJenkinsEndpoint();

    /**
     * Boolean option which specify whether the pipeline will fail in case of task failure or will
     * continue.
     * <p>
     * By default this option is false which mean the Pipeline will fail in case the task fail
     * itself
     */
    @Builder.Default
    private Boolean ignoreFailure = false;

    @Override
    public com.vmware.devops.client.codestream.stubs.Task initializeTask() {
        return com.vmware.devops.client.codestream.stubs.Task.builder()
                .type(getType())
                .preCondition(preCondition)
                .ignoreFailure(ignoreFailure)
                .endpoints(Map.of(EndpointKeys.JENKINS_SERVER, endpoint))
                .input(JenkinsInput.builder()
                        .job(job)
                        .jobFolder(jobFolder)
                        .parameters(inputs.stream()
                                .collect(Collectors
                                        .toMap(Input::getKey,
                                                i -> Optional.ofNullable(i.getValue()).orElse("")
                                                        .toString())))
                        .build())
                .build();
    }

    @Override
    public void populateData(Pipeline parent, String name,
            com.vmware.devops.client.codestream.stubs.Task task) {
        JenkinsInput input = (JenkinsInput) task.getInput();

        this.name = name;
        this.endpoint = task.getEndpoints().get(EndpointKeys.JENKINS_SERVER);
        this.ignoreFailure = task.isIgnoreFailure();
        this.preCondition = task.getPreCondition();
        this.job = input.getJob();
        this.jobFolder = input.getJobFolder();
        this.inputs = input.getParameters().entrySet().stream()
                .map(e -> new Input(e.getKey(), e.getValue())).collect(
                        Collectors.toList());
    }

    @Override
    public Type getType() {
        return Type.JENKINS;
    }

    /**
     * In some cases it is possible to not specify name of the task. In this situation the name of
     * the task will be automatically generated from the job property of jobFolder property in case
     * of multibranch Jenkins jobs
     * @return the name of the task
     */
    public String getName() {
        if (name != null) {
            return name;
        }

        if (jobFolder != null) {
            String result = jobFolder.replace("/", "-") + "-" + job;
            if (result.startsWith("-")) {
                result = result.substring(1);
            }
            return result;
        }

        return job;
    }
}
