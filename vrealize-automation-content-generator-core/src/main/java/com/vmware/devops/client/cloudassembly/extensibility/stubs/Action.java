/*
 * Copyright 2021-2021 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.vmware.devops.client.cloudassembly.extensibility.stubs;

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
public class Action {
    private String name;
    private Runtime runtime;
    private String entrypoint;
    private Type actionType;
    private boolean shared;
    private String projectId;
    private String id;
    private String selfLink;
    private ScriptSource scriptSource;
    private String source;
    private String compressedContent;
    private String contentResourceName;
    private Map<String, Object> inputs;
    private String dependencies;
    private int timeoutSeconds;
    private int memoryInMB;

    public enum Type {
        SCRIPT,
        FLOW
    }

    public enum Runtime {
        PYTHON;

        @JsonValue
        public String getValue() {
            return name().toLowerCase();
        }
    }

    @AllArgsConstructor
    public enum ScriptSource {
        SCRIPT(0),
        PACKAGE(1);

        private int value;

        @JsonValue
        public int getValue() {
            return value;
        }
    }
}