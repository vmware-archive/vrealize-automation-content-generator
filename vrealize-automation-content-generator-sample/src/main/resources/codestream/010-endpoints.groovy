/*
 * Copyright 2021-2021 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

import com.vmware.devops.GenerationContext
import com.vmware.devops.Utils
import com.vmware.devops.model.codestream.JenkinsEndpoint

GenerationContext context = context
context.getCodestreamConfiguration().defaultJenkinsEndpoint = "sample-jenkins"

Properties properties = Utils.readProperties("configuration.properties")

return JenkinsEndpoint.builder()
        .name(context.getCodestreamConfiguration().defaultJenkinsEndpoint)
        .url((String) properties.get("jenkins.url"))
        .username((String) properties.get("jenkins.username"))
        .password((String) properties.get("jenkins.password"))
        .build()