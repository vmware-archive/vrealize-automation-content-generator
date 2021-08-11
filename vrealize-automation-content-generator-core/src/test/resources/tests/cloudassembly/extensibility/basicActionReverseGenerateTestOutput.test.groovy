import com.vmware.devops.client.cloudassembly.extensibility.stubs.Action.Runtime
import com.vmware.devops.model.cloudassembly.extensibility.Action
import com.vmware.devops.model.cloudassembly.extensibility.Action.Dependency
import com.vmware.devops.model.cloudassembly.extensibility.Action.Dependency.Operation

return Action.builder()
        .name("test")
        .contentPath("test-action-script.py")
        .entrypoint("entrypoint")
        .inputs([
                "k1": "v1",
                "k2": "true",
        ])
        .timeout(6)
        .memory(450)
        .runtime(Runtime.PYTHON)
        .shared(true)
        .project("test-project")
        .dependencies([
                Dependency.builder()
                        .name("x")
                        .operation(Operation.EQUALS)
                        .version("1.0.0")
                        .build(),
                Dependency.builder()
                        .name("y")
                        .operation(Operation.GREATER_OR_EQUALS)
                        .version("2.0.0")
                        .build(),
        ])
        .build()