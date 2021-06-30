/*
 * Copyright 2021-2021 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

import com.vmware.devops.GenerationContext
import com.vmware.devops.client.cloudassembly.extensibility.stubs.Action.Runtime
import com.vmware.devops.client.cloudassembly.extensibility.stubs.Subscription.EventTopic
import com.vmware.devops.model.cloudassembly.extensibility.Action
import com.vmware.devops.model.cloudassembly.extensibility.Subscription

GenerationContext context = context
context.globalConfiguration.defaultProject = "testProjectName"

return Action.builder()
        .name("test-action")
        .runtime(Runtime.PYTHON)
        .entrypoint("handle")
        .shared(true)
        .contentPath("tests/cloudassembly/extensibility/basicActionScript.py")
        .subscriptions([
                Subscription.builder()
                        .eventTopic(EventTopic.POST_COMPUTE_PROVISION)
                        .build(),
                Subscription.builder()
                        .name("test2")
                        .eventTopic(EventTopic.POST_COMPUTE_PROVISION)
                        .build()
        ])
        .build()