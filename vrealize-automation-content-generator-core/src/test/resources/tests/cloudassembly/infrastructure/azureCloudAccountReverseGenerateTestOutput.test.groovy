import com.vmware.devops.model.cloudassembly.infrastructure.AzureCloudAccount
import com.vmware.devops.model.cloudassembly.infrastructure.AzureCloudAccount.AzureRegion

return AzureCloudAccount.builder()
        .name("test")
        .subscriptionId("subscription-id")
        .tenantId("tenant-id")
        .clientApplicationId("client-id")
        .clientApplicationSecretKey("SET_ME")
        .enabledRegions([
                AzureRegion.EAST_US,
                AzureRegion.EUROPE,
        ])
        .build()