import com.vmware.devops.model.codestream.JiraEndpoint

return JiraEndpoint.builder()
        .name("test")
        .project("project")
        .url("url")
        .username("username")
        .password("password")
        .acceptCertificate(true)
        .cloudProxy("proxy")
        .build()