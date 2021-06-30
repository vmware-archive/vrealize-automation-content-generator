/*
 * Copyright 2021-2021 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package tests.codestream

import com.vmware.devops.GenerationContext
import com.vmware.devops.model.codestream.AgentEndpoint
import com.vmware.devops.model.codestream.EmailEndpoint
import com.vmware.devops.model.codestream.JenkinsEndpoint

GenerationContext context = context
context.globalConfiguration.defaultProject = "testProjectName"
context.globalConfiguration.defaultCloudProxy = "testProxyName"

return EmailEndpoint.builder()
        .name("test")
        .senderAddress("test-address")
        .outboundHost("test-host")
        .outboundUsername("test-user")
        .outboundPassword("password")
        .build()