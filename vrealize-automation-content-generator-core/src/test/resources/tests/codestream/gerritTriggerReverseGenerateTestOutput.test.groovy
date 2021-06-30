import com.vmware.devops.model.codestream.GerritTrigger

return GerritTrigger.builder()
        .name("test")
        .project("test-project")
        .gerritProject("test-gerrit-project")
        .branch("master")
        .configurations([
                GerritTrigger.Configuration.builder()
                        .pipeline("pipeline")
                        .eventType(com.vmware.devops.client.codestream.stubs.GerritTrigger.Configuration.EventType.CHANGE_MERGED)
                        .input([
                                "k1": "v1",
                                "k2": "v2",
                        ])
                        .failureComment("Pipeline failed")
                        .successComment("Pipeline success")
                        .build(),
        ])
        .inclusions([
                GerritTrigger.Pattern.builder()
                        .type(com.vmware.devops.client.codestream.stubs.GerritTrigger.Pattern.PatternType.PLAIN)
                        .value("inclusion-value")
                        .build(),
                GerritTrigger.Pattern.builder()
                        .type(com.vmware.devops.client.codestream.stubs.GerritTrigger.Pattern.PatternType.REGEX)
                        .value("value*")
                        .build(),
        ])
        .exclusions([
                GerritTrigger.Pattern.builder()
                        .type(com.vmware.devops.client.codestream.stubs.GerritTrigger.Pattern.PatternType.PLAIN)
                        .value("exclusion-value")
                        .build(),
        ])
        .prioritizeExclusion(false)
        .enabled(false)
        .listener("listener")
        .build()