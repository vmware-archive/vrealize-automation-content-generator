import com.vmware.devops.model.cloudassembly.infrastructure.FlavorMapping
import com.vmware.devops.model.cloudassembly.infrastructure.NimbusCloudAccount.NimbusFlavor
import com.vmware.devops.model.cloudassembly.infrastructure.VsphereCloudAccount.VsphereFlavor

return FlavorMapping.builder()
        .name("small")
        .flavorMapping([
                "nimbus-endpoint / sc": NimbusFlavor.DEFAULT,
                "nimbus-endpoint / wdc": NimbusFlavor.DEFAULT,
                "vsphere-endpoint / DATACENTER-1": VsphereFlavor.builder()
                            .cpuCount(8)
                            .memoryMb(4096)
                            .build(),
        ])
        .build();