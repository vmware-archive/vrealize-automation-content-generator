/*
 * Copyright 2021-2021 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.vmware.devops.model.codestream;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Input entity represent inputs of different objects in CodeStream like Pipeline and Task. This
 * entity is simple object of key-value pair.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Input {
    /**
     * GerritTriggerInput entity specify possible Gerrit inputs provided by Gerrit trigger event
     */
    public static class GerritTriggerInput {
        private GerritTriggerInput() {
        }

        public static final Input gerritBranch() {
            return new Input("GERRIT_BRANCH", "", true);
        }

        public static final Input gerritChangeCommitMessage() {
            return new Input(
                    "GERRIT_CHANGE_COMMIT_MESSAGE", "", true);
        }

        public static final Input gerritChangeId() {
            return new Input("GERRIT_CHANGE_ID", "", true);
        }

        public static final Input gerritChangeNumber() {
            return new Input("GERRIT_CHANGE_NUMBER", "",
                    true);
        }

        public static final Input gerritChangeOwnerEmail() {
            return new Input("GERRIT_CHANGE_OWNER_EMAIL",
                    "", true);
        }

        public static final Input gerritChangeOwnerName() {
            return new Input("GERRIT_CHANGE_OWNER_NAME",
                    "", true);
        }

        public static final Input gerritChangeOwnerUsername() {
            return new Input(
                    "GERRIT_CHANGE_OWNER_USERNAME", "", true);
        }

        public static final Input gerritChangeSubject() {
            return new Input("GERRIT_CHANGE_SUBJECT", "",
                    true);
        }

        public static final Input gerritChangeUrl() {
            return new Input("GERRIT_CHANGE_URL", "", true);
        }

        public static final Input gerritEventAccountEmail() {
            return new Input(
                    "GERRIT_EVENT_ACCOUNT_EMAIL", "", true);
        }

        public static final Input gerritEventAccountName() {
            return new Input("GERRIT_EVENT_ACCOUNT_NAME",
                    "", true);
        }

        public static final Input gerritEventAccountUsername() {
            return new Input("GERRIT_EVENT_ACCOUNT_USERNAME",
                    "", true);
        }

        public static final Input gerritEventCreatedOn() {
            return new Input("GERRIT_EVENT_CREATED_ON",
                    "", true);
        }

        public static final Input gerritEventType() {
            return new Input("GERRIT_EVENT_TYPE", "", true);
        }

        public static final Input gerritPatchsetNumber() {
            return new Input("GERRIT_PATCHSET_NUMBER", "",
                    true);
        }

        public static final Input gerritPatchsetRevision() {
            return new Input("GERRIT_PATCHSET_REVISION",
                    "", true);
        }

        public static final Input gerritPatchsetUploader() {
            return new Input("GERRIT_PATCHSET_UPLOADER",
                    "", true);
        }

        public static final Input gerritPatchsetUploaderEmail() {
            return new Input(
                    "GERRIT_PATCHSET_UPLOADER_EMAIL", "", true);
        }

        public static final Input gerritPatchsetUploaderName() {
            return new Input(
                    "GERRIT_PATCHSET_UPLOADER_NAME", "", true);
        }

        public static final Input gerritProject() {
            return new Input("GERRIT_PROJECT", "", true);
        }

        public static final Input gerritRefspec() {
            return new Input("GERRIT_REFSPEC", "", true);
        }

        public static final Input gerritUrl() {
            return new Input("GERRIT_URL", "", true);
        }
    }

    public static class GitLabTriggerInput {

        private GitLabTriggerInput() {

        }

        public static final Input gitBranch() {
            return new Input("GIT_BRANCH_NAME", "", true);
        }

        public static final Input gitCommitId() {
            return new Input("GIT_COMMIT_ID", "", true);
        }

        public static final Input gitChangeSubject() {
            return new Input("GIT_CHANGE_SUBJECT", "", true);
        }

        public static final Input gitEventOwnerName() {
            return new Input("GIT_EVENT_OWNER_NAME", "", true);
        }

        public static final Input gitEventDescription() {
            return new Input("GIT_EVENT_DESCRIPTION", "", true);
        }

        public static final Input gitEventTimestamp() {
            return new Input("GIT_EVENT_TIMESTAMP", "", true);
        }

        public static final Input gitRepoName() {
            return new Input("GIT_REPO_NAME", "", true);
        }

        public static final Input gitServerUrl() {
            return new Input("GIT_SERVER_URL", "", true);
        }
    }

    /**
     * The key of the input.
     */
    private String key;

    /**
     * The value of the input.
     */
    private Object value;

    /**
     * Boolean flag which specify whether the input is global or not.
     * If set to true, this input will be added to the top level object as input parameter (ex.
     * Pipeline)
     */
    private boolean global;

    public Input(String key, Object value) {
        this.key = key;
        this.value = value;
    }

    public static String globalInputReference(String key) {
        return String.format("${input.%s}", key);
    }

    public static Map<String, String> inputListToMap(List<Input> inputs) {
        return inputs.stream().collect(Collectors
                .toMap(Input::getKey, i -> i.getValue().toString(), (first, second) -> second));
    }

    /**
     * Method for generating the reference of the input for further usage.
     * @return Reference to the input which can be used
     */
    public String globalInputReference() {
        return globalInputReference(this.key);
    }
}
