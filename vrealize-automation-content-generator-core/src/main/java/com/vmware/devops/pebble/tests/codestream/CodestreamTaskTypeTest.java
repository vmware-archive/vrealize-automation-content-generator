/*
 * Copyright 2021-2021 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.vmware.devops.pebble.tests.codestream;

import java.util.List;
import java.util.Map;

import com.mitchellbosecke.pebble.extension.Test;
import com.mitchellbosecke.pebble.template.EvaluationContext;
import com.mitchellbosecke.pebble.template.PebbleTemplate;

import com.vmware.devops.client.codestream.stubs.Task.Type;
import com.vmware.devops.model.codestream.CodestreamTask;
import com.vmware.devops.model.codestream.ParallelTask;

public class CodestreamTaskTypeTest implements Test {

    private static final String TYPE_ARG_KEY = "type";
    public static final String PARALLEL_TYPE = "parallel";

    @Override
    public List<String> getArgumentNames() {
        return List.of(TYPE_ARG_KEY);
    }

    @Override
    public boolean apply(Object task, Map<String, Object> args, PebbleTemplate self,
            EvaluationContext context, int lineNumber) {

        String typeArg = ((String) args.get(TYPE_ARG_KEY));

        if (typeArg.equals(PARALLEL_TYPE) && !(task instanceof ParallelTask)) {
            return false;
        }

        if (task instanceof ParallelTask) {
            return PARALLEL_TYPE.equalsIgnoreCase(typeArg);
        }

        return Type.valueOf(((String) args.get(TYPE_ARG_KEY)).toUpperCase())
                .equals(((CodestreamTask) task).getType());
    }

}