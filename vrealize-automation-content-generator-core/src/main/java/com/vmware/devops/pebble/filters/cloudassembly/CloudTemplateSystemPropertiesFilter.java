/*
 * Copyright 2021-2021 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.vmware.devops.pebble.filters.cloudassembly;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import com.mitchellbosecke.pebble.error.PebbleException;
import com.mitchellbosecke.pebble.extension.Filter;
import com.mitchellbosecke.pebble.template.EvaluationContext;
import com.mitchellbosecke.pebble.template.PebbleTemplate;

public class CloudTemplateSystemPropertiesFilter implements Filter {
    private static String IS_SYSTEM_PROPERTY_INPUT_ARG_NAME = "isSystemProperty";

    private static final Set<String> systemProperties = Set.of(
            "flavor",
            "image"
    );

    @Override
    public Object apply(Object properties, Map<String, Object> args, PebbleTemplate self,
            EvaluationContext context, int lineNumber) throws PebbleException {
        Map<String, Object> result = new TreeMap<>();

        Boolean isSystemProperty = (Boolean) args.get(IS_SYSTEM_PROPERTY_INPUT_ARG_NAME);
        ((Map<String, Object>) properties).entrySet().stream()
                .filter(e -> {
                    if (isSystemProperty) {
                        return systemProperties.contains(e.getKey());
                    } else {
                        return !systemProperties.contains(e.getKey());
                    }
                })
                .forEach(e -> result.put(e.getKey(), e.getValue()));

        return result;
    }

    @Override
    public List<String> getArgumentNames() {
        return List.of(
                IS_SYSTEM_PROPERTY_INPUT_ARG_NAME
        );
    }
}
