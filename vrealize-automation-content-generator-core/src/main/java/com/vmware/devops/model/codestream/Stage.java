/*
 * Copyright 2021-2021 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.vmware.devops.model.codestream;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Stage is the main entity we will be using to construct CodeStream Stages that can be included in
 * CodeStream Pipeline
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Stage {
    /**
     * The name of the stage.
     */
    private String name;

    /**
     * List of task to be included in the stage.
     * <p>
     * By default the list is empty which mean no task will be associated to the stage
     */
    @Builder.Default
    private List<Task> tasks = Collections.emptyList();

    public com.vmware.devops.client.codestream.stubs.Stage initializeStage() {
        return com.vmware.devops.client.codestream.stubs.Stage.builder()
                .taskOrder(tasks.stream().flatMap(t -> {
                    if (t instanceof ParallelTask) {
                        String result = ((ParallelTask) t).getTasks().stream()
                                .filter(t1 -> !(t1 instanceof NoOpTask))
                                .map(CodestreamTask::getName).collect(Collectors.joining(","));
                        return Stream.of(result);
                    }
                    if (t instanceof NoOpTask) {
                        return Stream.empty();
                    }

                    return Stream.of(((CodestreamTask) t).getName());
                }).collect(Collectors.toList()))
                .tasks(tasks.stream().flatMap(t -> {
                    if (t instanceof ParallelTask) {
                        return ((ParallelTask) t).getTasks().stream()
                                .filter(t1 -> !(t1 instanceof NoOpTask));
                    }
                    if (t instanceof NoOpTask) {
                        return Stream.empty();
                    }
                    return Stream.of(((CodestreamTask) t));
                }).collect(
                        Collectors.toMap(CodestreamTask::getName, CodestreamTask::initializeTask)))
                .build();
    }
}
