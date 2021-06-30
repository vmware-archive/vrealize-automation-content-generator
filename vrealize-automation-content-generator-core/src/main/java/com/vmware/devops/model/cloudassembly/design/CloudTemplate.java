/*
 * Copyright 2021-2021 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.vmware.devops.model.cloudassembly.design;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.vmware.devops.GenerationContext;
import com.vmware.devops.IdCache;
import com.vmware.devops.ReverseGenerationContext;
import com.vmware.devops.SerializationUtils;
import com.vmware.devops.Utils;
import com.vmware.devops.client.cloudassembly.design.stubs.Blueprint;
import com.vmware.devops.client.cloudassembly.extensibility.stubs.Subscription.RunnableType;
import com.vmware.devops.config.GlobalConfiguration;
import com.vmware.devops.model.ExtensibilityRunnable;
import com.vmware.devops.model.GenerationEntity;
import com.vmware.devops.model.ReverseGenerationEntity;
import com.vmware.devops.model.cloudassembly.design.CloudTemplate.BlueprintContent.CloudMachine;
import com.vmware.devops.model.cloudassembly.design.CloudTemplate.BlueprintContent.Input;
import com.vmware.devops.model.cloudassembly.design.CloudTemplate.BlueprintContent.Resource;
import com.vmware.devops.model.cloudassembly.design.CloudTemplate.BlueprintContent.StringInput;
import com.vmware.devops.model.cloudassembly.extensibility.Action;
import com.vmware.devops.model.cloudassembly.extensibility.Criteria.BlueprintNameCriteria;
import com.vmware.devops.model.cloudassembly.extensibility.Subscription;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Slf4j
public class CloudTemplate
        implements GenerationEntity, ReverseGenerationEntity<Blueprint> {
    /**
     * Cloud template name
     */
    private String name;

    /**
     * Cloud template content, as object. This is serialized into YAML before sent to vRA
     * If you want to write the content as YAML instead, see {@link CloudTemplate#contentPath}
     * Only one of {@link CloudTemplate#contentPath} or {@link CloudTemplate#content} should be set
     */
    private BlueprintContent content;

    /**
     * Path to YAML file containing the content of the cloud template
     * It's recommended to use {@link CloudTemplate#content} where applicable instead.
     */
    private String contentPath;

    /**
     * Is Cloud Template available for all projects. Defaults to false.
     */
    private Boolean global;

    /**
     * Extensibility definition for this blueprint. Subscriptions defined here will
     * automatically have {@link BlueprintNameCriteria}. Subscription and runnable names
     * defined here can be omitted in favor of generated ones, based on the cloud template name
     */
    private Extensibility extensibility;

    /**
     * The project name the cloud template belongs to.
     * Defaults to {@link GlobalConfiguration#getDefaultProject()} in {@link GenerationContext}
     * singleton
     */
    private String project;

    public Blueprint initializeBlueprint()
            throws IOException, InterruptedException, URISyntaxException {
        processInPlaceInputs();

        String contentData;
        if (contentPath != null) {
            contentData = Utils.readFile(contentPath);
        } else {
            contentData = SerializationUtils.toYaml(content);
        }

        String projectId;
        if (project != null) {
            projectId = IdCache.PROJECT_ID_CACHE.getId(project);
        } else {
            projectId = GenerationContext.getInstance().getGlobalConfiguration().getProjectId();
        }

        return Blueprint.builder()
                .name(name)
                .content(contentData)
                .projectId(projectId)
                .requestScopeOrg(Optional.ofNullable(global).orElse(false))
                .build();
    }

    public void processInPlaceInputs() {
        if (content == null) {
            return;
        }

        Map<String, Input> inputs = new HashMap<>();

        for (Resource r : content.getResources().values()) {
            if (r instanceof CloudMachine) {
                CloudMachine cloudMachine = ((CloudMachine) r);
                for (Entry<String, Object> e : cloudMachine.properties.entrySet()) {
                    if (e.getValue() instanceof Input) {
                        String sanitizedKey = sanitizeInputKey(sanitizeInputKey(e.getKey()));
                        inputs.put(sanitizedKey, (Input) e.getValue());
                        cloudMachine.properties
                                .put(e.getKey(), String.format("${input.%s}", sanitizedKey));
                    }
                }
            }
        }

        inputs.putAll(content.inputs);
        content.inputs = inputs;
    }

    @Override
    public void generate() throws Exception {
        Blueprint blueprint = initializeBlueprint();
        blueprint = GenerationContext.getInstance().getEndpointConfiguration().getClient()
                .getCloudAssembly()
                .getDesign()
                .createOrUpdateBlueprint(blueprint);
        IdCache.BLUEPRINT_ID_CACHE.getNameToId().put(blueprint.getName(), blueprint.getId());

        if (extensibility != null) {
            expandExtensibilityContent(blueprint);
            extensibility.getContent().stream().filter(c -> c.runnable instanceof Action)
                    .forEach(c -> {
                        Action action = (Action) c.runnable;
                        try {
                            action.generate();
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    });

            expandSubscriptions(blueprint);
            extensibility.subscriptions.forEach(s -> {
                try {
                    s.generate();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }

    public void expandExtensibilityContent(Blueprint blueprint) {
        for (int i = 0; i < extensibility.getContent().size(); i++) {
            Subscription subscription = extensibility.getContent().get(i).getSubscription();
            if (extensibility.getContent().get(i).runnable instanceof Action) {
                Action action = (Action) extensibility.getContent().get(i).runnable;

                action.setName(Optional.ofNullable(action.getName())
                        .orElse(subscription.getEventTopic().name()));
                action.setName(
                        String.format("%s-%s", blueprint.getName(), action.getName()));
                subscription.setRunnableName(action.getName());
                subscription.setRunnableType(RunnableType.ACTION);
                extensibility.subscriptions.add(subscription);
            } else {
                throw new UnsupportedOperationException(
                        "In-place definition not implemented for " + extensibility.getContent()
                                .get(i).runnable.getClass());
            }
        }
    }

    public void expandSubscriptions(Blueprint blueprint) {
        for (int i = 0; i < extensibility.subscriptions.size(); i++) {
            Subscription s = extensibility.subscriptions.get(i);
            s.setName(Optional.ofNullable(s.getName())
                    .orElse(s.getEventTopic().name()));
            s.setName(String.format("%s-%s-%s", blueprint.getName(), s.getRunnableName(),
                    s.getName()));
            try {
                s.setCriteria(s.getCriteria()
                        .and(new BlueprintNameCriteria(blueprint.getName())));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    public String sanitizeInputKey(String key) {
        return key.replaceAll("[^a-zA-Z0-9]", "_");
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BlueprintContent {
        @Default
        private int formatVersion = 1;

        @Builder.Default
        @JsonDeserialize(contentUsing = InputDeserializer.class)
        private Map<String, Input> inputs = new HashMap<>();

        @JsonDeserialize(contentUsing = ResourceDeserializer.class)
        private Map<String, Resource> resources;

        @Data
        public static class StringInput extends Input {
            private static final String TYPE = "string";

            private String description;
            @JsonProperty("default")
            private String defaultValue;

            public StringInput() {
                super(TYPE);
            }

            @Builder
            public StringInput(String description, String defaultValue) {
                super(TYPE);
                this.description = description;
                this.defaultValue = defaultValue;
            }
        }

        @Data
        public static class CloudMachine extends Resource {
            private static final String TYPE = "Cloud.Machine";

            @Default
            private Map<String, Object> properties = new HashMap<>();

            public CloudMachine() {
                super(TYPE);
            }

            @Builder
            public CloudMachine(String image, String flavor, Map<String, Object> properties) {
                super(TYPE);
                this.properties = new HashMap<>();
                this.properties.put("image", image);
                this.properties.put("flavor", Optional.ofNullable(flavor)
                        .orElse(GenerationContext.getInstance().getCloudAssemblyConfiguration()
                                .getDefaultFlavor()));
                if (properties != null) {
                    this.properties.putAll(properties);
                }
            }
        }

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        public abstract static class Input {
            private String type;
        }

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        public abstract static class Resource {
            private String type;
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Extensibility {
        @Builder.Default
        private List<Subscription> subscriptions = new ArrayList<>();

        @Builder.Default
        private List<SubscriptionWithContent> content = Collections.emptyList();

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        @Builder
        public static class SubscriptionWithContent {
            private ExtensibilityRunnable runnable;
            private Subscription subscription;
        }
    }

    @Override
    public void populateData(Blueprint blueprint) {
        project = ReverseGenerationContext.getInstance().getVraExportedData().getProjects().stream()
                .filter(p -> p.getId().equals(blueprint.getProjectId())).findFirst().get()
                .getName();
        name = blueprint.getName();
        global = blueprint.isRequestScopeOrg();
        contentPath = blueprint.getName() + "-cloud-template-content.yaml";
    }

    @Override
    public void dump(Blueprint blueprint, File output) throws Exception {
        ReverseGenerationEntity.super.dump(blueprint, output);
        try (FileWriter fw = new FileWriter(
                new File(ReverseGenerationContext.getInstance().getOutputDir(), contentPath)
                        .getAbsolutePath())) {
            fw.write(blueprint.getContent());
        }
    }

    @Override
    public String getTemplatePath() {
        return "templates/cloudassembly/design/cloudTemplateReverseGenerate.groovy.template";
    }

    @Override
    public void dumpAll() {
        boolean failed = false;
        for (Blueprint b : ReverseGenerationContext.getInstance().getVraExportedData()
                .getBlueprints()) {
            try {
                dump(b, ReverseGenerationContext.getInstance()
                        .newOutputDirFile(b.getName() + "-cloud-template.groovy"));
            } catch (Exception e) {
                failed = true;
                log.error("Failed to export cloud template " + b.getName(), e);
            }
        }

        if (failed) {
            throw new RuntimeException("At least one cloud template export failed.");
        }
    }

    private static class InputDeserializer extends JsonDeserializer<Input> {
        @Override
        public Input deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            TreeNode treeNode = p.readValueAsTree();
            String type = SerializationUtils.getYamlMapper()
                    .treeToValue(treeNode.get("type"), String.class);
            switch (type) {
            case StringInput.TYPE:
                return SerializationUtils.getYamlMapper().treeToValue(treeNode, StringInput.class);
            default:
                throw new IllegalStateException("Unknown input type " + type);

            }
        }
    }

    private static class ResourceDeserializer extends JsonDeserializer<Resource> {
        @Override
        public Resource deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            TreeNode treeNode = p.readValueAsTree();
            String type = SerializationUtils.getYamlMapper()
                    .treeToValue(treeNode.get("type"), String.class);
            switch (type) {
            case CloudMachine.TYPE:
                return SerializationUtils.getYamlMapper().treeToValue(treeNode, CloudMachine.class);
            default:
                throw new IllegalStateException("Unknown input type " + type);

            }
        }
    }
}
