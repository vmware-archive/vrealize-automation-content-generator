import com.vmware.devops.model.cloudassembly.infrastructure.AwsCloudAccount

return AwsCloudAccount.builder()
        .name("test")
        .accessKeyId("key")
        .secretAccessKey("SET_ME")
        .enabledRegions([
                "eu-west-1",
                "eu-west-2",
        ])
        .build()