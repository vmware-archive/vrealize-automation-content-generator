/*
 * Copyright 2021-2021 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.vmware.devops.pebble.filters.codestream;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.mitchellbosecke.pebble.error.PebbleException;
import com.mitchellbosecke.pebble.extension.Filter;
import com.mitchellbosecke.pebble.template.EvaluationContext;
import com.mitchellbosecke.pebble.template.PebbleTemplate;

import com.vmware.devops.client.codestream.stubs.Notification.Type;
import com.vmware.devops.model.codestream.Notification;

public class NotificationFilter implements Filter {
    private static String TYPE_INPUT_ARG_NAME = "type";

    @Override
    public Object apply(Object list, Map<String, Object> args, PebbleTemplate self,
            EvaluationContext context, int lineNumber) throws PebbleException {
        List<Notification> notifications = (List<Notification>) list;

        Type type = Type.valueOf(((String) args.get(TYPE_INPUT_ARG_NAME)).toUpperCase());

        return notifications.stream().filter(n -> n.getType().equals(type))
                .collect(Collectors.toList());
    }

    @Override
    public List<String> getArgumentNames() {
        return List.of(
                TYPE_INPUT_ARG_NAME
        );
    }
}
