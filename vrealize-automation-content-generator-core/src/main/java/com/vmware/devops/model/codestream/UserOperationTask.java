/*
 * Copyright 2021-2021 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.vmware.devops.model.codestream;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.vmware.devops.GenerationContext;
import com.vmware.devops.client.codestream.stubs.Task.EndpointKeys;
import com.vmware.devops.client.codestream.stubs.Task.Type;
import com.vmware.devops.client.codestream.stubs.Task.UserOperationInput;

/**
 * UserOperationTask implements {@link com.vmware.devops.model.codestream.Task} and can be included
 * in CodeStream Stage. It represent CodeStream Task of type UserOperation.
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserOperationTask implements Task, CodestreamTask {

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
     * The endpoint of the task.
     * <p>
     * This represent Agent endpoint entity.
     * See {@link com.vmware.devops.model.codestream.Endpoint},
     * {@link com.vmware.devops.model.codestream.JenkinsEndpoint}
     */
    private String endpoint;

    /**
     * List of approvers of the task.
     * <p>
     * List of users which will be able to approve or reject the UserOperation.
     */
    private List<String> approvers;

    /**
     * The summary of the task.
     */
    private String summary;

    /**
     * The description of the task.
     */
    private String description;

    /**
     * Boolean property which specify whether or not to send email for the user operation.
     */
    private Boolean sendEmail;

    /**
     * The number of days in which the user operation will expire.
     */
    private Integer expirationInDays;
    /**
     * The time after which this entity would expire based on 'expirationUnit'
     */
    private Integer expiration;
    /**
     * Time unit for the value specified in 'expiration'
     */
    private String expirationUnit;

    @Override
    public com.vmware.devops.client.codestream.stubs.Task initializeTask() {
        int expInDays = Optional.ofNullable(expirationInDays).orElse(3);
        if (null == expirationInDays && null != expiration && null != expirationUnit) {
            expInDays = 0;
        }
        com.vmware.devops.client.codestream.stubs.Task result = com.vmware.devops.client.codestream.stubs.Task
                .builder()
                .type(getType())
                .preCondition(preCondition)
                .ignoreFailure(Optional.ofNullable(ignoreFailure).orElse(false))
                .input(UserOperationInput.builder()
                        .approvers(approvers)
                        .summary(summary)
                        .description(description)
                        .sendemail(Optional.ofNullable(sendEmail).orElse(false))
                        .expirationInDays(expInDays)
                        .expiration(Optional.ofNullable(expiration).orElse(0))
                        .expirationUnit(expirationUnit)
                        .build())
                .build();

        if (Boolean.TRUE.equals(sendEmail)) {
            result.setEndpoints(
                    Map.of(EndpointKeys.EMAIL_SERVER, Optional.ofNullable(endpoint).orElse(
                            GenerationContext.getInstance().getCodestreamConfiguration()
                                    .getDefaultEmailEndpoint())));
        }

        return result;
    }

    @Override
    public void populateData(Pipeline parent, String name,
            com.vmware.devops.client.codestream.stubs.Task task) {
        UserOperationInput input = (UserOperationInput) task.getInput();

        this.name = name;
        this.endpoint = task.getEndpoints().get(EndpointKeys.EMAIL_SERVER);
        this.ignoreFailure = task.isIgnoreFailure();
        this.preCondition = task.getPreCondition();
        this.approvers = input.getApprovers();
        this.description = input.getDescription();
        if (input.getExpirationInDays() != 0) {
            this.expirationInDays = input.getExpirationInDays();
        }
        if (input.getExpiration() != 0) {
            this.expiration = input.getExpiration();
        }
        this.expirationUnit = input.getExpirationUnit();
        this.sendEmail = input.isSendemail();
        this.summary = input.getSummary();
    }

    @Override
    public Type getType() {
        return Type.USER_OPERATION;
    }
}
