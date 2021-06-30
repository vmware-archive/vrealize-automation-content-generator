import com.vmware.devops.model.codestream.JenkinsEndpoint

return JenkinsEndpoint.builder()
        .name("test")
        .project("project")
        .url("url")
        .username("username")
        .password("password")
        .pollInterval(3)
        .retryCount(4)
        .retryWaitSeconds(5)
        .cloudProxy("proxy")
        .build()