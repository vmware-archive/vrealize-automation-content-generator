/*
 * Copyright 2021-2021 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.vmware.devops.model.codestream;

import java.util.Collections;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ParallelTask implements {@link com.vmware.devops.model.codestream.Task} and can be included in
 * CodeStream Stage. It represent parallel CodeStream Tasks of any type that implements {@link
 * com.vmware.devops.model.codestream.Task}.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ParallelTask implements Task {

    /**
     * List of the tasks which to be parallel.
     * <p>
     * vRealize Automation Content Generator will unpack the
     * list and will construct the parallel description for you.
     */
    private List<CodestreamTask> tasks = Collections.emptyList();
}
