/*
 * Copyright 2021-2021 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.vmware.devops.client.codestream.stubs;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.vmware.devops.SerializationUtils;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Task {
    private Type type;
    private String preCondition;
    private boolean ignoreFailure;

    @JsonDeserialize(using = InputDeserializer.class)
    private Input input;

    private Map<EndpointKeys, String> endpoints;

    @AllArgsConstructor
    public enum Type {
        JENKINS("Jenkins"),
        REST("REST"),
        SSH("SSH"),
        PIPELINE("Pipeline"),
        USER_OPERATION("UserOperation"),
        CONDITION("Condition"),
        POLL("POLL");

        private String value;

        @JsonValue
        public String getValue() {
            return value;
        }
    }

    @AllArgsConstructor
    public enum EndpointKeys {
        JENKINS_SERVER("jenkinsServer"),
        AGENT("agent"),
        EMAIL_SERVER("emailServer");

        private String value;

        @JsonValue
        public String getValue() {
            return value;
        }
    }

    public interface Input {
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class JenkinsInput implements Input {
        private String job;
        private String jobFolder;
        private Map<String, String> parameters;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PipelineInput implements Input {
        private String pipeline;
        private Map<String, String> inputProperties;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RestInput implements Input {
        private RestActions action;
        private String url;
        private Map<String, String> headers;
        private String payload;

        public enum RestActions {
            GET,
            POST,
            PUT,
            DELETE,
            PATCH;

            @JsonValue
            public String getValue() {
                return name().toLowerCase();
            }
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PollInput implements Input {
        private String url;
        private Map<String, String> headers;
        private int pollCount;
        private Map<String, String> exitCriteria;
        private boolean ignoreFailure;
        private int pollIntervalSeconds;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SshInput implements Input {
        private String script;
        private String host;
        private String username;
        private String workingDirectory;
        private String password;
        private String privatekey;
        private String passphrase;
        private List<String> arguments;
        private Map<String, String> environmentVariables;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserOperationInput implements Input {
        private List<String> approvers;
        private String summary;
        private String description;
        private boolean sendemail;
        private int expirationInDays;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ConditionInput implements Input {
        private String condition;
    }

    private static class InputDeserializer extends JsonDeserializer<Input> {
        @Override
        public Input deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            Type type = ((Task) p.getParsingContext().getParent().getCurrentValue()).getType();
            switch (type) {
            case JENKINS:
                return SerializationUtils.getJsonMapper().readValue(p, JenkinsInput.class);
            case REST:
                return SerializationUtils.getJsonMapper().readValue(p, RestInput.class);
            case SSH:
                return SerializationUtils.getJsonMapper().readValue(p, SshInput.class);
            case PIPELINE:
                return SerializationUtils.getJsonMapper().readValue(p, PipelineInput.class);
            case USER_OPERATION:
                return SerializationUtils.getJsonMapper().readValue(p, UserOperationInput.class);
            case CONDITION:
                return SerializationUtils.getJsonMapper().readValue(p, ConditionInput.class);
            case POLL:
                return SerializationUtils.getJsonMapper().readValue(p, PollInput.class);
            default:
                throw new IllegalStateException("Unknown input type " + type);

            }
        }
    }
}
