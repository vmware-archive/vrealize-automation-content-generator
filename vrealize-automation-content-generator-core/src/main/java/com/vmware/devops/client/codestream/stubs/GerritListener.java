/*
 * Copyright 2021-2021 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.vmware.devops.client.codestream.stubs;

import java.util.Objects;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GerritListener extends Entity {
    public static final String KIND = "GERRIT_LISTENER";

    private String name;
    private String project;
    private String endpoint;
    private String apiToken;
    private boolean connected;

    @Override
    public String getKind() {
        return KIND;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        GerritListener that = (GerritListener) o;
        return connected == that.connected &&
                Objects.equals(name, that.name) &&
                Objects.equals(project, that.project) &&
                Objects.equals(endpoint, that.endpoint) &&
                Objects.equals(apiToken, that.apiToken);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), name, project, endpoint, apiToken, connected);
    }
}