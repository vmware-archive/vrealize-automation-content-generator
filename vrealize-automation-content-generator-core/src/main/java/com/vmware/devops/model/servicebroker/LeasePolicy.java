/*
 * Copyright 2021-2021 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.vmware.devops.model.servicebroker;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.vmware.devops.GenerationContext;
import com.vmware.devops.IdCache;
import com.vmware.devops.ReverseGenerationContext;
import com.vmware.devops.client.servicebroker.stubs.Policy;
import com.vmware.devops.client.servicebroker.stubs.Policy.EnforcementType;
import com.vmware.devops.client.servicebroker.stubs.Policy.LeaseDefinition;
import com.vmware.devops.client.servicebroker.stubs.Policy.Type;
import com.vmware.devops.model.GenerationEntity;
import com.vmware.devops.model.ReverseGenerationEntity;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
public class LeasePolicy implements GenerationEntity, ReverseGenerationEntity<Policy> {

    /**
     * Name of the policy
     */
    private String name;


    /**
     * Project name is you want to apply this policy to a single project. Leave empty for org-scope
     * policies.
     */
    private String project;

    /**
     * Enforcement type. Default is {@link EnforcementType#HARD}
     */
    @Builder.Default
    private EnforcementType enforcementType = EnforcementType.HARD;

    /**
     * Max total lease. Default value is 365
     */
    @Builder.Default
    private Integer maxTotalLease = 365;

    /**
     * Max lease. Default value is 7
     */
    @Builder.Default
    private Integer maxLease = 7;

    /**
     * Grace period. Default value is 0
     */
    @Builder.Default
    private Integer gracePeriod = 0;

    public Policy initializePolicy() {
        Policy policy = Policy.builder()
                .name(name)
                .typeId(Type.LEASE)
                .enforcementType(enforcementType)
                .definition(
                        LeaseDefinition.builder()
                                .leaseGrace(gracePeriod)
                                .leaseTotalTermMax(maxTotalLease)
                                .leaseTermMax(maxLease)
                                .build()
                )
                .build();

        if (project != null) {
            try {
                policy.setProjectId(IdCache.PROJECT_ID_CACHE.getId(project));
            } catch (Exception e) {
                throw new IllegalStateException(
                        "Failed to resolve ID for project with name " + project, e);
            }
        }

        return policy;
    }

    @Override
    public void generate() throws Exception {
        Policy policy = initializePolicy();
        GenerationContext.getInstance().getEndpointConfiguration().getClient()
                .getServiceBroker().createOrUpdatePolicy(policy);
    }

    @Override
    public void populateData(Policy policy) {
        name = policy.getName();
        if (policy.getProjectId() != null) {
            project = ReverseGenerationContext.getInstance().getVraExportedData().getProjects()
                    .stream()
                    .filter(p -> p.getId().equals(policy.getProjectId())).findFirst().get()
                    .getName();
        }
        enforcementType = policy.getEnforcementType();
        maxLease = ((LeaseDefinition)policy.getDefinition()).getLeaseTermMax();
        maxTotalLease = ((LeaseDefinition)policy.getDefinition()).getLeaseTotalTermMax();
        gracePeriod = ((LeaseDefinition)policy.getDefinition()).getLeaseGrace();
    }

    @Override
    public String getTemplatePath() {
        return "templates/servicebroker/leasePolicyReverseGenerate.groovy.peb";
    }

    @Override
    public void dumpAll() {
        boolean failed = false;
        for (Policy p : ReverseGenerationContext.getInstance().getVraExportedData()
                .getPolicies()) {
            if (p.getTypeId() == Type.LEASE) {
                try {
                    dump(p, ReverseGenerationContext.getInstance()
                            .newOutputDirFile(p.getName() + "-lease-policy.groovy"));
                } catch (Exception e) {
                    failed = true;
                    log.error("Failed to export lease policy " + p.getName(), e);
                }
            }
        }

        if (failed) {
            throw new RuntimeException("At least one lease policy export failed.");
        }
    }
}
