import com.vmware.devops.model.cloudassembly.infrastructure.AwsCloudAccount
import com.vmware.devops.model.cloudassembly.infrastructure.AwsCloudAccount.AwsRegion

return AwsCloudAccount.builder()
        .name("test")
        .accessKeyId("key")
        .secretAccessKey("SET_ME")
        .enabledRegions([
                AwsRegion.EU_WEST_1,
                AwsRegion.EU_WEST_2,
        ])
        .build()