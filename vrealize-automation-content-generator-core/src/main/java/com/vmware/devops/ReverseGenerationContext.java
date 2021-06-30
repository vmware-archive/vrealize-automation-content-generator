/*
 * Copyright 2021-2021 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.vmware.devops;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import lombok.Data;

import com.vmware.devops.config.EndpointConfiguration;
import com.vmware.devops.model.VraExportedData;

@Data
public class ReverseGenerationContext {
    private static ReverseGenerationContext reverseGenerationContext;

    private EndpointConfiguration endpointConfiguration = new EndpointConfiguration();
    private String outputDir;
    private VraExportedData vraExportedData;

    public void initializeVraExportedData()
            throws InterruptedException, IOException, URISyntaxException {
        vraExportedData = new VraExportedData(endpointConfiguration.getClient());
    }

    public static synchronized ReverseGenerationContext getInstance() {
        if (reverseGenerationContext == null) {
            reverseGenerationContext = new ReverseGenerationContext();
        }

        return reverseGenerationContext;
    }

    public File newOutputDirFile(String fileName) {
        return new File(new File(outputDir), fileName);
    }
}
