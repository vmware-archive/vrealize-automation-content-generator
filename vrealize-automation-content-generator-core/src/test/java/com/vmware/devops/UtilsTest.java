/*
 * Copyright 2021-2021 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.vmware.devops;

import java.io.IOException;
import java.util.Properties;

import org.junit.Assert;
import org.junit.Test;

public class UtilsTest {

    @Test
    public void testReadFile() throws IOException {
        String content = Utils.readFile("test-property-file.properties");
        Assert.assertEquals("k = v", content);
    }

    @Test
    public void testReadProperties() {
        Properties properties = Utils.readProperties("test-property-file.properties");
        Assert.assertEquals("v", properties.get("k"));
    }
}
