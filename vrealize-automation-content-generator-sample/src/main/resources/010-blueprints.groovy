/*
 * Copyright 2021-2021 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

import com.vmware.devops.GenerationContext
import com.vmware.devops.Utils
import com.vmware.devops.client.cloudassembly.extensibility.stubs.Subscription.EventTopic
import com.vmware.devops.client.cloudassembly.extensibility.stubs.Subscription.EventType
import com.vmware.devops.model.cloudassembly.design.CloudTemplate
import com.vmware.devops.model.cloudassembly.extensibility.Action
import com.vmware.devops.model.cloudassembly.extensibility.Criteria
import com.vmware.devops.model.cloudassembly.extensibility.Subscription
import com.vmware.devops.model.cloudassembly.infrastructure.ImageMapping

GenerationContext context = context

Properties properties = Utils.readProperties("configuration.properties")

String imageName = "sample-image"

ImageMapping mapping = ImageMapping.builder()
        .name(imageName)
        .imageMapping([
                (properties.get("vc.datacenter").toString()): (String) properties.get("template.url")
        ])
        .build()

CloudTemplate cloudTemplate = CloudTemplate.builder()
        .name("sample-blueprint")
        .content(CloudTemplate.BlueprintContent.builder()
                .resources("vrealize-automation-content-generator-sample-resource": CloudTemplate.BlueprintContent.CloudMachine.builder()
                        .image(imageName)
                        .build())
                .build())
        .extensibility(CloudTemplate.Extensibility.builder()
                .content([
                        CloudTemplate.Extensibility.SubscriptionWithContent.builder()
                                .runnable(Action.builder()
                                        .name("send-message")
                                        .contentPath("sample-action.py")
                                        .entrypoint("send_message")
                                        .inputs([
                                                "slack_token": properties.get("slackToken"),
                                                "instance"   : context.getEndpointConfiguration().getInstance(),
                                                "token"      : context.getEndpointConfiguration().getAuthenticationDetails().getRefreshToken()
                                        ])
                                        .dependencies([
                                                Action.Dependency.builder()
                                                        .name("requests")
                                                        .version("2.22.0")
                                                        .build()
                                        ])
                                        .build())
                                .subscription(Subscription.builder()
                                        .eventTopic(EventTopic.PRE_DEPLOYMENT_REQUEST)
                                        .criteria(new Criteria.EventTypeCriteria(EventType.CREATE_DEPLOYMENT))
                                        .build())
                                .build()
                ])
                .build())
        .build()

return [mapping, cloudTemplate]