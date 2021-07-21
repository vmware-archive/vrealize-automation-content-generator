/*
 * Copyright 2021-2021 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.vmware.devops.client.catalog.stubs;

import java.io.IOException;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import com.vmware.devops.SerializationUtils;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Policy {
    private String id;
    private String name;
    private String projectId;
    private EnforcementType enforcementType;
    private Type typeId;

    @JsonDeserialize(using = DefinitionDeserializer.class)
    private Definition definition;

    public interface Definition {
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LeaseDefinition implements Definition {
        private int leaseGrace;
        private int leaseTermMax;
        private int leaseTotalTermMax;
    }

    public enum EnforcementType {
        SOFT,
        HARD
    }

    @AllArgsConstructor
    public enum Type {
        LEASE("com.vmware.policy.deployment.lease");

        @Getter
        @JsonValue
        private String id;
    }

    private static class DefinitionDeserializer extends JsonDeserializer<LeaseDefinition> {
        @Override
        public LeaseDefinition deserialize(JsonParser p, DeserializationContext ctxt)
                throws IOException {
            Type type = ((Policy) p.getParsingContext().getParent().getCurrentValue()).typeId;
            TreeNode treeNode = p.readValueAsTree();
            switch (type) {
            case LEASE:
                return SerializationUtils.getJsonMapper()
                        .treeToValue(treeNode, LeaseDefinition.class);
            default:
                throw new IllegalStateException("Unknown input type " + type);

            }
        }
    }
}
