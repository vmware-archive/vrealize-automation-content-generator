/*
 * Copyright 2021-2021 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.vmware.devops.config;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CodestreamConfiguration {
    private String defaultEmailEndpoint;
    private String defaultGerritListener;
    private String defaultJenkinsEndpoint;
    private String defaultBranch;
    private String defaultAgentEndpoint;
    private String defaultJiraEndpoint;
}
