/*
 * Copyright 2021-2021 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.vmware.devops.cli;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import com.vmware.devops.ReverseGenerationContext;
import com.vmware.devops.config.EndpointConfiguration.AuthenticationDetails;
import com.vmware.devops.model.ReverseGenerationEntity;
import com.vmware.devops.model.cloudassembly.design.CloudTemplate;
import com.vmware.devops.model.cloudassembly.extensibility.Action;
import com.vmware.devops.model.cloudassembly.extensibility.Subscription;
import com.vmware.devops.model.cloudassembly.infrastructure.AwsCloudAccount;
import com.vmware.devops.model.cloudassembly.infrastructure.AzureCloudAccount;
import com.vmware.devops.model.cloudassembly.infrastructure.FlavorMapping;
import com.vmware.devops.model.cloudassembly.infrastructure.ImageMapping;
import com.vmware.devops.model.cloudassembly.infrastructure.NimbusCloudAccount;
import com.vmware.devops.model.cloudassembly.infrastructure.Project;
import com.vmware.devops.model.cloudassembly.infrastructure.VsphereCloudAccount;
import com.vmware.devops.model.codestream.AgentEndpoint;
import com.vmware.devops.model.codestream.EmailEndpoint;
import com.vmware.devops.model.codestream.GerritEndpoint;
import com.vmware.devops.model.codestream.GerritTrigger;
import com.vmware.devops.model.codestream.JenkinsEndpoint;
import com.vmware.devops.model.codestream.JiraEndpoint;
import com.vmware.devops.model.codestream.Pipeline;
import com.vmware.devops.model.codestream.Variable;

@Command(name = "reverseGenerate", mixinStandardHelpOptions = true,
        description = "Reverse generate the vRA models to groovy code.")
@Slf4j
public class ReverseGenerate implements Callable<Integer> {
    private static List<Class<? extends ReverseGenerationEntity>> reverseGenerationEntities = List
            .of(
                    //Cloud Assembly
                    CloudTemplate.class,
                    Project.class,
                    Action.class,
                    Subscription.class,
                    FlavorMapping.class,
                    ImageMapping.class,
                    NimbusCloudAccount.class,
                    VsphereCloudAccount.class,
                    AwsCloudAccount.class,
                    AzureCloudAccount.class,
                    // Codestream
                    Pipeline.class,
                    Variable.class,
                    AgentEndpoint.class,
                    JenkinsEndpoint.class,
                    JiraEndpoint.class,
                    GerritEndpoint.class,
                    EmailEndpoint.class,
                    GerritTrigger.class
            );

    @Option(names = { "-o", "--output-path" },
            description = "Path to directory where to generate groovy files",
            defaultValue = "src/main/resources"
    )
    private File output;

    @Option(names = { "-i", "--instance" }, paramLabel = "INSTANCE",
            description = "On-prem or cloud instance",
            defaultValue = "https://api.mgmt.cloud.vmware.com")
    private String instance;

    @Option(names = { "-l", "--login-instance" }, paramLabel = "INSTANCE",
            description = "Csp or VIDM instance",
            defaultValue = "https://console.cloud.vmware.com")
    private String loginInstance;

    @Option(names = { "-t", "--refresh-token" }, paramLabel = "TOKEN",
            description = "Csp or VIDM instance",
            required = false)
    private String refreshToken;

    @Option(names = { "-u", "--username" }, paramLabel = "USERNAME",
            description = "Username when using u/p for login",
            required = false)
    private String username;

    @Option(names = { "-p", "--password" }, paramLabel = "PASSWORD",
            description = "Password when using u/p for login",
            required = false)
    private String password;

    @Override
    public Integer call() throws InterruptedException, IOException, URISyntaxException {
        ReverseGenerationContext.getInstance().getEndpointConfiguration().setInstance(instance);
        ReverseGenerationContext.getInstance().getEndpointConfiguration()
                .setLoginInstance(loginInstance);
        if (refreshToken != null) {
            ReverseGenerationContext.getInstance().getEndpointConfiguration()
                    .setAuthenticationDetails(new AuthenticationDetails(refreshToken));
        } else if (username != null) {
            ReverseGenerationContext.getInstance().getEndpointConfiguration()
                    .setAuthenticationDetails(new AuthenticationDetails(username, password));
        } else {
            throw new IllegalArgumentException("Refresh token or username/password is required");
        }
        ReverseGenerationContext.getInstance().setOutputDir(output.getAbsolutePath());
        ReverseGenerationContext.getInstance().initializeVraExportedData();

        List<Class<? extends ReverseGenerationEntity>> allReverseGenerationEntities = new ArrayList<>(
                reverseGenerationEntities);
        allReverseGenerationEntities.addAll(getAdditionalReverseGenerationEntities());

        int exitCode = 0;
        for (Class c : allReverseGenerationEntities) {
            try {
                log.info("Processing " + c.getName());
                ReverseGenerationEntity entity = (ReverseGenerationEntity) c.getConstructor()
                        .newInstance();
                entity.dumpAll();
            } catch (Exception e) {
                exitCode = 1;
                log.error("Error while reverse generating model class " + c, e);
            }
        }

        return exitCode;
    }

    public static void main(String... args) {
        int exitCode = new CommandLine(new ReverseGenerate()).execute(args);
        System.exit(exitCode);
    }

    protected List<Class<? extends ReverseGenerationEntity>> getAdditionalReverseGenerationEntities() {
        return Collections.emptyList();
    }
}


