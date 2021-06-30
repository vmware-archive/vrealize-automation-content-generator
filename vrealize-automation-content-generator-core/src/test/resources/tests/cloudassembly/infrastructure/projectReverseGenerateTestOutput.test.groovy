import com.vmware.devops.client.cloudassembly.infrastructure.stubs.ProjectPrincipal
import com.vmware.devops.client.cloudassembly.infrastructure.stubs.Project.PlacementPolicy
import com.vmware.devops.model.cloudassembly.infrastructure.Project

return Project.builder()
        .name("test")
        .users([
                Project.UserGroup.builder()
                        .email("admin@org.com")
                        .role(ProjectPrincipal.Role.ADMINISTRATOR)
                        .build(),
                Project.UserGroup.builder()
                        .email("member@org.com")
                        .role(ProjectPrincipal.Role.MEMBER)
                        .build(),
                Project.UserGroup.builder()
                        .email("viewer@org.com")
                        .role(ProjectPrincipal.Role.VIEWER)
                        .build(),
        ])
        .groups([
                Project.UserGroup.builder()
                        .email("admin-group")
                        .role(ProjectPrincipal.Role.ADMINISTRATOR)
                        .build(),
        ])
        .cloudZones([
                "sc",
                "wdc",
        ])
        .placementPolicy(PlacementPolicy.SPREAD)
        .build()