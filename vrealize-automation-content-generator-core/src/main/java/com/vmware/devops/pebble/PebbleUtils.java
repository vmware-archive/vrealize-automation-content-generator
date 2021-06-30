/*
 * Copyright 2021-2021 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.vmware.devops.pebble;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;

import com.mitchellbosecke.pebble.PebbleEngine;
import com.mitchellbosecke.pebble.template.PebbleTemplate;

public class PebbleUtils {
    private PebbleUtils() {}

    private static PebbleEngine pebbleEngine;

    public static String generate(Map<String, Object> pebbleContext, String pebbleTemplatePath)
            throws IOException {

        PebbleTemplate pipelineTemplate = getPebbleEngine()
                .getTemplate(pebbleTemplatePath);

        StringWriter writer = new StringWriter();
        pipelineTemplate.evaluate(writer, pebbleContext);
        return writer.toString();
    }

    private static synchronized PebbleEngine getPebbleEngine() {
        if (pebbleEngine == null) {
            pebbleEngine = new PebbleEngine.Builder()
                    .extension(new GeneratorExtension())
                    .strictVariables(true)
                    .autoEscaping(false)
                    .newLineTrimming(false)
                    .build();
        }

        return pebbleEngine;
    }
}
