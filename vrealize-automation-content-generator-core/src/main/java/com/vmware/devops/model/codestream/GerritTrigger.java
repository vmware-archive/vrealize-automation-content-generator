/*
 * Copyright 2021-2021 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.vmware.devops.model.codestream;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.vmware.devops.GenerationContext;
import com.vmware.devops.ReverseGenerationContext;
import com.vmware.devops.client.codestream.stubs.GerritTrigger.Configuration.EventType;
import com.vmware.devops.client.codestream.stubs.GerritTrigger.Pattern.PatternType;
import com.vmware.devops.model.ReverseGenerationEntity;

/**
 * GerritTrigger entity extends {@link com.vmware.devops.model.codestream.Trigger} and represent
 * Gerrit Trigger objects in CodeStream. The entity is used to specify which pipeline to be trigger
 * based on certain conditions associated with Gerrit projects.
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@Slf4j
public class GerritTrigger extends Trigger implements
        ReverseGenerationEntity<com.vmware.devops.client.codestream.stubs.GerritTrigger> {
    /**
     * Configuration is the entity that specify the main configuration of the {@link
     * com.vmware.devops.model.codestream.GerritTrigger}
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Configuration {
        /**
         * Name of the pipeline which to be triggers by the trigger.
         */
        private String pipeline;

        /**
         * Map of inputs to be provided to the pipeline on trigger event.
         * <p>
         * Key-value pairs of strings.
         */
        private Map<String, String> input;

        /**
         * Comment which to be posted on Gerrit object in case of pipeline failure.
         */
        private String failureComment;

        /**
         * Comment which to be posted on Gerrit object in case of pipeline success.
         */
        private String successComment;

        /**
         * Event type on which the trigger to perform action.
         * <p>
         * By default the event type is CHANGE_MERGED.
         * See {@link com.vmware.devops.client.codestream.stubs.GerritTrigger.Configuration.EventType}
         */
        @Builder.Default
        private EventType eventType = EventType.CHANGE_MERGED;

        public com.vmware.devops.client.codestream.stubs.GerritTrigger.Configuration initializeConfiguration() {
            return com.vmware.devops.client.codestream.stubs.GerritTrigger.Configuration
                    .builder()
                    .pipeline(pipeline)
                    .input(input)
                    .eventType(eventType)
                    .failureComment(failureComment)
                    .successComment(successComment)
                    .build();
        }
    }

    /**
     * Pattern entity specify what type of pattern to be used for trigger configuration
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Pattern {
        /**
         * Value of the pattern
         */
        private String value;

        /**
         * Type of the pattern.
         * <p>
         * By default PLAIN is used.
         * See {@link com.vmware.devops.client.codestream.stubs.GerritTrigger.Pattern.PatternType}
         */
        @Builder.Default
        private PatternType type = PatternType.PLAIN;

        public com.vmware.devops.client.codestream.stubs.GerritTrigger.Pattern initializePattern() {
            return com.vmware.devops.client.codestream.stubs.GerritTrigger.Pattern
                    .builder()
                    .value(value)
                    .type(type)
                    .build();
        }

    }

    /**
     * The name of the Gerrit Trigger
     */
    private String name;

    /**
     * The name of the Gerrit Project
     */
    private String gerritProject;

    /**
     * List of different configuration for the Gerrit Trigger.
     * <p>
     * See {@link com.vmware.devops.model.codestream.GerritTrigger.Configuration
     */
    private List<Configuration> configurations;

    /**
     * The state of the Gerrit Trigger.
     * <p>
     * By default the property has value - true. If true the trigger will be enabled after
     * generation, otherwise the trigger will be
     * disabled after generation.
     */
    @Builder.Default
    private Boolean enabled = true;

    /**
     * The project of the Gerrit Trigger.
     * <p>
     * Defaults to the one specified in global configuration.
     * In case the project is specified explicitly, it will be associated to the trigger.
     */
    @Builder.Default
    private String project = GenerationContext.getInstance().getGlobalConfiguration()
            .getDefaultProject();

    /**
     * The listener of the Gerrit Trigger.
     * <p>
     * By default vRealize Automation Generator will use the one specified in global configuration.
     * In case the listener is specified explicitly, it will be associated to the trigger.
     */
    @Builder.Default
    private String listener = GenerationContext.getInstance().getCodestreamConfiguration()
            .getDefaultGerritListener();

    /**
     * Branch of the project which to be triggered with the Gerrit Trigger.
     * <p>
     * Defaults to the one specified in global configuration.
     * In case the branch is specified explicitly, it will be associated to the trigger. If the
     * branch in global configuration is null, vRealize Automation Generator will try to find the
     * system property with key 'BRANCH_NAME' with default value 'master'
     */
    @Builder.Default
    private String branch = Optional.ofNullable(
            GenerationContext.getInstance().getCodestreamConfiguration().getDefaultBranch())
            .orElse(System.getProperty("BRANCH_NAME", "master"));

    /**
     * List of patterns to include
     * <p>
     * See {@link com.vmware.devops.model.codestream.GerritTrigger.Pattern}
     */
    @Builder.Default
    private List<Pattern> inclusions = new ArrayList<>();

    /**
     * List of patterns to exclude
     * <p>
     * See {@link com.vmware.devops.model.codestream.GerritTrigger.Pattern}
     */
    @Builder.Default
    private List<Pattern> exclusions = new ArrayList<>();

    /**
     * Option for prioritizing the exclusion patterns.
     *
     * By default this option is false.
     */
    @Builder.Default
    private Boolean prioritizeExclusion = false;

    public com.vmware.devops.client.codestream.stubs.GerritTrigger initializeTrigger() {
        return com.vmware.devops.client.codestream.stubs.GerritTrigger.builder()
                .name(name)
                .project(project)
                .gerritProject(gerritProject)
                .branch(branch)
                .configurations(
                        configurations.stream().map(Configuration::initializeConfiguration).collect(
                                Collectors.toList()))
                .inclusions(inclusions.stream().map(Pattern::initializePattern)
                        .collect(Collectors.toList()))
                .exclusions(exclusions.stream().map(Pattern::initializePattern)
                        .collect(Collectors.toList()))
                .prioritizeExclusion(prioritizeExclusion)
                .enabled(enabled)
                .listener(listener)
                .build();
    }

    @Override
    public void generate() throws Exception {
        com.vmware.devops.client.codestream.stubs.GerritTrigger trigger = initializeTrigger();
        GenerationContext.getInstance().getEndpointConfiguration().getClient().getCodestream()
                .createOrUpdateGerritTrigger(trigger);
    }

    @Override
    public void populateData(com.vmware.devops.client.codestream.stubs.GerritTrigger gerritTrigger)
            throws Exception {
        name = gerritTrigger.getName();
        project = gerritTrigger.getProject();
        gerritProject = gerritTrigger.getGerritProject();
        branch = gerritTrigger.getBranch();

        configurations = gerritTrigger.getConfigurations().stream().map(conf ->
                Configuration.builder()
                        .pipeline(conf.getPipeline())
                        .input(conf.getInput())
                        .eventType(conf.getEventType())
                        .failureComment(conf.getFailureComment())
                        .successComment(conf.getSuccessComment()).build()
        ).collect(Collectors.toList());

        inclusions = gerritTrigger.getInclusions().stream().map(inclusion -> Pattern.builder()
                .type(inclusion.getType())
                .value(inclusion.getValue()).build()).collect(Collectors.toList());

        exclusions = gerritTrigger.getExclusions().stream().map(exclusion -> Pattern.builder()
                .type(exclusion.getType())
                .value(exclusion.getValue()).build()).collect(Collectors.toList());

        prioritizeExclusion = gerritTrigger.isPrioritizeExclusion();
        enabled = gerritTrigger.isEnabled();
        listener = gerritTrigger.getListener();

    }

    @Override
    public String getTemplatePath() {
        return "templates/codestream/gerritTriggerReverseGenerate.groovy.peb";
    }

    @Override
    public void dumpAll() {
        boolean failed = false;
        for (com.vmware.devops.client.codestream.stubs.GerritTrigger gerritTrigger : ReverseGenerationContext
                .getInstance().getVraExportedData().getGerritTriggers()) {
            try {
                dump(gerritTrigger, ReverseGenerationContext.getInstance()
                        .newOutputDirFile("600-" + gerritTrigger.getName() + "-gerrit-trigger.groovy"));

            } catch (Exception e) {
                failed = true;
                log.error("Failed to export gerrit trigger " + gerritTrigger.getName(), e);
            }
        }

        if (failed) {
            throw new RuntimeException("At least one gerrit trigger export failed.");
        }

    }
}
