/*
 * Copyright 2021-2021 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

import com.vmware.devops.model.cloudassembly.design.CloudTemplate

return CloudTemplate.builder()
        .name("vrealize-automation-content-generator-test")
        .contentPath("tests/cloudassembly/design/basicBlueprintContent.yaml")
        .build()