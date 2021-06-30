/*
 * Copyright 2021-2021 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.vmware.devops.client.codestream.stubs;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Variable extends Entity {
    public static final String KIND = "VARIABLE";

    private String project;
    private String id;
    private String name;
    private String description;
    private VariableType type;
    private String value;

    @AllArgsConstructor
    public enum VariableType {
        REGULAR("REGULAR"),
        SECRET("SECRET"),
        RESTRICTED("RESTRICTED");

        private String value;

        @JsonValue
        public String getValue() {
            return value;
        }
    }

    @Override
    public String getKind() {
        return KIND;
    }
}
