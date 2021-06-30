/*
 * Copyright 2021-2021 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.vmware.devops.model.codestream;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import lombok.Getter;

import com.vmware.devops.GenerationContext;
import com.vmware.devops.model.GenerationEntity;

public abstract class Endpoint implements GenerationEntity {
    public static final EndpointFingerprintCache ENDPOINT_FINGERPRINT_CACHE = new EndpointFingerprintCache();

    public abstract com.vmware.devops.client.codestream.stubs.Endpoint initializeEndpoint()
            throws Exception;

    @Override
    public void generate() throws Exception {
        com.vmware.devops.client.codestream.stubs.Endpoint endpoint = initializeEndpoint();
        GenerationContext.getInstance().getEndpointConfiguration().getClient()
                .getCodestream().createOrUpdateEndpoint(endpoint);
    }

    public static class EndpointFingerprintCache {

        @Getter
        private final Map<String, String> urlToFingerprint = new ConcurrentHashMap<>();

        private EndpointFingerprintCache() {
        }

        public String getFingerprint(String url, String cloudProxyId)
                throws InterruptedException, IOException, URISyntaxException {
            if (urlToFingerprint.get(url) == null) {
                urlToFingerprint.put(url,
                        GenerationContext.getInstance().getEndpointConfiguration().getClient()
                                .getCodestream().getEndpointCertificate(url, cloudProxyId)
                                .getCertificates().get(0)
                                .getFingerprints().get("SHA-256"));
            }

            return urlToFingerprint.get(url);
        }
    }
}
