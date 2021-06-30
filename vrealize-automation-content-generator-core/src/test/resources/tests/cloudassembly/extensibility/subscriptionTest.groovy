/*
 * Copyright 2021-2021 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

import com.vmware.devops.GenerationContext
import com.vmware.devops.client.cloudassembly.extensibility.stubs.Subscription.EventTopic
import com.vmware.devops.client.cloudassembly.extensibility.stubs.Subscription.RunnableType
import com.vmware.devops.model.cloudassembly.extensibility.Subscription

GenerationContext context = context
context.extensibilityConfiguration.defaultSubscriptionTimeout = 13
context.extensibilityConfiguration.defaultSubscriptionPriority = 7

return Subscription.builder()
        .name("test")
        .runnableName("testAction")
        .runnableType(RunnableType.ACTION)
        .eventTopic(EventTopic.POST_COMPUTE_PROVISION)
        .build()