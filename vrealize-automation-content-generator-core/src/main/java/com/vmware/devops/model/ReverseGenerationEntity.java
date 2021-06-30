/*
 * Copyright 2021-2021 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.vmware.devops.model;

import java.io.File;
import java.util.Map;

import com.vmware.devops.ReverseGenerationContext;
import com.vmware.devops.Utils;
import com.vmware.devops.pebble.PebbleUtils;

public interface ReverseGenerationEntity<T> {
    void populateData(T root) throws Exception;

    String getTemplatePath();

    default void dump(T root, File output) throws Exception {
        populateData(root);
        Map<String, Object> pebbleContext = Map.of(
                "entity", this,
                "context", ReverseGenerationContext.getInstance()
        );
        String content = PebbleUtils.generate(pebbleContext, getTemplatePath());
        Utils.writeFile(output, content.getBytes());
    }

    void dumpAll();
}
