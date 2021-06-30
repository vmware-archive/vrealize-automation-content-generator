
# vrealize-automation-content-generator

## Overview

vRealize Automation Content Generator is a tool that allows you to create, edit and maintain vRealize Automation content as a code.

## Supported entities

Currently you can generate:

* Cloud Assembly
    - Cloud Templates
    - Cloud Accounts
        - vSphere
    - Actions
    - Subscriptions
    - Flavor Mapping
    - Image Mapping
    - Projects
* Codestream
    - Endpoints
        - Email
        - Jenkins
        - Jira
        - Agent
        - Gerrit
    - Pipelines
    - Triggers
        - Gerrit
    - Listeners
        - Gerrit
    - Variables

The list will be expanded on demand.

## Format

The content is written in Groovy and it looks like this:

```
import com.vmware.devops.model.codestream.Input
import com.vmware.devops.model.codestream.JenkinsTask
import com.vmware.devops.model.codestream.Pipeline
import com.vmware.devops.model.codestream.Stage

return Pipeline.builder()
        .name("sample-pipeline")
        .stages([
                Stage.builder()
                        .name("Stage-1")
                        .tasks([
                                JenkinsTask.builder()
                                        .name("Task-1")
                                        .job("job-name")
                                        .inputs([
                                                new Input("hello", "world")
                                        ])
                                        .build()
                        ])
                        .build()
        ])
        .build()
```

The format is:

* Easy to write - you can create or edit entities from code without going through the vRealize Automation UI
* Easy to read - changes are easy to share and can go through peer review
* Robust - hard to make mistakes while typing the code, and if you do the IDE is going to detect that
* Generic - all entities follow the same format conventions so the written code is coherent
* Reusable - common sections can be extracted and reused easily
* Extensible - custom generation entities can be defined
* Supported by modern IDE - Groovy is a standard language that is supported by all modern IDE so you get all the benefits from that fact

## Try it out

### Prerequisites

* JDK 11+
* Maven 3.5.0+
* (Optional) Docker 19.03+

### Build

`mvn clean install -DskipITs`

Optionally, you can build a container with the core deliverables after the above:

```
cd vrealize-automation-content-generator-core/
docker build --tag vrealize-automation-content-generator .
``` 

We'll be working on uploading ready-to-use deliverables soon.

## Documentation
Examples and detailed documentation in the [wiki](https://github.com/vmware-samples/vrealize-automation-content-generator/wiki)

## Contributing

The vrealize-automation-content-generator project team welcomes contributions from the community. Before you start working with vrealize-automation-content-generator, please
read our [Developer Certificate of Origin](https://cla.vmware.com/dco). All contributions to this repository must be
signed as described on that page. Your signature certifies that you wrote the patch or have the right to pass it on
as an open-source patch. For more detailed information, refer to [CONTRIBUTING.md](CONTRIBUTING.md).

## License
