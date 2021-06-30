/*
 * Copyright 2021-2021 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.vmware.devops.pebble.filters.cloudassembly;

import java.util.List;
import java.util.Map;

import com.mitchellbosecke.pebble.error.PebbleException;
import com.mitchellbosecke.pebble.extension.Filter;
import com.mitchellbosecke.pebble.template.EvaluationContext;
import com.mitchellbosecke.pebble.template.PebbleTemplate;

public class CriteriaFilter implements Filter {
    private static String TYPE_INPUT_ARG_NAME = "type";

    @Override
    public Object apply(Object input, Map<String, Object> args, PebbleTemplate self,
            EvaluationContext context, int lineNumber) throws PebbleException {
        return ((String) input).replaceAll("\"", "\\\\\"");
    }

    @Override
    public List<String> getArgumentNames() {
        return List.of(
                TYPE_INPUT_ARG_NAME);
    }
}
