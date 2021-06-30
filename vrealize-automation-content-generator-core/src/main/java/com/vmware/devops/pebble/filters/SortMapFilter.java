/*
 * Copyright 2021-2021 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.vmware.devops.pebble.filters;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.mitchellbosecke.pebble.error.PebbleException;
import com.mitchellbosecke.pebble.extension.Filter;
import com.mitchellbosecke.pebble.template.EvaluationContext;
import com.mitchellbosecke.pebble.template.PebbleTemplate;

public class SortMapFilter implements Filter {
    @Override
    public Object apply(Object map, Map<String, Object> args, PebbleTemplate self,
            EvaluationContext context, int lineNumber) throws PebbleException {
        return new TreeMap<>((Map) map);
    }

    @Override
    public List<String> getArgumentNames() {
        return null;
    }
}
