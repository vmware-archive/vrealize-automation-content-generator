/*
 * Copyright 2021-2021 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.vmware.devops.client.cloudassembly.extensibility.stubs;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Subscription {
    private String id;
    private Type type;
    private String name;
    private String description;
    private boolean disabled;
    private EventTopic eventTopicId;
    private boolean blocking;
    private String criteria;
    private RunnableType runnableType;
    private String runnableId;
    private int timeout;
    private int priority;
    private RunnableType recoverRunnableType;
    private String recoverRunnableId;
    private Constraints constraints;

    public enum Type {
        RUNNABLE
    }

    @AllArgsConstructor
    public enum RunnableType {
        ACTION("extensibility.abx");

        private String value;

        @JsonValue
        public String getValue() {
            return value;
        }
    }

    @AllArgsConstructor
    public enum EventTopic {
        POST_COMPUTE_PROVISION("compute.provision.post"),
        POST_DEPLOYMENT_REQUEST("deployment.request.post"),
        POST_DEPLOYMENT_RESOURCE_REQUEST("deployment.resource.action.post"),
        PRE_DEPLOYMENT_REQUEST("deployment.request.pre");

        private String value;

        @JsonValue
        public String getValue() {
            return value;
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Constraints {
        private List<String> projectId;
    }

    @AllArgsConstructor
    public enum ResourceAction {
        CHANGE_LEASE("ChangeLease");

        private String value;

        public String getValue() {
            return value;
        }
    }

    public enum EventType {
        CREATE_DEPLOYMENT
    }
}