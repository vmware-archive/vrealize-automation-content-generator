import com.vmware.devops.model.cloudassembly.extensibility.Action

return Action.builder()
        .name("test")
        .contentPath("test-action-flow.yaml")
        .timeout(13)
        .shared(false)
        .project("test-project")
        .build()