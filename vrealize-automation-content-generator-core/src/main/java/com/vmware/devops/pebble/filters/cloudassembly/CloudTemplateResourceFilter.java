/*
 * Copyright 2021-2021 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.vmware.devops.pebble.filters.cloudassembly;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.mitchellbosecke.pebble.error.PebbleException;
import com.mitchellbosecke.pebble.extension.Filter;
import com.mitchellbosecke.pebble.template.EvaluationContext;
import com.mitchellbosecke.pebble.template.PebbleTemplate;

import com.vmware.devops.model.cloudassembly.design.CloudTemplate.BlueprintContent.CloudMachine;
import com.vmware.devops.model.cloudassembly.design.CloudTemplate.BlueprintContent.Resource;

public class CloudTemplateResourceFilter implements Filter {
    private static String TYPE_INPUT_ARG_NAME = "type";

    @Override
    public Object apply(Object resources, Map<String, Object> args, PebbleTemplate self,
            EvaluationContext context, int lineNumber) throws PebbleException {
        Map<String, Resource> result = new TreeMap<>();

        String type = (String) args.get(TYPE_INPUT_ARG_NAME);
        if (type.equalsIgnoreCase("machine")) {
            ((Map<String, Resource>) resources).entrySet().stream()
                    .filter(e -> e.getValue() instanceof CloudMachine)
                    .forEach(e -> result.put(e.getKey(), e.getValue()));

            return result;
        }

        throw new UnsupportedOperationException("Unknown type: " + type);
    }

    @Override
    public List<String> getArgumentNames() {
        return List.of(
                TYPE_INPUT_ARG_NAME
        );
    }
}
