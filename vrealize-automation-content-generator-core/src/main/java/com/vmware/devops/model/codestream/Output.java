/*
 * Copyright 2021-2021 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.vmware.devops.model.codestream;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Output entity represent output of different objects in CodeStream like Pipeline and Task.
 */
@AllArgsConstructor
@Data
public class Output {
    /**
     * Local key of the output to be bind to.
     */
    private String localKey;

    /**
     * Global key of the output which to refer to later.
     */
    private String globalKey;

    /**
     * Method which generate the reference to task output
     * @param stageName
     *         Name of the stage
     * @param taskName
     *         Name of the task
     * @param key
     *         Key which we want to expose
     * @return Reference to output of task
     */
    public static String taskOutputReference(String stageName, String taskName, String key) {
        return String.format("${%s.%s.output.%s}", stageName, taskName, key);
    }
}
