/*
 * Copyright 2021-2021 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.vmware.devops.model.codestream;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.vmware.devops.GenerationContext;
import com.vmware.devops.ReverseGenerationContext;
import com.vmware.devops.client.codestream.stubs.Variable.VariableType;
import com.vmware.devops.model.GenerationEntity;
import com.vmware.devops.model.ReverseGenerationEntity;

/**
 * Variable entity represent Variable object in CodeStream
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@Slf4j
public class Variable implements GenerationEntity,
        ReverseGenerationEntity<com.vmware.devops.client.codestream.stubs.Variable> {
    /**
     * Name of the variable.
     */
    private String name;

    /**
     * Description of the variable.
     */
    private String description;

    /**
     * Value of the variable.
     */
    private String value;

    /**
     * Type of the variable.
     * <p>
     * By default REGULAR type is used.
     * See {@link com.vmware.devops.client.codestream.stubs.Variable.VariableType}
     */
    @Builder.Default
    private VariableType type = VariableType.REGULAR;

    /**
     * Method for generating the reference of the variable.
     * @param key
     *         Name of the variable
     * @return Reference of the variable to be used
     */
    public static String variableReference(String key) {
        return String.format("${var.%s}", key);
    }

    public com.vmware.devops.client.codestream.stubs.Variable initializeVariable() {
        return com.vmware.devops.client.codestream.stubs.Variable.builder()
                .project(GenerationContext.getInstance().getGlobalConfiguration()
                        .getDefaultProject())
                .name(name)
                .description(description)
                .type(type)
                .value(value)
                .build();
    }

    @Override
    public void generate() throws Exception {
        com.vmware.devops.client.codestream.stubs.Variable variable = initializeVariable();
        GenerationContext.getInstance().getEndpointConfiguration().getClient().getCodestream()
                .createOrUpdateVariable(variable);
    }

    /**
     * Method for generating the reference of the variable.
     * @return Reference of the variable to be used
     */
    public String variableReference() {
        return variableReference(this.name);
    }

    @Override
    public void populateData(com.vmware.devops.client.codestream.stubs.Variable variable)
            throws Exception {
        name = variable.getName();
        description = variable.getDescription();
        type = variable.getType();
        value = variable.getValue();
    }

    @Override
    public String getTemplatePath() {
        return "templates/codestream/variableReverseGenerate.groovy.peb";
    }

    @Override
    public void dumpAll() {
        boolean failed = false;
        for (com.vmware.devops.client.codestream.stubs.Variable variable : ReverseGenerationContext
                .getInstance().getVraExportedData().getVariables()) {
            try {
                dump(variable, ReverseGenerationContext.getInstance()
                        .newOutputDirFile("010-" + variable.getName() + "-variable.groovy"));
            } catch (Exception e) {
                failed = true;
                log.error("Failed to export variable " + variable.getName(), e);
            }
        }

        if (failed) {
            throw new RuntimeException("At least one variable export failed.");
        }
    }
}
