/*
 * Copyright 2021-2021 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.vmware.devops.config;

import java.io.IOException;
import java.net.URISyntaxException;

import lombok.Data;
import lombok.Getter;

import com.vmware.devops.client.Client;

@Data
public class EndpointConfiguration {
    private String instance;
    private String loginInstance;
    private AuthenticationDetails authenticationDetails;

    public Client getClient() throws InterruptedException, IOException, URISyntaxException {
        if (authenticationDetails.refreshToken != null) {
            return new Client(loginInstance, instance, authenticationDetails.refreshToken);
        } else {
            Client client = new Client(loginInstance, instance, authenticationDetails.username,
                    authenticationDetails.password);
            authenticationDetails.refreshToken = client.getRefreshToken();
            return client;
        }
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
}
