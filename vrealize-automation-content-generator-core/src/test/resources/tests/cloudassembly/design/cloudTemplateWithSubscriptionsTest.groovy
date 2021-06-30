/*
 * Copyright 2021-2021 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

import com.vmware.devops.GenerationContext
import com.vmware.devops.model.cloudassembly.design.CloudTemplate
import com.vmware.devops.model.cloudassembly.design.CloudTemplate.BlueprintContent
import com.vmware.devops.model.cloudassembly.design.CloudTemplate.BlueprintContent.CloudMachine
import com.vmware.devops.model.cloudassembly.design.CloudTemplate.BlueprintContent.StringInput
import com.vmware.devops.model.cloudassembly.extensibility.Criteria
import com.vmware.devops.model.cloudassembly.extensibility.Subscription

GenerationContext context = context
context.globalConfiguration.defaultProject = "testProjectName"
context.cloudAssemblyConfiguration.defaultFlavor = "default"

return CloudTemplate.builder()
        .name("testBlueprint")
        .content(BlueprintContent.builder()
                .inputs("hello": StringInput.builder()
                        .defaultValue("world")
                        .build())
                .resources("machine": CloudMachine.builder()
                        .image("test-image")
                        .properties([
                                "test:x": StringInput.builder()
                                        .defaultValue("y")
                                        .build()
                        ])
                        .build())
                .build())
        .extensibility(
                CloudTemplate.Extensibility.builder()
                        .subscriptions([
                                Subscription.builder()
                                        .runnableName("testAction")
                                        .runnableType(com.vmware.devops.client.cloudassembly.extensibility.stubs.Subscription.RunnableType.ACTION)
                                        .eventTopic(com.vmware.devops.client.cloudassembly.extensibility.stubs.Subscription.EventTopic.POST_COMPUTE_PROVISION)
                                        .build(),
                                Subscription.builder()
                                        .name("test2")
                                        .runnableName("testAction")
                                        .runnableType(com.vmware.devops.client.cloudassembly.extensibility.stubs.Subscription.RunnableType.ACTION)
                                        .eventTopic(com.vmware.devops.client.cloudassembly.extensibility.stubs.Subscription.EventTopic.POST_COMPUTE_PROVISION)
                                        .criteria(new Criteria("dummy"))
                                        .build()
                        ])
                        .build()
        )
        .build()