/*
 * Copyright 2021-2021 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.vmware.devops.model.codestream;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.vmware.devops.GenerationContext;
import com.vmware.devops.SerializationUtils;
import com.vmware.devops.client.codestream.stubs.Task.EndpointKeys;
import com.vmware.devops.client.codestream.stubs.Task.RestInput;
import com.vmware.devops.client.codestream.stubs.Task.RestInput.RestActions;
import com.vmware.devops.client.codestream.stubs.Task.Type;

/**
 * RestTask implements {@link com.vmware.devops.model.codestream.Task} and can be included in
 * CodeStream Stage. It represent CodeStream Task of type Rest.
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RestTask implements Task, CodestreamTask, WithOutputs {
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
     * This property will be used for the Rest mechanism.
     */
    private String url;

    /**
     * The payload of the task.
     * <p>
     * This property will be used for the request payload.
     */
    private Object payload;

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
     * Rest action of the task
     * <p>
     * By default action is GET.
     * See {@link com.vmware.devops.client.codestream.stubs.Task.RestInput.RestActions}
     */
    @Builder.Default
    private RestActions action = RestActions.GET;

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

    @Override
    public com.vmware.devops.client.codestream.stubs.Task initializeTask() {
        String payloadAsString;
        if (payload instanceof String) {
            payloadAsString = (String) payload;
        } else {
            try {
                payloadAsString = SerializationUtils.toJson(payload);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }

        return com.vmware.devops.client.codestream.stubs.Task.builder()
                .type(getType())
                .preCondition(preCondition)
                .ignoreFailure(ignoreFailure)
                .input(RestInput.builder()
                        .action(action)
                        .url(url)
                        .headers(headers)
                        .payload(payloadAsString)
                        .build())
                .endpoints(Map.of(EndpointKeys.AGENT, agent))
                .build();
    }

    @Override
    public void populateData(Pipeline parent, String name,
            com.vmware.devops.client.codestream.stubs.Task task) {
        RestInput input = (RestInput) task.getInput();
        this.name = name;
        this.agent = task.getEndpoints().get(EndpointKeys.AGENT);
        this.ignoreFailure = task.isIgnoreFailure();
        this.preCondition = task.getPreCondition();
        this.url = input.getUrl();
        this.action = input.getAction();
        this.headers = input.getHeaders();
        this.payload = input.getPayload();
    }

    @Override
    public Type getType() {
        return Type.REST;
    }
}
