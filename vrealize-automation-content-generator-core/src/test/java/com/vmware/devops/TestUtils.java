/*
 * Copyright 2021-2021 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.vmware.devops;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Properties;

public class TestUtils {
    private static Properties properties = loadProperties();

    public static String getProperty(String key) {
        return System.getProperty(key, properties.getProperty(key));
    }

    public static Properties loadProperties() {
        Properties properties = new Properties();
        try {
            properties.load(Thread.currentThread().getContextClassLoader()
                    .getResourceAsStream(
                            System.getProperty("testPropertiesFile", "test.properties")));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }

        return properties;
    }

    public static File createTempDir() throws IOException {
        return Files.createTempDirectory("vrealize-automation-content-generator-test").toFile();
    }
}
