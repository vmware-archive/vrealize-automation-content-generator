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

import com.vmware.devops.client.cloudassembly.infrastructure.stubs.Endpoint.EndpointType;
import com.vmware.devops.model.cloudassembly.infrastructure.Flavor;
import com.vmware.devops.model.cloudassembly.infrastructure.NimbusCloudAccount.NimbusFlavor;
import com.vmware.devops.model.cloudassembly.infrastructure.VsphereCloudAccount.VsphereFlavor;

public class FlavorMappingFilter implements Filter {
    private static String TYPE_INPUT_ARG_NAME = "type";

    @Override
    public Object apply(Object input, Map<String, Object> args, PebbleTemplate self,
            EvaluationContext context, int lineNumber) throws PebbleException {
        Map<String, Flavor> result = new TreeMap<>();

        String type = (String) args.get(TYPE_INPUT_ARG_NAME);
        if (type.equalsIgnoreCase(EndpointType.VSPHERE.getValue())) {
            ((Map<String, Flavor>) input).entrySet().stream()
                    .filter(e -> e.getValue() instanceof VsphereFlavor)
                    .forEach(e -> result.put(e.getKey(), e.getValue()));
            return result;
        }
        if (type.equalsIgnoreCase(EndpointType.NIMBUS.getValue())) {
            ((Map<String, Flavor>) input).entrySet().stream()
                    .filter(e -> e.getValue() instanceof NimbusFlavor)
                    .forEach(e -> result.put(e.getKey(), e.getValue()));
            return result;
        }
        throw new UnsupportedOperationException("Unknown flavor type: " + type);
    }

    @Override
    public List<String> getArgumentNames() {
        return List.of(
                TYPE_INPUT_ARG_NAME);
    }
}
