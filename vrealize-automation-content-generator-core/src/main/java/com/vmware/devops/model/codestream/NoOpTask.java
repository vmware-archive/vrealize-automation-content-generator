/*
 * Copyright 2021-2021 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.vmware.devops.model.codestream;

import com.vmware.devops.client.codestream.stubs.Task.Type;

/**
 * No-op task, it will be filtered out and not send to vRA
 * Handy when you're building task lists programmatically
 */
public class NoOpTask implements Task, CodestreamTask {
    public static final NoOpTask INSTANCE = new NoOpTask();

    private NoOpTask() {
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public com.vmware.devops.client.codestream.stubs.Task initializeTask() {
        return null;
    }

    @Override
    public void populateData(Pipeline parent, String name,
            com.vmware.devops.client.codestream.stubs.Task task) {
    }

    @Override
    public Type getType() {
        return null;
    }
}
