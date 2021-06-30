/*
 * Copyright 2021-2021 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.vmware.devops.model.codestream;

/**
 * No-op stage, it will be filtered out and not send to vRA
 * Handy when you're building stage lists programmatically
 */
public class NoOpStage extends Stage {
    public static final NoOpStage INSTANCE = new NoOpStage();

    private NoOpStage() {
    }
}
