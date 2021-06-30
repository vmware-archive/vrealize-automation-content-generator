import com.vmware.devops.model.codestream.AgentEndpoint

return AgentEndpoint.builder()
        .name("test")
        .project("project")
        .cloudProxy("proxy")
        .build()