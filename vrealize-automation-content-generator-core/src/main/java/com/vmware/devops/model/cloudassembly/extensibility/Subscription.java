/*
 * Copyright 2021-2021 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.vmware.devops.model.cloudassembly.extensibility;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Optional;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.vmware.devops.GenerationContext;
import com.vmware.devops.IdCache;
import com.vmware.devops.ReverseGenerationContext;
import com.vmware.devops.client.cloudassembly.extensibility.stubs.Subscription.EventTopic;
import com.vmware.devops.client.cloudassembly.extensibility.stubs.Subscription.RunnableType;
import com.vmware.devops.client.cloudassembly.extensibility.stubs.Subscription.Type;
import com.vmware.devops.config.ExtensibilityConfiguration;
import com.vmware.devops.model.GenerationEntity;
import com.vmware.devops.model.ReverseGenerationEntity;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@Slf4j
public class Subscription
        implements GenerationEntity,
        ReverseGenerationEntity<com.vmware.devops.client.cloudassembly.extensibility.stubs.Subscription> {
    /**
     * Is subscription disabled. Defaults to false.
     */
    private Boolean disabled;

    /**
     * Runnable type.
     */
    private RunnableType runnableType;

    /**
     * Runnable name.
     */
    private String runnableName;

    /**
     * Recover runnable type. Recover runnable is optional.
     */
    private RunnableType recoverRunnableType;

    /**
     * Recover runnable name. Recover runnable is optional.
     */
    private String recoverRunnableName;

    /**
     * Subscription name
     */
    private String name;

    /**
     * Is the subscription blocking. Defaults to false
     */
    private Boolean blocking;

    /**
     * Event topic of the subscription
     */
    private EventTopic eventTopic;

    /**
     * Subscription timeout in minutes.
     * Default to {@link ExtensibilityConfiguration#getDefaultSubscriptionTimeout()} of {@link
     * GenerationContext} singleton.
     */
    private Integer timeout;

    /**
     * Subscription priority
     * Default to {@link ExtensibilityConfiguration#getDefaultSubscriptionPriority()} of {@link
     * GenerationContext} singleton.
     */
    private Integer priority;

    @Builder.Default
    private Criteria criteria = Criteria.EMPTY_CRITERIA;

    public com.vmware.devops.client.cloudassembly.extensibility.stubs.Subscription initializeSubscription()
            throws InterruptedException, IOException, URISyntaxException {
        String recoverRunnableId = null;
        if (recoverRunnableName != null) {
            recoverRunnableId = getRunnableId(recoverRunnableName, recoverRunnableType);
        }

        return com.vmware.devops.client.cloudassembly.extensibility.stubs.Subscription
                .builder()
                .type(Type.RUNNABLE)
                .disabled(Optional.ofNullable(disabled).orElse(false))
                .name(name)
                .blocking(Optional.ofNullable(blocking).orElse(false))
                .eventTopicId(eventTopic)
                .timeout(Optional.ofNullable(timeout)
                        .orElse(GenerationContext.getInstance().getExtensibilityConfiguration()
                                .getDefaultSubscriptionTimeout()))
                .priority(Optional.ofNullable(priority)
                        .orElse(GenerationContext.getInstance().getExtensibilityConfiguration()
                                .getDefaultSubscriptionPriority()))
                .criteria(criteria.toString())
                .runnableId(getRunnableId(runnableName, runnableType))
                .runnableType(runnableType)
                .recoverRunnableId(recoverRunnableId)
                .recoverRunnableType(recoverRunnableType)
                .build();
    }

    public String getRunnableId(String name, RunnableType type)
            throws InterruptedException, IOException, URISyntaxException {
        switch (type) {
        case ACTION:
            return IdCache.ACTION_ID_CACHE.getId(name);
        default:
            throw new UnsupportedOperationException(
                    String.format("Runnable of type %s not implemented", type));
        }
    }

    @Override
    public void generate() throws Exception {
        com.vmware.devops.client.cloudassembly.extensibility.stubs.Subscription subscription = initializeSubscription();
        GenerationContext.getInstance().getEndpointConfiguration().getClient().getCloudAssembly()
                .getExtensibility().createOrUpdateSubscription(subscription);
    }

    @Override
    public void populateData(
            com.vmware.devops.client.cloudassembly.extensibility.stubs.Subscription subscription) {
        if (subscription.getRunnableType().equals(RunnableType.ACTION)) {
            runnableName = ReverseGenerationContext.getInstance().getVraExportedData().getActions()
                    .stream().filter(a -> a.getId().equals(subscription.getRunnableId()))
                    .findFirst().get().getName();
        }

        if (subscription.getRecoverRunnableId() != null) {
            if (subscription.getRecoverRunnableType().equals(RunnableType.ACTION)) {
                recoverRunnableName = ReverseGenerationContext.getInstance().getVraExportedData()
                        .getActions()
                        .stream().filter(a -> a.getId().equals(subscription.getRecoverRunnableId()))
                        .findFirst().get().getName();
            }
        }

        disabled = subscription.isDisabled();
        runnableType = subscription.getRunnableType();
        recoverRunnableType = subscription.getRecoverRunnableType();
        name = subscription.getName();
        blocking = subscription.isBlocking();
        eventTopic = subscription.getEventTopicId();
        timeout = subscription.getTimeout();
        priority = subscription.getPriority();
        criteria = new Criteria(subscription.getCriteria());
    }

    @Override
    public String getTemplatePath() {
        return "templates/cloudassembly/extensibility/subscriptionReverseGenerate.groovy.template";
    }

    @Override
    public void dumpAll() {
        boolean failed = false;
        for (com.vmware.devops.client.cloudassembly.extensibility.stubs.Subscription s : ReverseGenerationContext
                .getInstance().getVraExportedData().getSubscriptions()) {
            try {
                dump(s, ReverseGenerationContext.getInstance()
                        .newOutputDirFile("600-" + s.getName() + "-subscription.groovy"));
            } catch (Exception e) {
                failed = true;
                log.error("Failed to export subscription " + s.getName(), e);
            }
        }

        if (failed) {
            throw new RuntimeException("At least one subscription export failed.");
        }
    }
}
