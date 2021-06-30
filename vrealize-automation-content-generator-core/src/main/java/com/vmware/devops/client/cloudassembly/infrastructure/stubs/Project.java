/*
 * Copyright 2021-2021 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.vmware.devops.client.cloudassembly.infrastructure.stubs;

import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Project {
    public static final String PLACEMENT_POLICY_PROPERTY_KEY = "__projectPlacementPolicy";

    private String id;
    private String name;
    private Map<String, String> properties;
    private List<ProjectPrincipal> viewers;
    private List<ProjectPrincipal> members;
    private List<ProjectPrincipal> administrators;

    public enum PlacementPolicy {
        DEFAULT,
        SPREAD
    }
}
