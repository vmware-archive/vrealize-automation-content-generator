/*
 * Copyright 2021-2021 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.vmware.devops.client.codestream.stubs;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class Notification {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EmailNotification extends Notification {
        private Event event;
        private String endpoint;
        private List<String> to;
        private String subject;
        private String body;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class JiraNotification extends Notification {
        private Event event;
        private String endpoint;
        private String issuetype;
        private String project;
        private String summary;
        private String description;
        private String assignee;
        private String action;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WebhookNotificaton extends Notification {
        private Event event;
        private WebhookAction action;
        private String url;
        private Map<String, String> headers;
        private String payload;
        private String cloudProxyId;

        public enum WebhookAction {
            POST,
            PUT,
            PATCH;

            @JsonValue
            public String getValue() {
                return name().toLowerCase();
            }
        }
    }

    @AllArgsConstructor
    public enum Type {
        EMAIL("email"),
        JIRA("jira"),
        WEBHOOK("webhook");

        private String value;

        @JsonValue
        public String getValue() {
            return value;
        }
    }

    @AllArgsConstructor
    public enum Event {
        SUCCESS,
        WAITING,
        FAILURE,
        CANCELED,
        STARTED;
    }
}
