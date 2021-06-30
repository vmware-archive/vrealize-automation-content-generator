import com.vmware.devops.model.cloudassembly.design.CloudTemplate

return CloudTemplate.builder()
        .name("test-1")
        .project("project")
        .global(true)
        .contentPath("test-1-cloud-template-content.yaml")
        .build()