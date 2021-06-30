/*
 * Copyright 2021-2021 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.vmware.devops;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinPool.ForkJoinWorkerThreadFactory;
import java.util.concurrent.ForkJoinWorkerThread;

import lombok.Getter;

import com.vmware.devops.config.CloudAssemblyConfiguration;
import com.vmware.devops.config.CodestreamConfiguration;
import com.vmware.devops.config.EndpointConfiguration;
import com.vmware.devops.config.ExtensibilityConfiguration;
import com.vmware.devops.config.GlobalConfiguration;

public class GenerationContext {
    private static GenerationContext generationContext;

    @Getter
    private EndpointConfiguration endpointConfiguration = new EndpointConfiguration();

    @Getter
    private GlobalConfiguration globalConfiguration = new GlobalConfiguration();

    @Getter
    private CodestreamConfiguration codestreamConfiguration = new CodestreamConfiguration();

    @Getter
    private CloudAssemblyConfiguration cloudAssemblyConfiguration = new CloudAssemblyConfiguration();

    @Getter
    private ExtensibilityConfiguration extensibilityConfiguration = new ExtensibilityConfiguration();

    @Getter
    private ForkJoinPool threadPool = new ForkJoinPool(Runtime.getRuntime().availableProcessors(),
            new CustomForkJoinWorkerThreadFactory(), null, false);

    private GenerationContext() {
    }

    public static synchronized GenerationContext getInstance() {
        if (generationContext == null) {
            generationContext = new GenerationContext();
        }

        return generationContext;
    }

    // Ensure empty context
    public static void reset() {
        generationContext = null;
    }

    // This fixes classloaded issues when using parallel streaming api
    private static class CustomForkJoinWorkerThreadFactory implements ForkJoinWorkerThreadFactory {

        @Override
        public final ForkJoinWorkerThread newThread(ForkJoinPool pool) {
            return new MyForkJoinWorkerThread(pool);
        }

        private static class MyForkJoinWorkerThread extends ForkJoinWorkerThread {

            private MyForkJoinWorkerThread(final ForkJoinPool pool) {
                super(pool);
                setContextClassLoader(Thread.currentThread().getContextClassLoader());
            }
        }
    }
}
