/*
 * Copyright 2021-2021 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package tests.codestream

import com.vmware.devops.GenerationContext
import com.vmware.devops.model.codestream.JenkinsEndpoint
import com.vmware.devops.model.codestream.JiraEndpoint

GenerationContext context = context
context.globalConfiguration.defaultProject = "testProjectName"
context.globalConfiguration.defaultCloudProxy = "testProxyName"
context.codestreamConfiguration.defaultJiraEndpoint = "testJiraEndpoint"

return JiraEndpoint.builder()
        .name("test")
        .url("https://dummy.eng.vmware.com")
        .username("user")
        .password("pass")
        .acceptCertificate(true)
        .build()