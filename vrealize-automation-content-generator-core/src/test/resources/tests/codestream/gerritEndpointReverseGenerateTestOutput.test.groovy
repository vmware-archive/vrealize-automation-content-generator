import com.vmware.devops.model.codestream.GerritEndpoint

return GerritEndpoint.builder()
        .name("test")
        .project("project")
        .url("url")
        .username("username")
        .password("password")
        .privateKey("""privateKey""")
        .passPhrase("passphrase")
        .cloudProxy("proxy")
        .build()