/*
 * Copyright 2021-2021 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.vmware.devops.client.codestream.stubs;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Endpoint {
    private String id;
    private String name;
    private String desciption;
    private Map<String, Object> properties;
    private Type type;
    private String cloudProxyId;
    private String project;

    public enum Type {
        JENKINS,
        GERRIT,
        AGENT,
        EMAIL,
        JIRA;

        @JsonValue
        public String getValue() {
            return name().toLowerCase();
        }
    }

    public enum EmailEncryptionMethod {
        TLS,
        SSL,
        NONE
    }

    public enum EmailProtocol {
        SMTP,
        POP3;

        @JsonValue
        public String getValue() {
            return name().toLowerCase();
        }
    }
}