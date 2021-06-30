import com.vmware.devops.model.cloudassembly.infrastructure.NimbusCloudAccount

return NimbusCloudAccount.builder()
        .enabledRegions([
                NimbusCloudAccount.NimbusRegion.SC,
                NimbusCloudAccount.NimbusRegion.WDC,
        ])
        .build()