/*
 * Copyright 2021-2021 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.vmware.devops.model.cloudassembly.extensibility;

import java.util.Optional;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import com.vmware.devops.IdCache;
import com.vmware.devops.client.cloudassembly.extensibility.stubs.Subscription.EventType;
import com.vmware.devops.client.cloudassembly.extensibility.stubs.Subscription.ResourceAction;

@NoArgsConstructor
@AllArgsConstructor
public class Criteria {
    public static final Criteria EMPTY_CRITERIA = new Criteria();

    @Getter
    private String criteria = "";

    /**
     * @param c
     * @return criteria of "(thisCriteria) && (paramCriteria)"
     */
    public Criteria and(Criteria c) {
        return new CompositeCriteria(this, c) {
            @Override
            protected String compose() {
                return Optional.ofNullable(checkForEmptyOperation())
                        .orElse(String.format("(%s) && (%s)", getFirst(), getSecond()));
            }
        };
    }

    /**
     * @param c
     * @return criteria of "(thisCriteria) || (paramCriteria)"
     */
    public Criteria or(Criteria c) {
        return new CompositeCriteria(this, c) {
            @Override
            protected String compose() {
                return Optional.ofNullable(checkForEmptyOperation())
                        .orElse(String.format("(%s) || (%s)", getFirst(), getSecond()));
            }
        };
    }

    /**
     * @return criteria of "!(thisCriteria)"
     */
    public Criteria not() {
        if (criteria.length() == 0) {
            return EMPTY_CRITERIA;
        }

        return new Criteria(String.format("!(%s)", criteria));
    }

    @Override
    public String toString() {
        return criteria;
    }

    @AllArgsConstructor
    public static class BlueprintNameCriteria extends Criteria {
        private String name;

        @Override
        public String toString() {
            try {
                return String.format("event.data.blueprintId == \"%s\"",
                        IdCache.BLUEPRINT_ID_CACHE.getId(name));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Equality criteria for event.data.eventType property
     */
    public static class EventTypeCriteria extends Criteria {
        public EventTypeCriteria(EventType type) {
            super(String.format("event.data.eventType == \"%s\"", type));
        }
    }

    /**
     * Equality criteria for event.data.actionName property
     */
    public static class ResourceActionNameCriteria extends Criteria {
        public ResourceActionNameCriteria(ResourceAction action) {
            super(String.format("event.data.actionName == \"%s\"", action.getValue()));
        }
    }

    @AllArgsConstructor
    public abstract static class CompositeCriteria extends Criteria {
        @Getter
        private Criteria first;

        @Getter
        private Criteria second;

        protected abstract String compose();

        @Override
        public String toString() {
            return compose();
        }

        protected String checkForEmptyOperation() {
            if (getFirst().toString().length() == 0 && getSecond().toString().length() == 0) {
                return EMPTY_CRITERIA.toString();
            }

            if (getFirst().toString().length() == 0) {
                return getSecond().toString();
            }

            if (getSecond().toString().length() == 0) {
                return getFirst().toString();
            }

            return null;
        }
    }
}
