/*
 * Copyright 2021-2021 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.vmware.devops.model.cloudassembly.infrastructure;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.vmware.devops.GenerationContext;
import com.vmware.devops.IdCache;
import com.vmware.devops.ReverseGenerationContext;
import com.vmware.devops.client.Client;
import com.vmware.devops.client.cloudassembly.infrastructure.InfrastructureClient;
import com.vmware.devops.client.cloudassembly.infrastructure.InfrastructureClient.ModifyProjectPrincipalRequest;
import com.vmware.devops.client.cloudassembly.infrastructure.InfrastructureClient.UpdateProjectPrincipalsRequest;
import com.vmware.devops.client.cloudassembly.infrastructure.stubs.CloudZone;
import com.vmware.devops.client.cloudassembly.infrastructure.stubs.Project.PlacementPolicy;
import com.vmware.devops.client.cloudassembly.infrastructure.stubs.ProjectConfig;
import com.vmware.devops.client.cloudassembly.infrastructure.stubs.ProjectConfig.ProjectState;
import com.vmware.devops.client.cloudassembly.infrastructure.stubs.ProjectPrincipal;
import com.vmware.devops.client.cloudassembly.infrastructure.stubs.ProjectPrincipal.Role;
import com.vmware.devops.client.cloudassembly.infrastructure.stubs.ProjectPrincipal.Type;
import com.vmware.devops.config.CloudAssemblyConfiguration;
import com.vmware.devops.model.GenerationEntity;
import com.vmware.devops.model.ReverseGenerationEntity;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@Slf4j
public class Project
        implements GenerationEntity,
        ReverseGenerationEntity<com.vmware.devops.client.cloudassembly.infrastructure.stubs.Project> {
    /**
     * Project name
     */
    private String name;

    /**
     * Cloud zones enabled in the project. Defaults to all cloud zones in {@link
     * CloudAssemblyConfiguration#getDefaultCloudAccount()} in the {@link GenerationContext}
     * singleton
     */
    @Builder.Default
    private List<String> cloudZones = Collections.emptyList();

    /**
     * Placement policy for this project. Defaults to {@link CloudAssemblyConfiguration#getDefaultProjectPlacementPolicy()}
     * of {@link GenerationContext} singleton
     */
    @Builder.Default
    private PlacementPolicy placementPolicy = GenerationContext.getInstance()
            .getCloudAssemblyConfiguration()
            .getDefaultProjectPlacementPolicy();

    /**
     * Project users
     */
    @Builder.Default
    private List<UserGroup> users = Collections.emptyList();

    /**
     * Project groups
     */
    @Builder.Default
    private List<UserGroup> groups = Collections.emptyList();

    public com.vmware.devops.client.cloudassembly.infrastructure.stubs.Project initializeProject() {
        return com.vmware.devops.client.cloudassembly.infrastructure.stubs.Project.builder()
                .name(name)
                .properties(Map.of(
                        com.vmware.devops.client.cloudassembly.infrastructure.stubs.Project.PLACEMENT_POLICY_PROPERTY_KEY,
                        placementPolicy.toString()
                ))
                .build();
    }

    public ProjectConfig initializeProjectConfig() {
        return ProjectConfig.builder()
                .cloudZones(
                        cloudZones.parallelStream().map(
                                n -> {
                                    try {
                                        if (!n.contains("/")) {
                                            n = GenerationContext.getInstance()
                                                    .getCloudAssemblyConfiguration()
                                                    .getDefaultCloudAccount() + " / " + n;
                                        } else {
                                            String[] splitted = n.split("/");
                                            n = splitted[0].trim() + " / " + splitted[1]
                                                    .trim(); // make sure whitespace does not matter
                                        }

                                        return ProjectConfig.CloudZone.builder()
                                                .name(n)
                                                .placementZoneLink(
                                                        IdCache.CLOUD_ZONE_LINK_CACHE.getId(n))
                                                .build();
                                    } catch (Exception e) {
                                        throw new RuntimeException(e);
                                    }
                                }
                        ).collect(Collectors.toList())
                )
                .build();
    }

    @Override
    public void generate() throws Exception {
        com.vmware.devops.client.cloudassembly.infrastructure.stubs.Project project = initializeProject();
        ProjectConfig projectConfig = initializeProjectConfig();

        Client client = GenerationContext.getInstance()
                .getEndpointConfiguration().getClient();
        project = client.getCloudAssembly().getInfrastructure()
                .createOrUpdateProject(project);
        projectConfig.setProjectState(ProjectState.builder()
                .documentSelfLink(InfrastructureClient.PROJECTS_ENDPOINT + "/" + project.getId())
                .build());
        client.getCloudAssembly().getInfrastructure()
                .updateProjectConfig(projectConfig);
        client.getCloudAssembly().getInfrastructure()
                .updateProjectPrincipals(project.getId(),
                        createUpdateProjectPrincipalsRequest(project));

        IdCache.PROJECT_ID_CACHE.getNameToId().put(project.getName(), project.getId());
    }

    public UpdateProjectPrincipalsRequest createUpdateProjectPrincipalsRequest(
            com.vmware.devops.client.cloudassembly.infrastructure.stubs.Project project) {
        Set<String> userGroupMails = Stream.concat(users.stream(), groups.stream())
                .map(UserGroup::getEmail).collect(Collectors.toSet());

        List<ProjectPrincipal> existingUsers = new ArrayList<>();
        if (project.getAdministrators() != null) {
            existingUsers.addAll(project.getAdministrators());
        }

        if (project.getMembers() != null) {
            existingUsers.addAll(project.getMembers());
        }

        if (project.getViewers() != null) {
            existingUsers.addAll(project.getViewers());
        }

        List<ProjectPrincipal> remove = existingUsers.stream()
                .filter(p -> !userGroupMails.contains(p.getEmail()))
                .collect(
                        Collectors.toList());

        List<ModifyProjectPrincipalRequest> modify = users.stream()
                .map(u -> ModifyProjectPrincipalRequest.builder()
                        .email(u.email)
                        .type(Type.USER)
                        .role(u.role)
                        .build()
                ).collect(Collectors.toList());

        modify.addAll(groups.stream().map(u -> ModifyProjectPrincipalRequest.builder()
                .email(u.email)
                .type(Type.GROUP)
                .role(u.role)
                .build()
        ).collect(Collectors.toList()));

        return UpdateProjectPrincipalsRequest
                .builder()
                .modify(modify)
                .remove(remove)
                .build();
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserGroup {
        public static final String ALL_USERS_GROUP = "ALL USERS@";

        private String email;
        private ProjectPrincipal.Role role;
    }

    @Override
    public void populateData(
            com.vmware.devops.client.cloudassembly.infrastructure.stubs.Project project) {
        name = project.getName();
        users = new ArrayList<>();
        groups = new ArrayList<>();
        populateUsers(project.getAdministrators(), Role.ADMINISTRATOR);
        populateUsers(project.getMembers(), Role.MEMBER);
        populateUsers(project.getViewers(), Role.VIEWER);
        if (project.getProperties() != null) {
            String placementPolicyString = project.getProperties().get(
                    com.vmware.devops.client.cloudassembly.infrastructure.stubs.Project.PLACEMENT_POLICY_PROPERTY_KEY);
            if (placementPolicyString != null && !placementPolicyString.isEmpty()) {
                placementPolicy = PlacementPolicy.valueOf(placementPolicyString);
            }
        }
        List<CloudZone> exportedCloudZones = ReverseGenerationContext.getInstance()
                .getVraExportedData().getCloudZones();
        if (null != exportedCloudZones && !exportedCloudZones.isEmpty()) {
            cloudZones = exportedCloudZones.stream()
                    .filter(zone -> zone.getProjectIds().contains(project.getId()))
                    .map(CloudZone::getName)
                    .collect(Collectors.toList());
        }
    }

    private void populateUsers(List<ProjectPrincipal> principals, Role role) {
        if (principals != null && !principals.isEmpty()) {
            users.addAll(principals
                    .stream()
                    .filter(principal -> principal.getType().equals(Type.USER))
                    .map(principal -> UserGroup.builder()
                            .email(principal.getEmail())
                            .role(role)
                            .build())
                    .collect(Collectors.toList()));
            groups.addAll(principals
                    .stream()
                    .filter(principal -> principal.getType().equals(Type.GROUP))
                    .map(principal -> UserGroup.builder()
                            .email(principal.getEmail())
                            .role(role)
                            .build())
                    .collect(Collectors.toList()));
        }
    }

    @Override
    public String getTemplatePath() {
        return "templates/cloudassembly/infrastructure/projectReverseGenerate.groovy.peb";
    }

    @Override
    public void dumpAll() {
        boolean failed = false;
        for (com.vmware.devops.client.cloudassembly.infrastructure.stubs.Project project : ReverseGenerationContext
                .getInstance().getVraExportedData().getProjects()) {
            try {
                dump(project, ReverseGenerationContext.getInstance()
                        .newOutputDirFile("020-" + project.getName() + "-project.groovy"));
            } catch (Exception e) {
                failed = true;
                log.error("Failed to export project " + project.getName(), e);
            }
        }

        if (failed) {
            throw new RuntimeException("At least one project export failed.");
        }
    }
}
