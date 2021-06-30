/*
 * Copyright 2021-2021 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.vmware.devops.model;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.vmware.devops.client.Client;
import com.vmware.devops.client.cloudassembly.design.stubs.Blueprint;
import com.vmware.devops.client.cloudassembly.extensibility.stubs.Action;
import com.vmware.devops.client.cloudassembly.extensibility.stubs.Subscription;
import com.vmware.devops.client.cloudassembly.infrastructure.stubs.CloudZone;
import com.vmware.devops.client.cloudassembly.infrastructure.stubs.DataCollector;
import com.vmware.devops.client.cloudassembly.infrastructure.stubs.Endpoint;
import com.vmware.devops.client.cloudassembly.infrastructure.stubs.ImageName;
import com.vmware.devops.client.cloudassembly.infrastructure.stubs.InstanceName;
import com.vmware.devops.client.cloudassembly.infrastructure.stubs.Project;
import com.vmware.devops.client.cloudassembly.infrastructure.stubs.Region;
import com.vmware.devops.client.codestream.stubs.CloudProxy;
import com.vmware.devops.client.codestream.stubs.Pipeline;
import com.vmware.devops.client.codestream.stubs.Variable;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
public class VraExportedData {
    // Cloud Assembly
    List<Blueprint> blueprints;
    List<Project> projects;
    List<CloudZone> cloudZones;
    List<Region> regions;
    List<Action> actions;
    List<Subscription> subscriptions;
    List<InstanceName> instanceNames;
    List<ImageName> imageNames;
    List<Endpoint> endpoints;
    List<DataCollector> dataCollectors;
    // Codestream
    List<Pipeline> pipelines;
    List<Variable> variables;
    List<CloudProxy> cloudProxies;
    List<com.vmware.devops.client.codestream.stubs.Endpoint> codestreamEndpoints;
    List<com.vmware.devops.client.codestream.stubs.GerritTrigger> gerritTriggers;

    public VraExportedData(Client client)
            throws InterruptedException, IOException, URISyntaxException {
        // Cloud Assembly
        blueprints = client.getCloudAssembly()
                .getDesign().getAllBlueprints();
        projects = client.getCloudAssembly()
                .getInfrastructure().getAllProjects();
        cloudZones = client.getCloudAssembly()
                .getInfrastructure().getAllCloudZones();
        regions = client.getCloudAssembly()
                .getInfrastructure().getAllRegions();
        actions = client.getCloudAssembly()
                .getExtensibility().getAllActions();
        subscriptions = client.getCloudAssembly()
                .getExtensibility().getAllSubscriptions();
        instanceNames = client.getCloudAssembly()
                .getInfrastructure().getAllInstanceNames();
        imageNames = client.getCloudAssembly()
                .getInfrastructure().getAllImageNames();
        endpoints = client.getCloudAssembly()
                .getInfrastructure().getAllEndpoints();
        // Codestream
        pipelines = client.getCodestream().getAllPipelines();
        variables = client.getCodestream().getAllVariables();
        codestreamEndpoints = client.getCodestream().getAllEndpoints();
        gerritTriggers = client.getCodestream().getAllGerritTriggers();

        try {
            cloudProxies = client.getCodestream().getAllCloudProxies();
        } catch (Exception e) {
            log.warn("Failed to fetch cloud proxies. This is expected to fail when running on-prem");
            cloudProxies = Collections.emptyList();
        }
    }
}
