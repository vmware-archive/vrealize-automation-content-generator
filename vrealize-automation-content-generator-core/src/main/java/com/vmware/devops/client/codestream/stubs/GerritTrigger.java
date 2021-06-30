/*
 * Copyright 2021-2021 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.vmware.devops.client.codestream.stubs;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GerritTrigger extends Entity {
    public static final String KIND = "GERRIT_TRIGGER";

    private String name;
    private String project;
    private String gerritProject;
    private String branch;
    private String listener;
    private boolean enabled;
    private List<Configuration> configurations;
    private List<Pattern> inclusions;
    private List<Pattern> exclusions;
    private boolean prioritizeExclusion;

    @Builder
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Configuration {
        private String pipeline;
        private EventType eventType;
        private Map<String, String> input;
        private String failureComment;
        private String successComment;

        @AllArgsConstructor
        public enum EventType {
            CHANGE_MERGED("change-merged");

            private String value;

            @JsonValue
            public String getValue() {
                return value;
            }
        }
    }

    @Builder
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Pattern {
        private PatternType type;
        private String value;

        @AllArgsConstructor
        public enum PatternType {
            PLAIN, REGEX;
        }
    }

    @Override
    public String getKind() {
        return KIND;
    }
}