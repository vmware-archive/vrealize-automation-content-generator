/*
 * Copyright 2021-2021 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.vmware.devops.model.codestream;

import com.vmware.devops.client.codestream.stubs.Task.Type;

public interface CodestreamTask {
    String getName();

    com.vmware.devops.client.codestream.stubs.Task initializeTask();

    void populateData(Pipeline parent, String name,
            com.vmware.devops.client.codestream.stubs.Task task) throws Exception;

    default void dump(Pipeline parent, String name,
            com.vmware.devops.client.codestream.stubs.Task task) throws Exception {
    }

    Type getType();
}
