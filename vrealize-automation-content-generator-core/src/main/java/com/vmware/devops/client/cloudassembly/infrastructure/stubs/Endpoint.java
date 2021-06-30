/*
 * Copyright 2021-2021 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.vmware.devops.client.cloudassembly.infrastructure.stubs;

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
public class Endpoint {
    private Map<String, Object> endpointProperties;
    private Map<String, String> customProperties;
    private EndpointType endpointType;
    private String name;
    private String documentSelfLink;

    @AllArgsConstructor
    public enum EndpointType {
        NIMBUS,
        VSPHERE;

        @JsonValue
        public String getValue() {
            return name().toLowerCase();
        }
    }
}
