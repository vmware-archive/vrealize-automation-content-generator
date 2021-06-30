/*
 * Copyright 2021-2021 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.vmware.devops;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateEncodingException;

import org.junit.Assert;
import org.junit.Test;

public class UtilsIT {

    @Test
    public void testGetCertificateFromUrl() throws IOException, KeyManagementException, NoSuchAlgorithmException,
            CertificateEncodingException {
        Assert.assertNotNull(Utils.getCertificateFromUrl("https://google.com"));
    }

}
