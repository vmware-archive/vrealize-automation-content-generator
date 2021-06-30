/*
 * Copyright 2021-2021 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.vmware.devops.cli;

import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import picocli.CommandLine;
import picocli.CommandLine.Option;

import com.vmware.devops.GenerationContext;
import com.vmware.devops.SpecProcessor;
import com.vmware.devops.config.EndpointConfiguration.AuthenticationDetails;
import com.vmware.devops.model.GenerationEntity;

@Slf4j
public class Generate implements Callable<Integer> {
    private static final int MAX_ORDER = 999;
    private static final Pattern ORDER_PATTERN = Pattern.compile("^[0-9]{1,3}(?=-)");

    @Option(names = { "--path" },
            description = "Path to file or directory containing the specs",
            defaultValue = "src/main/resources"
    )
    private File path;

    @Option(names = { "--filter" },
            description = "Regex to filter the specs from other files",
            defaultValue = ".*\\.groovy"
    )
    private String filter;

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
    public Integer call() {
        List<List<File>> specs = findSpecs(path, null);

        GenerationContext.getInstance().getEndpointConfiguration().setInstance(instance);
        GenerationContext.getInstance().getEndpointConfiguration().setLoginInstance(loginInstance);
        if (refreshToken != null) {
            GenerationContext.getInstance().getEndpointConfiguration()
                    .setAuthenticationDetails(new AuthenticationDetails(refreshToken));
        } else if (username != null) {
            GenerationContext.getInstance().getEndpointConfiguration()
                    .setAuthenticationDetails(new AuthenticationDetails(username, password));
        } else {
            throw new IllegalArgumentException("Refresh token or username/password is required");
        }

        AtomicInteger exitCode = new AtomicInteger(0);
        ForkJoinTask<?> task = GenerationContext.getInstance().getThreadPool().submit(() ->
                specs.stream().forEach(layer -> layer.parallelStream().flatMap(f -> {
                    log.info("Processing " + f);
                    String content;
                    try {
                        content = IOUtils.toString(new FileInputStream(f), StandardCharsets.UTF_8);
                        SpecProcessor specProcessor = new SpecProcessor();
                        Object o = specProcessor.process(content);
                        if (o instanceof List) {
                            return ((List<GenerationEntity>) specProcessor
                                    .process(content)).stream();
                        } else if (o instanceof GenerationEntity) {
                            return Stream.of((GenerationEntity) o);
                        }
                    } catch (Exception e) {
                        log.error("Failed to process file: " + f, e);
                        exitCode.set(1);
                        return Stream.empty();
                    }

                    log.info("Nothing to generate for " + f);
                    return Stream.empty();
                }).forEach(o -> {
                    try {
                        o.generate();
                    } catch (Exception e) {
                        log.error("Failed to generate object: " + o, e);
                        exitCode.set(1);
                    }
                }))
        );
        task.join();

        return exitCode.get();
    }

    public static void main(String... args) {
        int exitCode = new CommandLine(new Generate()).execute(args);
        System.exit(exitCode);
    }

    public List<List<File>> findSpecs(File root, List<List<File>> result) {
        if (result == null) {
            result = new ArrayList<>();

            for (int i = 0; i <= MAX_ORDER; i++) {
                result.add(new ArrayList<>());
            }
        }

        if (root == null) {
            return result;
        }

        if (root.isFile() && root.getPath().matches(filter)) {
            Matcher matcher = ORDER_PATTERN.matcher(root.getName());
            int index = 500;
            if (matcher.find()) {
                index = Integer.parseInt(matcher.group());
            }

            result.get(index).add(root);
        } else if (root.isDirectory()) {
            File[] files = root.listFiles();
            if (files != null) {
                for (File f : files) {
                    findSpecs(f, result);
                }
            }
        }

        return result;
    }
}
