import com.vmware.devops.client.cloudassembly.extensibility.stubs.Action.Runtime
import com.vmware.devops.model.cloudassembly.extensibility.Action

return Action.builder()
        .name("test")
        .contentPath("hello-world.zip")
        .entrypoint("entrypoint")
        .timeout(6)
        .memory(300)
        .runtime(Runtime.PYTHON)
        .shared(true)
        .project("test-project")
        .build()