/*
 * Copyright 2021-2021 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.vmware.devops.client.cloudassembly.infrastructure.stubs;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectPrincipal {
    private String email;
    private Type type;

    public enum Type {
        USER,
        GROUP;

        @JsonValue
        public String getValue() {
            return name().toLowerCase();
        }
    }

    public enum Role {
        ADMINISTRATOR,
        MEMBER,
        VIEWER;

        @JsonValue
        public String getValue() {
            return name().toLowerCase();
        }
    }
}
