/*
 * Copyright 2021-2021 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.vmware.devops.model.codestream;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.vmware.devops.GenerationContext;
import com.vmware.devops.client.codestream.stubs.Task.EndpointKeys;
import com.vmware.devops.client.codestream.stubs.Task.PollInput;
import com.vmware.devops.client.codestream.stubs.Task.Type;

/**
 * PollTask implements {@link com.vmware.devops.model.codestream.Task} and can be included in
 * CodeStream Stage. It represent CodeStream Task of type Poll.
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PollTask implements Task, CodestreamTask, WithOutputs {
    public static final String EXIT_CRITERIA_FAILURE_KEY = "failure";
    public static final String EXIT_CRITERIA_SUCCESS_KEY = "success";
    private static final Map<String, String> DEFAULT_HEADERS = Collections.unmodifiableMap(Map.of(
            "Accept", "application/json",
            "Content-Type", "application/json"
    ));

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
     * The url of the task.
     * <p>
     * This property will be used for the poll mechanism.
     */
    private String url;

    /**
     * The failure criteria of the task.
     * <p>
     * Reopresent what is the criteria on which the task will fail and exit.
     */
    private String failureCriteria;

    /**
     * The success criteria of the task.
     * <p>
     * Reopresent what is the criteria on which the task will success and exit.
     */
    private String successCriteria;

    /**
     * The agent of the task.
     * <p>
     * This agent will be used to perform the poll requests. By default, the agent specified in
     * global CodeStream configuration will be used.
     */
    @Builder.Default
    private String agent = GenerationContext.getInstance().getCodestreamConfiguration()
            .getDefaultAgentEndpoint();

    /**
     * List of the outputs of the task.
     */
    @Builder.Default
    private List<Output> outputs = Collections.emptyList();

    /**
     * Map of the headers of the task.
     * <p>
     * By default following headers will be used:
     * "Accept": "application/json",
     * "Content-Type: "application/json"
     */
    @Builder.Default
    private Map<String, String> headers = DEFAULT_HEADERS;

    /**
     * Boolean option which specify whether the pipeline will fail in case of task failure or will
     * continue.
     * <p>
     * By default this option is false which mean the Pipeline will fail in case the task fail
     * itself
     */
    @Builder.Default
    private Boolean ignoreFailure = false;

    /**
     * Boolean option which specify whether the pipeline will fail in case of intermittent poll
     * failure or will continue.
     * <p>
     * By default this option is false which mean the Pipeline will fail in case the poll fail
     * intermittently
     */
    @Builder.Default
    private Boolean ignoreIntermittentPollFailure = false;

    /**
     * The poll count of the task.
     * <p>
     * This property specify how many times the poll will be performed
     */
    @Builder.Default
    private int pollCount = 0;

    /**
     * The poll interval of the task.
     * <p>
     * This property specify what interval to be used between each poll of the task. By default 60
     * seconds.
     */
    @Builder.Default
    private int pollIntervalSeconds = 60;

    @Override
    public com.vmware.devops.client.codestream.stubs.Task initializeTask() {
        Map<String, String> exitCriteria = new HashMap<>();
        exitCriteria.put(EXIT_CRITERIA_FAILURE_KEY, failureCriteria);
        exitCriteria.put(EXIT_CRITERIA_SUCCESS_KEY, successCriteria);

        return com.vmware.devops.client.codestream.stubs.Task.builder()
                .type(getType())
                .preCondition(preCondition)
                .ignoreFailure(ignoreFailure)
                .input(PollInput.builder()
                        .url(url)
                        .headers(headers)
                        .pollCount(pollCount)
                        .pollIntervalSeconds(pollIntervalSeconds)
                        .ignoreFailure(ignoreIntermittentPollFailure)
                        .exitCriteria(exitCriteria)
                        .build()
                )
                .endpoints(Map.of(EndpointKeys.AGENT, agent))
                .build();
    }

    @Override
    public void populateData(Pipeline parent, String name,
            com.vmware.devops.client.codestream.stubs.Task task) {
        PollInput input = (PollInput) task.getInput();

        this.name = name;
        this.agent = task.getEndpoints().get(EndpointKeys.AGENT);
        this.ignoreFailure = task.isIgnoreFailure();
        this.preCondition = task.getPreCondition();
        this.url = input.getUrl();
        this.headers = input.getHeaders();
        this.ignoreIntermittentPollFailure = input.isIgnoreFailure();
        this.pollCount = input.getPollCount();
        this.pollIntervalSeconds = input.getPollIntervalSeconds();
        this.successCriteria =
                input.getExitCriteria().get(PollTask.EXIT_CRITERIA_SUCCESS_KEY);
        this.failureCriteria =
                input.getExitCriteria().get(PollTask.EXIT_CRITERIA_FAILURE_KEY);
    }

    @Override
    public Type getType() {
        return Type.POLL;
    }
}
