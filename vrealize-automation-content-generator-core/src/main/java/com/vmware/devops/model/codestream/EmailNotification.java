/*
 * Copyright 2021-2021 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.vmware.devops.model.codestream;

import java.util.List;
import java.util.Optional;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.vmware.devops.GenerationContext;
import com.vmware.devops.client.codestream.stubs.Notification.Event;
import com.vmware.devops.client.codestream.stubs.Notification.Type;

/**
 * EmailNotification represent entity that implements {@link com.vmware.devops.model.codestream.Notification}
 * and specify Pipeline Notification of type Email.
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmailNotification implements
        Notification<com.vmware.devops.client.codestream.stubs.Notification.EmailNotification> {

    /**
     * The endpoint of the notification.
     * <p>
     * This represent Email endpoint entity.
     * See {@link com.vmware.devops.model.codestream.Endpoint},
     * {@link com.vmware.devops.model.codestream.EmailEndpoint}
     */
    private String endpoint;

    /**
     * List of emails to send the notification to.
     */
    private List<String> emailTo;

    /**
     * The subject of the email.
     */
    private String subject;

    /**
     * The body of the email.
     *
     * By default is empty string.
     */
    @Builder.Default
    private String body = "";

    /**
     * The event on which the notification to be triggered.
     * <p>
     * See {@link com.vmware.devops.client.codestream.stubs.Notification.Event
     */
    @Builder.Default
    private Event event = Event.SUCCESS;

    @Override
    public Type getType() {
        return Type.EMAIL;
    }

    @Override
    public com.vmware.devops.client.codestream.stubs.Notification initializeNotification() {
        return com.vmware.devops.client.codestream.stubs.Notification.EmailNotification.builder()
                .event(event)
                .endpoint(Optional.ofNullable(endpoint).orElse(
                        GenerationContext.getInstance().getCodestreamConfiguration()
                                .getDefaultEmailEndpoint()
                ))
                .to(emailTo)
                .subject(subject)
                .body(body)
                .build();
    }

    @Override
    public void populateData(
            com.vmware.devops.client.codestream.stubs.Notification.EmailNotification notification) {
        this.endpoint = notification.getEndpoint();
        this.body = notification.getBody();
        this.emailTo = notification.getTo();
        this.event = notification.getEvent();
        this.subject = notification.getSubject();
    }
}
