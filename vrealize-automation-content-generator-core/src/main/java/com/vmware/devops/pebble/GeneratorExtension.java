/*
 * Copyright 2021-2021 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.vmware.devops.pebble;

import java.util.Map;

import com.mitchellbosecke.pebble.extension.AbstractExtension;
import com.mitchellbosecke.pebble.extension.Filter;
import com.mitchellbosecke.pebble.extension.Test;

import com.vmware.devops.pebble.filters.SortMapFilter;
import com.vmware.devops.pebble.filters.cloudassembly.CloudTemplateInputFilter;
import com.vmware.devops.pebble.filters.cloudassembly.CloudTemplateResourceFilter;
import com.vmware.devops.pebble.filters.cloudassembly.CloudTemplateSystemPropertiesFilter;
import com.vmware.devops.pebble.filters.cloudassembly.CriteriaFilter;
import com.vmware.devops.pebble.filters.cloudassembly.FlavorMappingFilter;
import com.vmware.devops.pebble.filters.codestream.NotificationFilter;
import com.vmware.devops.pebble.filters.codestream.SortInputListFilter;
import com.vmware.devops.pebble.tests.codestream.CodestreamTaskTypeTest;

public class GeneratorExtension extends AbstractExtension {

    @Override
    public Map<String, Filter> getFilters() {
        return Map.of(
                "cloudTemplateInput", new CloudTemplateInputFilter(),
                "cloudTemplateResource", new CloudTemplateResourceFilter(),
                "cloudTemplateSystemProperties", new CloudTemplateSystemPropertiesFilter(),
                "flavorMapping", new FlavorMappingFilter(),
                "criteria", new CriteriaFilter(),
                "sortMap", new SortMapFilter(),
                "sortInputList", new SortInputListFilter(),
                "codestreamNotificationFilter", new NotificationFilter()
        );
    }

    @Override
    public Map<String, Test> getTests() {
        return Map.of(
                "codestreamTaskType", new CodestreamTaskTypeTest()
        );
    }
}
