/*
 * Copyright 2021-2021 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.vmware.devops.model.codestream;

import com.vmware.devops.client.codestream.stubs.Notification.Type;

public interface Notification<T extends com.vmware.devops.client.codestream.stubs.Notification> {
    Type getType();

    com.vmware.devops.client.codestream.stubs.Notification initializeNotification();

    void populateData(T notification);
}
