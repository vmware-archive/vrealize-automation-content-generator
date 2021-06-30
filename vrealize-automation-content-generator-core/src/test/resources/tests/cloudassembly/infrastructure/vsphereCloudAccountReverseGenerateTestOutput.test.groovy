import com.vmware.devops.model.cloudassembly.infrastructure.VsphereCloudAccount

return VsphereCloudAccount.builder()
        .name("test")
        .hostname("vc-hostname")
        .username("username")
        .password("SET_ME")
        .datacenters([
                "Datacenter-1",
                "Datacenter-2",
        ])
        .cloudProxy("data-collector")
        .build()