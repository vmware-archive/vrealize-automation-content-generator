/*
 * Copyright 2021-2021 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.vmware.devops.model.codestream;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.vmware.devops.client.codestream.stubs.Task.PipelineInput;
import com.vmware.devops.client.codestream.stubs.Task.Type;

/**
 * PipelineTask implements {@link com.vmware.devops.model.codestream.Task} and can be included in
 * CodeStream Stage. It represent CodeStream Task of type Pipeline.
 */

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PipelineTask implements Task, CodestreamTask, WithInputs, WithOutputs {
    /**
     * The name of the task
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
     * The CodeStream Pipeline of the task.
     * <p>
     * This property represent other CodeStream pipeline which to be execute in the task
     */
    private String pipeline;

    /**
     * List of the inputs of the task.
     */
    @Builder.Default
    private List<Input> inputs = Collections.emptyList();

    /**
     * List of the output of the task.
     */
    @Builder.Default
    private List<Output> outputs = Collections.emptyList();

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
                .input(PipelineInput.builder()
                        .pipeline(Optional.ofNullable(pipeline).orElse(name))
                        .inputProperties(inputs.stream()
                                .collect(Collectors
                                        .toMap(Input::getKey, i -> i.getValue().toString())))
                        .build())
                .build();
    }

    @Override
    public void populateData(Pipeline parent, String name,
            com.vmware.devops.client.codestream.stubs.Task task) {
        PipelineInput input = (PipelineInput) task.getInput();

        this.name = name;
        this.ignoreFailure = task.isIgnoreFailure();
        this.preCondition = task.getPreCondition();
        this.inputs = input.getInputProperties().entrySet().stream()
                .map(e -> new Input(e.getKey(), e.getValue())).collect(
                        Collectors.toList());
        this.pipeline = input.getPipeline();
    }

    @Override
    public Type getType() {
        return Type.PIPELINE;
    }
}
