import com.vmware.devops.model.codestream.Variable

return Variable.builder()
        .name("test")
        .description("description")
        .type(com.vmware.devops.client.codestream.stubs.Variable.VariableType.REGULAR)
        .value("value")
        .build()