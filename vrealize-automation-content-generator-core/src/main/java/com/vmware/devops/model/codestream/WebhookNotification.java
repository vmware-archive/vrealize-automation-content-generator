/*
 * Copyright 2021-2021 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.vmware.devops.model.codestream;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.vmware.devops.GenerationContext;
import com.vmware.devops.IdCache;
import com.vmware.devops.ReverseGenerationContext;
import com.vmware.devops.client.codestream.CodestreamClient;
import com.vmware.devops.client.codestream.stubs.Notification.Event;
import com.vmware.devops.client.codestream.stubs.Notification.Type;
import com.vmware.devops.client.codestream.stubs.Notification.WebhookNotificaton;
import com.vmware.devops.client.codestream.stubs.Notification.WebhookNotificaton.WebhookAction;

/**
 * WebhookNotification represent entity that implements {@link com.vmware.devops.model.codestream.Notification}
 * and specify Pipeline Notification of type Webhook.
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class WebhookNotification implements
        Notification<com.vmware.devops.client.codestream.stubs.Notification.WebhookNotificaton> {
    /**
     * The url of the webhook
     */
    private String url;


    /**
     * Id of the cloud proxy which to be used for the webhook
     */
    @Builder.Default
    private String cloudProxy = GenerationContext.getInstance().getGlobalConfiguration()
            .getDefaultCloudProxy();

    /**
     * Headers of the webhook.
     * <p>
     * By default not headers are attached to the request.
     */
    @Builder.Default
    private Map<String, String> headers = new HashMap<>();

    /**
     * The payload of the webhook.
     */
    @Builder.Default
    private String payload = "";

    /**
     * The event on which the notification to be triggered.
     * <p>
     * See {@link com.vmware.devops.client.codestream.stubs.Notification.Event
     */
    @Builder.Default
    private Event event = Event.FAILURE;

    /**
     * Webhook Action of the task
     * <p>
     * By default action is POST.
     * See {@link com.vmware.devops.client.codestream.stubs.Notification.WebhookNotificaton.WebhookAction}
     */
    @Builder.Default
    private WebhookAction action = WebhookAction.POST;

    @Override
    public Type getType() {
        return Type.WEBHOOK;
    }

    @Override
    public com.vmware.devops.client.codestream.stubs.Notification initializeNotification() {
        String cloudProxyId = null;
        if (cloudProxy != null) {
            try {
                cloudProxyId = IdCache.CODESTREAM_CLOUD_PROXY_ID_CACHE.getId(cloudProxy);
            } catch (InterruptedException | IOException | URISyntaxException e) {
                throw new RuntimeException(e);
            }
        }
        return com.vmware.devops.client.codestream.stubs.Notification.WebhookNotificaton.builder()
                .event(event)
                .action(action)
                .url(url)
                .headers(headers)
                .payload(payload)
                .cloudProxyId(cloudProxyId)
                .build();
    }

    @Override
    public void populateData(WebhookNotificaton notification) {
        this.action = notification.getAction();
        if (notification.getCloudProxyId() != null) {
            this.cloudProxy = CodestreamClient
                    .getProxyName(ReverseGenerationContext.getInstance().getVraExportedData()
                            .getCloudProxies().stream()
                            .filter(c -> c.getId().equals(notification.getCloudProxyId())).findFirst()
                            .get());
        }
        this.event = notification.getEvent();
        this.headers = notification.getHeaders();
        this.payload = notification.getPayload();
        this.url = notification.getUrl();
    }
}
