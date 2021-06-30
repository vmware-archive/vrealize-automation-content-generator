/*
 * Copyright 2021-2021 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.vmware.devops;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;

public class SpecProcessor {
    public Object process(String content) {
        Binding binding = new Binding();
        binding.setVariable("context", GenerationContext.getInstance());
        GroovyShell shell = new GroovyShell(binding);

        return shell.evaluate(content);
    }
}
