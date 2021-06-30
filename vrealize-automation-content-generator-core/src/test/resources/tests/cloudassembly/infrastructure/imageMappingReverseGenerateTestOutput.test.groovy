import com.vmware.devops.model.cloudassembly.infrastructure.ImageMapping

return ImageMapping.builder()
        .name("test")
        .imageMapping([
            "endpoint / sc": "image-1",
            "endpoint / wdc": "image-2",
        ])
        .build()