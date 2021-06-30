/*
 * Copyright 2021-2021 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package tests.codestream

import com.vmware.devops.GenerationContext
import com.vmware.devops.model.codestream.JenkinsEndpoint

GenerationContext context = context
context.globalConfiguration.defaultProject = "testProjectName"
context.globalConfiguration.defaultCloudProxy = "testProxyName"

return JenkinsEndpoint.builder()
        .name("test")
        .url("https://dummy.eng.vmware.com")
        .username("hello")
        .password("world")
        .build()