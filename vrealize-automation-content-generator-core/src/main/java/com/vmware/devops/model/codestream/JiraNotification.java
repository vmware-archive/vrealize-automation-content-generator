/*
 * Copyright 2021-2021 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.vmware.devops.model.codestream;

import java.util.Optional;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.vmware.devops.GenerationContext;
import com.vmware.devops.client.codestream.stubs.Notification.Event;
import com.vmware.devops.client.codestream.stubs.Notification.Type;

/**
 * JiraNotification represent entity that implements {@link com.vmware.devops.model.codestream.Notification}
 * and specify Pipeline Notification of type Jira.
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class JiraNotification implements
        Notification<com.vmware.devops.client.codestream.stubs.Notification.JiraNotification> {
    /**
     * The endpoint of the notification.
     * <p>
     * This represent Jira endpoint entity.
     * See {@link com.vmware.devops.model.codestream.Endpoint},
     * {@link com.vmware.devops.model.codestream.JiraEndpoint}
     */
    private String endpoint;

    /**
     * The issue type of the Jira object.
     */
    private String issuetype;

    /**
     * The Jira project
     */
    private String project;

    /**
     * The summary of the Jira object.
     */
    private String summary;

    /**
     * The assignee of the Jira object.
     */
    private String assignee;

    /**
     * The description of the Jira object.
     */
    @Builder.Default
    private String description = "";

    /**
     * The action of the Jira notification which to be performed.
     * <p>
     * By default the action is create which mean the Notification will perform create jira object
     */
    @Builder.Default
    private String action = "create";

    /**
     * The event on which the notification to be triggered.
     * <p>
     * See {@link com.vmware.devops.client.codestream.stubs.Notification.Event
     */
    @Builder.Default
    private Event event = Event.FAILURE;

    @Override
    public Type getType() {
        return Type.JIRA;
    }

    @Override
    public com.vmware.devops.client.codestream.stubs.Notification initializeNotification() {
        return com.vmware.devops.client.codestream.stubs.Notification.JiraNotification.builder()
                .event(event)
                .endpoint(Optional.ofNullable(endpoint).orElse(
                        GenerationContext.getInstance().getCodestreamConfiguration()
                                .getDefaultJiraEndpoint()))
                .issuetype(issuetype)
                .project(project)
                .summary(summary)
                .assignee(assignee)
                .action(action)
                .description(description)
                .build();
    }

    @Override
    public void populateData(
            com.vmware.devops.client.codestream.stubs.Notification.JiraNotification notification) {
        this.action = notification.getAction();
        this.assignee = notification.getAssignee();
        this.description = notification.getDescription();
        this.endpoint = notification.getEndpoint();
        this.event = notification.getEvent();
        this.issuetype = notification.getIssuetype();
        this.project = notification.getProject();
        this.summary = notification.getSummary();
    }
}
