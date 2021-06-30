/*
 * Copyright 2021-2021 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.vmware.devops.model.codestream;

import java.util.Optional;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.vmware.devops.client.codestream.stubs.Task.ConditionInput;
import com.vmware.devops.client.codestream.stubs.Task.Type;

/**
 * ConditionTask implements {@link com.vmware.devops.model.codestream.Task} and can be included in
 * CodeStream Stage. It represent CodeStream Task of type Condition.
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ConditionTask implements Task, CodestreamTask {

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
     * Boolean option which specify whether the pipeline will fail in case of task failure or will
     * continue.
     * <p>
     * By default this option is false which mean the Pipeline will fail in case the task fail
     * itself
     */
    private Boolean ignoreFailure;

    /**
     * The condition of the task to be performed.
     * <p>
     * String representation of Boolean expression which will be performed. The result of the
     * condition will decide whether the task is successful or not
     */
    private String condition;

    @Override
    public com.vmware.devops.client.codestream.stubs.Task initializeTask() {
        com.vmware.devops.client.codestream.stubs.Task result = com.vmware.devops.client.codestream.stubs.Task
                .builder()
                .type(getType())
                .preCondition(preCondition)
                .ignoreFailure(Optional.ofNullable(ignoreFailure).orElse(false))
                .input(ConditionInput.builder()
                        .condition(Optional.ofNullable(condition).orElse(""))
                        .build()
                )
                .build();

        return result;
    }

    @Override
    public void populateData(Pipeline parent, String name,
            com.vmware.devops.client.codestream.stubs.Task task) {
        ConditionInput input = (ConditionInput) task.getInput();

        this.name = name;
        this.ignoreFailure = task.isIgnoreFailure();
        this.preCondition = task.getPreCondition();
        this.condition = input.getCondition();
    }

    @Override
    public Type getType() {
        return Type.CONDITION;
    }
}
