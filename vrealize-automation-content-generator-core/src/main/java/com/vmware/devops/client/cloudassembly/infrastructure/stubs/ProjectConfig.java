/*
 * Copyright 2021-2021 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.vmware.devops.client.cloudassembly.infrastructure.stubs;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectConfig {
    private ProjectState projectState;
    private List<CloudZone> cloudZones;


    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CloudZone {
        private String name;
        private String placementZoneLink;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProjectState {
        private String documentSelfLink;
    }
}
