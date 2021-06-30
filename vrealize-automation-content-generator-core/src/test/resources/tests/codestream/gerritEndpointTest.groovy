/*
 * Copyright 2021-2021 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package tests.codestream

import com.vmware.devops.GenerationContext
import com.vmware.devops.model.codestream.GerritEndpoint

GenerationContext context = context
context.globalConfiguration.defaultProject = "testProjectName"
context.globalConfiguration.defaultCloudProxy = "testProxyName"

return GerritEndpoint.builder()
        .name("test")
        .url("https://dummy.eng.vmware.com")
        .username("hello")
        .password("world")
        .privateKey("key")
        .passPhrase("passphrase")
        .build()