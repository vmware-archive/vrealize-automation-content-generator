/*
 * Copyright 2021-2021 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.vmware.devops.model.codestream;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import com.vmware.devops.GenerationContext;
import com.vmware.devops.ReverseGenerationContext;
import com.vmware.devops.Utils;
import com.vmware.devops.client.codestream.stubs.Task.EndpointKeys;
import com.vmware.devops.client.codestream.stubs.Task.SshInput;
import com.vmware.devops.client.codestream.stubs.Task.SshInput.SshInputBuilder;
import com.vmware.devops.client.codestream.stubs.Task.Type;
import com.vmware.devops.client.codestream.stubs.Variable.VariableType;

/**
 * RestTask implements {@link com.vmware.devops.model.codestream.Task} and can be included in
 * CodeStream Stage. It represent CodeStream Task of type Rest.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SshTask implements Task, CodestreamTask, WithOutputs {

    /**
     * SshTaskAuthentication is the main entity that specify the authentication mechanism of SshTask
     */
    @Data
    public static class SshTaskAuthentication {
        /**
         * The username for connecting to the remote host
         */
        private String username;

        /**
         * The private key for connecting to the remote host
         */
        private String privateKey;

        /**
         * The pass phrase for the private key
         */
        private String passphrase;

        /**
         * The password for connecting to the remote host
         */
        private String password;

        /**
         * Constructor for username/password authentication
         * @param username
         *         Username to be used for the ssh connection
         * @param password
         *         Password to be used for the ssh connection
         */
        public SshTaskAuthentication(String username, String password) {
            this.username = username;
            this.password = password;
        }

        /**
         * Constructor for SSH key pair authentication
         * @param username
         *         Username to be used for the ssh connection
         * @param privateKey
         *         Private key to be used for the ssh connection
         * @param passphrase
         *         Passphrase to be used for the ssh connection
         */
        public SshTaskAuthentication(String username, String privateKey, String passphrase) {
            this.username = username;
            this.privateKey = privateKey;
            this.passphrase = passphrase;
        }

        public boolean isUsernamePassword() {
            return privateKey == null || privateKey.length() == 0;
        }
    }

    /**
     * The name of the task.
     */
    private String name;

    /**
     * The precondition of the task.
     * <p>
     * String representation of Boolean expression which will be used to decide whether or not the
     * task to be performed.
     */
    private String preCondition;

    /**
     * Working directory of the task.
     * <p>
     * Specify in which directory the script to be performed.
     */
    private String workingDirectory;

    /**
     * The host of the task.
     * <p>
     * Specify ip/hostname of the host which to be used.
     */
    private String host;

    /**
     * Script path which to be used to load the script from.
     */
    private String scriptPath;

    /**
     * List of arguments of the script
     */
    private List<String> arguments;

    /**
     * Map of environemnt variable which to be passed to the script execution context
     * <p>
     * Key-value pairs of strings
     */
    private Map<String, String> environmentVariables;

    /**
     * Authentication type.
     * <p>
     * See {@link com.vmware.devops.model.codestream.SshTask},
     * {@link com.vmware.devops.model.codestream.SshTask.SshTaskAuthentication}
     */
    private SshTaskAuthentication authentication;

    /**
     * The agent of the task.
     * <p>
     * This agent will be used to perform the poll requests. By default, the agent specified in
     * global CodeStream configuration will be used.
     */
    private String agent;

    /**
     * Boolean option which specify whether the pipeline will fail in case of task failure or will
     * continue.
     * <p>
     * By default this option is false which mean the Pipeline will fail in case the task fail
     * itself
     */
    private Boolean ignoreFailure;

    /**
     * List of the outputs of the task.
     */
    private List<Output> outputs;
    @Getter(AccessLevel.PACKAGE)
    @Setter(AccessLevel.PACKAGE)
    private String variablePrefix;

    @Builder
    public SshTask(String name, String preCondition, String workingDirectory, String host,
            String scriptPath, List<String> arguments,
            Map<String, String> environmentVariables,
            SshTaskAuthentication authentication, String agent, Boolean ignoreFailure,
            List<Output> outputs) {
        this.name = name;
        this.preCondition = preCondition;
        this.workingDirectory = workingDirectory;
        this.host = host;
        this.scriptPath = scriptPath;
        this.arguments = Optional.ofNullable(arguments).orElse(Collections.emptyList());
        this.environmentVariables = environmentVariables;
        this.authentication = authentication;
        this.agent = Optional.ofNullable(agent)
                .orElse(GenerationContext.getInstance().getCodestreamConfiguration()
                        .getDefaultAgentEndpoint());
        this.ignoreFailure = Optional.ofNullable(ignoreFailure).orElse(false);
        this.outputs = Optional.ofNullable(outputs).orElse(Collections.emptyList());
    }

    @Override
    public com.vmware.devops.client.codestream.stubs.Task initializeTask() {
        String script;
        try {
            script = Utils.readFile(scriptPath);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        SshInputBuilder inputBuilder = SshInput.builder()
                .workingDirectory(workingDirectory)
                .host(host)
                .script(script)
                .arguments(arguments)
                .environmentVariables(environmentVariables);

        if (authentication != null) {
            if (authentication.isUsernamePassword()) {
                inputBuilder.username(authentication.username)
                        .password(Variable.variableReference(getPasswordVariableName()));
            } else {
                inputBuilder.username(authentication.username)
                        .privatekey(authentication.privateKey)
                        .passphrase(Variable.variableReference(getPassPhraseVariableName()));
            }
        }

        return com.vmware.devops.client.codestream.stubs.Task.builder()
                .type(getType())
                .preCondition(preCondition)
                .ignoreFailure(ignoreFailure)
                .input(inputBuilder.build())
                .endpoints(Map.of(EndpointKeys.AGENT, agent))
                .build();
    }

    @Override
    public void populateData(Pipeline parent, String name,
            com.vmware.devops.client.codestream.stubs.Task task) {
        SshInput input = (SshInput) task.getInput();
        SshTaskAuthentication authentication;
        if (input.getPrivatekey() != null) {
            authentication = new SshTaskAuthentication(input.getUsername(),
                    input.getPrivatekey(), input.getPassphrase());
        } else {
            authentication = new SshTaskAuthentication(input.getUsername(),
                    input.getPassword());
        }

        this.name = name;
        this.agent = task.getEndpoints().get(EndpointKeys.AGENT);
        this.ignoreFailure = task.isIgnoreFailure();
        this.preCondition = task.getPreCondition();
        this.authentication = authentication;
        this.arguments = input.getArguments();
        this.environmentVariables = input.getEnvironmentVariables();
        this.host = input.getHost();
        this.scriptPath = String.format("%s-%s-task-script.sh", parent.getName(), name);
        this.workingDirectory = input.getWorkingDirectory();
    }

    @Override
    public void dump(Pipeline parent, String name,
            com.vmware.devops.client.codestream.stubs.Task task) throws IOException {
        SshInput input = (SshInput) task.getInput();
        Utils.writeFile(ReverseGenerationContext.getInstance().newOutputDirFile(scriptPath),
                input.getScript().getBytes());
    }

    @Override
    public Type getType() {
        return Type.SSH;
    }

    public Variable initializePasswordVariable() {
        return Variable.builder()
                .name(getPasswordVariableName())
                .type(VariableType.SECRET)
                .value(authentication.password)
                .build();
    }

    public Variable initializePassPhaseVariable() {
        return Variable.builder()
                .name(getPassPhraseVariableName())
                .type(VariableType.SECRET)
                .value(authentication.passphrase)
                .build();
    }

    public String getPasswordVariableName() {
        return String.format("%s-%s-ssh-task-password", variablePrefix, name);
    }

    public String getPassPhraseVariableName() {
        return String.format("%s-%s-ssh-task-pass-phrase", variablePrefix, name);
    }
}
