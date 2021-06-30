/*
 * Copyright 2021-2021 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.vmware.devops.model.cloudassembly.infrastructure;

import java.util.Map;
import java.util.stream.Collectors;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.vmware.devops.GenerationContext;
import com.vmware.devops.IdCache;
import com.vmware.devops.ReverseGenerationContext;
import com.vmware.devops.client.cloudassembly.infrastructure.stubs.ImageName;
import com.vmware.devops.client.cloudassembly.infrastructure.stubs.Region;
import com.vmware.devops.model.GenerationEntity;
import com.vmware.devops.model.ReverseGenerationEntity;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@Slf4j
public class ImageMapping
        implements GenerationEntity, ReverseGenerationEntity<ImageName> {
    /**
     * Image mapping name
     */
    private String name;

    /**
     * Image mapping
     */
    private Map<String, String> imageMapping;

    public ImageName initializeImageName() {
        return ImageName.builder()
                .name(name)
                .imageMapping(
                        imageMapping.entrySet().stream().collect(Collectors.toMap(
                                e -> {
                                    try {
                                        String region = e.getKey();
                                        if (!region.contains("/")) {
                                            region = GenerationContext.getInstance()
                                                    .getCloudAssemblyConfiguration()
                                                    .getDefaultCloudAccount() + "/" + region;
                                        }

                                        return IdCache.REGION_LINK_CACHE.getId(region);
                                    } catch (Exception ex) {
                                        throw new RuntimeException(ex);
                                    }
                                },
                                e -> ImageName.ImageMapping.builder().image(e.getValue()).build()
                        ))
                )
                .build();
    }

    @Override
    public void generate() throws Exception {
        ImageName imageName = initializeImageName();
        GenerationContext.getInstance().getEndpointConfiguration().getClient()
                .getCloudAssembly().getInfrastructure().createOrUpdateImageName(imageName);
    }

    @Override
    public void populateData(ImageName imageName) throws Exception {
        name = imageName.getName();
        imageMapping = imageName.getImageMapping().entrySet()
                .stream()
                .collect(Collectors.toMap(
                        e -> {
                            Region region = ReverseGenerationContext
                                    .getInstance().getVraExportedData()
                                    .getRegions().stream()
                                    .filter(r -> r.getDocumentSelfLink().equals(e.getKey()))
                                    .findFirst()
                                    .get();
                            return region.getEndpoint().getName() + " / " + region.getRegionName();
                        },
                        e -> e.getValue().getImage()
                ));
    }

    @Override
    public String getTemplatePath() {
        return "templates/cloudassembly/infrastructure/imageMappingReverseGenerate.groovy.peb";
    }

    @Override
    public void dumpAll() {
        boolean failed = false;
        for (ImageName imageName : ReverseGenerationContext.getInstance().getVraExportedData()
                .getImageNames()) {
            try {
                dump(imageName, ReverseGenerationContext.getInstance()
                        .newOutputDirFile(imageName.getName() + "-image-mapping.groovy"));
            } catch (Exception e) {
                failed = true;
                log.error("Failed to export image mapping " + imageName.getName(), e);
            }
        }

        if (failed) {
            throw new RuntimeException("At least one image mapping export failed.");
        }
    }
}
