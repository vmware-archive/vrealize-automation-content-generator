/*
 * Copyright 2021-2021 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.vmware.devops;

import org.junit.Before;

public class GenerationTestBase {
    @Before
    public void cleanContext() {
        GenerationContext.reset();
    }
}
