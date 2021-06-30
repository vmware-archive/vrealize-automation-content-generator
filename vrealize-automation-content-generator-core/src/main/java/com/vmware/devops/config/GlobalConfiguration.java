/*
 * Copyright 2021-2021 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.vmware.devops.config;

import java.io.IOException;
import java.net.URISyntaxException;

import lombok.Data;
import lombok.NoArgsConstructor;

import com.vmware.devops.IdCache;

@Data
@NoArgsConstructor
public class GlobalConfiguration {

    private String defaultProject;
    private String defaultCloudProxy;

    public String getProjectId() throws InterruptedException, IOException, URISyntaxException {
        return IdCache.PROJECT_ID_CACHE.getId(defaultProject);
    }
}
