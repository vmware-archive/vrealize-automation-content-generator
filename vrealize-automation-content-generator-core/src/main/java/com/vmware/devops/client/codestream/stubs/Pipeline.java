/*
 * Copyright 2021-2021 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.vmware.devops.client.codestream.stubs;

import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Pipeline extends Entity {
    public static final String KIND = "PIPELINE";

    private String name;
    private String description;
    private String project;
    private String icon;
    private int concurrency;
    private boolean enabled;
    private List<String> stageOrder;
    private Map<String, Stage> stages;
    private Map<String, String> input;
    private Map<String, String> output;
    private Map<Notification.Type, List<Notification>> notifications;

    @Override
    public String getKind() {
        return KIND;
    }

}
