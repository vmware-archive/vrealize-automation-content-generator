/*
 * Copyright 2021-2021 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.vmware.devops.model.codestream;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public interface WithOutputs {
    List<Output> getOutputs();

    static List<Output> fromMap(Map<String, String> m) {
        return m.entrySet().parallelStream().map(e -> new Output(e.getKey(), e.getValue())).collect(
                Collectors.toList());
    }

    static List<Output> fromObject(Object o) {
        if (o instanceof List) {
            return (List<Output>) o;
        }

        if (o instanceof Map) {
            return fromMap((Map<String, String>) o);
        }

        throw new IllegalArgumentException("Cannot cast object to outputs: " + o.getClass());
    }
}
