/*
 * Copyright 2021-2021 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.vmware.devops.pebble.filters.codestream;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import com.mitchellbosecke.pebble.error.PebbleException;
import com.mitchellbosecke.pebble.extension.Filter;
import com.mitchellbosecke.pebble.template.EvaluationContext;
import com.mitchellbosecke.pebble.template.PebbleTemplate;

import com.vmware.devops.model.codestream.Input;

public class SortInputListFilter implements Filter {
    @Override
    public Object apply(Object list, Map<String, Object> args, PebbleTemplate self,
            EvaluationContext context, int lineNumber) throws PebbleException {
        Collections.sort((List<Input>) list, Comparator.comparing(o -> o.getKey()));
        return list;
    }

    @Override
    public List<String> getArgumentNames() {
        return null;
    }
}
