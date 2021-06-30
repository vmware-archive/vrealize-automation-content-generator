/*
 * Copyright 2021-2021 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.vmware.devops.client.cloudassembly.infrastructure.stubs;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ImageName {
    private String name;
    private Map<String, ImageMapping> imageMapping;
    private Map<String, ImageMapping> oldImageMapping;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ImageMapping {
        private String image;
    }
}