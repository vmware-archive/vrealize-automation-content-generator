/*
 * Copyright 2021-2021 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.vmware.devops.client.codestream.stubs;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CloudProxy {
    public static final String PROXY_NAME_KEY = "proxyName";
    public static final String HOST_NAME_KEY = "hostName";

    private String id;
    private Map<String, String> customProperties;
}
