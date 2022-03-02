/*
 * Copyright 2021-2021 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.vmware.devops.model.codestream;

/**
 * No-op input, it will be filtered out and not send to vRA
 * Handy when you're building task lists programmatically
 */
public class NoOpInput extends Input {
    public static final NoOpInput INSTANCE = new NoOpInput();

    private NoOpInput() {
    }
}
