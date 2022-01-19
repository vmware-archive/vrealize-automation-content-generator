/*
 * Copyright 2021-2021 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.vmware.devops.config;

import java.io.IOException;
import java.net.URISyntaxException;

import lombok.Getter;
import lombok.Setter;

import com.vmware.devops.client.Client;

public class EndpointConfiguration {
    @Getter
    @Setter
    private String instance;

    @Getter
    @Setter
    private String loginInstance;

    private AuthenticationDetails authenticationDetails;

    private Client client;

    public synchronized Client getClient()
            throws InterruptedException, IOException, URISyntaxException {
        if (client == null) {
            if (authenticationDetails.refreshToken != null) {
                client = new Client(loginInstance, instance, authenticationDetails.refreshToken);
            } else {
                client = new Client(loginInstance, instance, authenticationDetails.username,
                        authenticationDetails.password);
                synchronized (this) {
                    authenticationDetails.refreshToken = client.getRefreshToken();
                }
            }
        }

        return client;
    }

    public static class AuthenticationDetails {
        @Getter
        private String username;

        @Getter
        private String password;

        @Getter
        private String refreshToken;

        public AuthenticationDetails(String refreshToken) {
            this.refreshToken = refreshToken;
        }

        public AuthenticationDetails(String username, String password) {
            this.username = username;
            this.password = password;
        }
    }

    public synchronized AuthenticationDetails getAuthenticationDetails() {
        return authenticationDetails;
    }

    public synchronized void setAuthenticationDetails(
            AuthenticationDetails authenticationDetails) {
        this.authenticationDetails = authenticationDetails;
    }
}
