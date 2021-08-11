/*
 * Copyright 2021-2021 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.vmware.devops.model.cloudassembly.extensibility;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;

import com.vmware.devops.GenerationContext;
import com.vmware.devops.IdCache;
import com.vmware.devops.ReverseGenerationContext;
import com.vmware.devops.SerializationUtils;
import com.vmware.devops.Utils;
import com.vmware.devops.client.cloudassembly.extensibility.stubs.Action.Runtime;
import com.vmware.devops.client.cloudassembly.extensibility.stubs.Action.ScriptSource;
import com.vmware.devops.client.cloudassembly.extensibility.stubs.Action.Type;
import com.vmware.devops.client.cloudassembly.extensibility.stubs.Subscription.RunnableType;
import com.vmware.devops.config.GlobalConfiguration;
import com.vmware.devops.model.ExtensibilityRunnable;
import com.vmware.devops.model.GenerationEntity;
import com.vmware.devops.model.ReverseGenerationEntity;
import com.vmware.devops.model.cloudassembly.extensibility.Action.Flow.ActionStep;
import com.vmware.devops.model.cloudassembly.extensibility.Action.Flow.Step;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@Slf4j
public class Action implements GenerationEntity, ExtensibilityRunnable,
        ReverseGenerationEntity<com.vmware.devops.client.cloudassembly.extensibility.stubs.Action> {
    public static final String DEPENDENCIES_DELIMITER = "\n";
    public static final int DEFAULT_TIMEOUT = 600;
    public static final int DEFAULT_MEMORY = 300;

    /**
     * Action runtime. Defaults to {@link Runtime#PYTHON}
     */
    private Runtime runtime;

    /**
     * Path to the content of the action. This can be
     * - text file when the content is plain script
     * - zip archive when the content is a package
     * - yaml file when the action is a flow.
     * <p>
     * For flows, it's recommended to use {@link Action#flow} instead
     */
    private String contentPath;

    /**
     * Action entrypoint
     */
    private String entrypoint;

    /**
     * The project name the action belongs to.
     * Defaults to {@link GlobalConfiguration#getDefaultProject()} in {@link GenerationContext}
     * singleton
     */
    private String project;

    /**
     * Action name
     */
    private String name;

    /**
     * Is action available for other projects
     */
    private boolean shared;

    /**
     * Action inputs
     */
    private Map<String, Object> inputs;

    /**
     * Action timeout in seconds. Defaults to {@link Action#DEFAULT_TIMEOUT}
     */
    private Integer timeout;

    /**
     * Memory in MB.  Defaults to {@link Action#DEFAULT_MEMORY}
     */
    private Integer memory;

    /**
     * Flow as object. This is serialized to YAML before send to vRA.
     * If you want to provide YAML manually, use {@link Action#contentPath }
     */
    private Flow flow;

    /**
     * Action dependencies
     */
    @Builder.Default
    private List<Dependency> dependencies = Collections.emptyList();

    /**
     * Action subscriptions
     */
    @Builder.Default
    private List<Subscription> subscriptions = Collections.emptyList();

    /**
     * There are likely corner cases here, expand on demand
     */
    public List<Action> expandFlow() {
        if (flow == null) {
            return Collections.emptyList();
        }
        List<Action> result = new ArrayList<>();

        List<String> stepOrder = new ArrayList<>();
        for (Entry<String, Step> stepEntry : flow.getSteps().entrySet()) {
            stepOrder.add(stepEntry.getKey());
            if (stepEntry.getValue() instanceof ActionStep &&
                    ((ActionStep) stepEntry.getValue()).action instanceof Action) {
                Action childAction = (Action) ((ActionStep) stepEntry.getValue()).action;
                childAction.name = Optional.ofNullable(childAction.name)
                        .orElse(name + "-" + stepEntry.getKey());
                result.add(childAction);
                ((ActionStep) stepEntry.getValue()).setAction(childAction.name);
            }
        }

        LinkedHashMap<String, Step> steps = new LinkedHashMap<>();
        // I may have to create a custom object for start and end at some point
        ActionStep startStep = new ActionStep();
        startStep.setNext(stepOrder.get(0));
        steps.put(Flow.FLOW_START, startStep);

        int index = -1;
        for (Entry<String, Step> stepEntry : flow.getSteps().entrySet()) {
            index++;
            if (stepEntry.getValue() instanceof ActionStep) {
                if (index + 1 < flow.getSteps().size()) {
                    ((ActionStep) stepEntry.getValue()).setNext(stepOrder.get(index + 1));
                } else {
                    ((ActionStep) stepEntry.getValue()).setNext(Flow.FLOW_END);
                }
            }
            steps.put(stepEntry.getKey(), stepEntry.getValue());
        }

        flow.steps = steps;

        return result;
    }

    public com.vmware.devops.client.cloudassembly.extensibility.stubs.Action initializeAction()
            throws IOException, URISyntaxException, InterruptedException {
        String compressedContent = null;
        String contentResourceName = null;
        String source = null;
        ScriptSource scriptSource = null;
        Type actionType;
        if (flow == null && !contentPath.endsWith(".yaml")) {
            actionType = Type.SCRIPT;
            runtime = Optional.ofNullable(runtime)
                    .orElse(GenerationContext.getInstance().getExtensibilityConfiguration()
                            .getDefaultActionRuntime());
            if (contentPath.endsWith(".zip")) {
                // No classpath option here, don't submit binaries under SCM
                File file = new File(contentPath);
                byte[] data = IOUtils.toByteArray(new FileInputStream(file));
                compressedContent = Base64.getEncoder().encodeToString(data);
                contentResourceName = file.getName();
                scriptSource = ScriptSource.PACKAGE;
            } else {
                source = Utils.readFile(contentPath);
                scriptSource = ScriptSource.SCRIPT;
            }
        } else {
            actionType = Type.FLOW;
            if (contentPath != null) {
                source = Utils.readFile(contentPath);
            } else {
                source = SerializationUtils.toYaml(flow);
            }
        }

        return com.vmware.devops.client.cloudassembly.extensibility.stubs.Action.builder()
                .runtime(runtime)
                .source(source)
                .entrypoint(entrypoint)
                .actionType(actionType)
                .name(name)
                .scriptSource(scriptSource)
                .compressedContent(compressedContent)
                .contentResourceName(contentResourceName)
                .shared(shared)
                .inputs(inputs)
                .projectId(IdCache.PROJECT_ID_CACHE.getId(Optional.ofNullable(project)
                        .orElse(GenerationContext.getInstance().getGlobalConfiguration()
                                .getDefaultProject())))
                .dependencies(dependencies.stream().map(Dependency::toString)
                        .collect(Collectors.joining(DEPENDENCIES_DELIMITER)))
                .timeoutSeconds(Optional.ofNullable(timeout).orElse(DEFAULT_TIMEOUT))
                .memoryInMB(Optional.ofNullable(memory).orElse(DEFAULT_MEMORY))
                .build();
    }

    @Override
    public void generate() throws Exception {
        List<Action> inlineActions = expandFlow();
        for (Action a : inlineActions) {
            a.generate();
        }

        com.vmware.devops.client.cloudassembly.extensibility.stubs.Action action = initializeAction();
        action = GenerationContext.getInstance()
                .getEndpointConfiguration().getClient().getCloudAssembly().getExtensibility()
                .createOrUpdateAction(action);
        IdCache.ACTION_ID_CACHE.getNameToId().put(action.getName(), action.getId());

        expandSubscriptions(action);
        subscriptions.forEach(s -> {
            try {
                s.generate();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    public void expandSubscriptions(
            com.vmware.devops.client.cloudassembly.extensibility.stubs.Action action) {
        for (Subscription s : subscriptions) {
            s.setName(Optional.ofNullable(s.getName())
                    .orElse(s.getEventTopic().name()));
            s.setName(action.getName() + "-" + s.getName());
            s.setRunnableType(RunnableType.ACTION);
            s.setRunnableName(action.getName());
        }
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Dependency {
        private String name;
        private String version;

        /**
         * Operation. Defaults to {@link Operation#EQUALS}
         */
        @Builder.Default
        private Operation operation = Operation.EQUALS;

        @Override
        public String toString() {
            return name + operation.value + version;
        }

        @AllArgsConstructor
        public enum Operation {
            GREATER_OR_EQUALS(">="),
            EQUALS("==");

            @Getter
            private String value;
        }

        public static Dependency parse(String s) {
            Operation operation;
            if (s.contains(Operation.EQUALS.value)) {
                operation = Operation.EQUALS;
            } else if (s.contains(Operation.GREATER_OR_EQUALS.value)) {
                operation = Operation.GREATER_OR_EQUALS;
            } else {
                throw new UnsupportedOperationException(
                        "Unknown operation when parsing action dependency: " + s);
            }

            String[] splitted = s.split(operation.value);

            return Dependency.builder()
                    .operation(operation)
                    .name(splitted[0].trim())
                    .version(splitted[1].trim())
                    .build();
        }
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Flow {
        private static final String FLOW_START = "flow_start";
        private static final String FLOW_END = "flow_end";

        @JsonProperty("flow")
        private LinkedHashMap<String, Step> steps;

        @Builder.Default
        private int version = 1;

        @Data
        @AllArgsConstructor
        @NoArgsConstructor
        public static class ActionStep implements Step {
            private Object action;
            private String next;

            @Builder
            public ActionStep(Object action) {
                this.action = action;
            }
        }

        @Data
        @Builder
        @AllArgsConstructor
        @NoArgsConstructor
        public static class SwithcStep implements Step {
            @JsonProperty("switch")
            private Map<String, String> conditions;
        }

        public interface Step {
        }
    }

    @Override
    public void populateData(
            com.vmware.devops.client.cloudassembly.extensibility.stubs.Action action) {
        project = ReverseGenerationContext.getInstance().getVraExportedData().getProjects()
                .stream().filter(p -> p.getId().equals(action.getProjectId())).findFirst().get()
                .getName();

        runtime = action.getRuntime();
        entrypoint = action.getEntrypoint();
        name = action.getName();
        shared = action.isShared();
        inputs = action.getInputs();
        timeout = action.getTimeoutSeconds();
        memory = action.getMemoryInMB();

        if (action.getDependencies() != null && !action.getDependencies()
                .trim()
                .equals("")) {
            dependencies = Arrays
                    .stream(action.getDependencies().split(DEPENDENCIES_DELIMITER))
                    .map(Dependency::parse).collect(
                            Collectors.toList());
        }

        if (action.getActionType().equals(Type.FLOW)) {
            contentPath = action.getName() + "-action-flow.yaml";
        } else if (action.getContentResourceName() != null) {
            contentPath = action.getContentResourceName();
        } else {
            switch (runtime) {
            case PYTHON:
                contentPath = action.getName() + "-action-script.py";
                break;
            default:
                throw new UnsupportedOperationException("Unknown runtime: " + runtime);
            }
        }
    }

    @Override
    public String getTemplatePath() {
        return "templates/cloudassembly/extensibility/actionReverseGenerate.groovy.peb";
    }

    @Override
    public void dump(com.vmware.devops.client.cloudassembly.extensibility.stubs.Action action,
            File output)
            throws Exception {
        ReverseGenerationEntity.super.dump(action, output);

        File contentOutputFile = ReverseGenerationContext.getInstance()
                .newOutputDirFile(contentPath);
        if (action.getContentResourceName() == null) {
            Utils.writeFile(contentOutputFile, action.getSource().getBytes());
            return;
        }

        File exportedAction = File.createTempFile(action.getName() + "-action", ".zip");
        ReverseGenerationContext.getInstance().getEndpointConfiguration().getClient()
                .getCloudAssembly().getExtensibility().exportAction(action, exportedAction);

        try (FileOutputStream fos = new FileOutputStream(contentOutputFile)) {
            try (ZipInputStream zis = new ZipInputStream(
                    new FileInputStream(exportedAction))) {
                ZipEntry entry = zis.getNextEntry();
                boolean found = false;
                while (entry != null && !found) {
                    if (entry.getName().endsWith("zip")) {
                        found = true;
                        byte[] buffer = new byte[1024];
                        int len;
                        while ((len = zis.read(buffer)) > 0) {
                            fos.write(buffer, 0, len);
                        }
                    }
                    entry = zis.getNextEntry();
                }
            }
        }
    }

    @Override
    public void dumpAll() {
        boolean failed = false;
        for (com.vmware.devops.client.cloudassembly.extensibility.stubs.Action action : ReverseGenerationContext
                .getInstance().getVraExportedData().getActions()) {
            try {
                dump(action, ReverseGenerationContext.getInstance()
                        .newOutputDirFile(action.getName() + "-action.groovy"));
            } catch (Exception e) {
                failed = true;
                log.error("Failed to export action " + action.getName(), e);
            }
        }

        if (failed) {
            throw new RuntimeException("At least one action export failed.");
        }
    }
}
