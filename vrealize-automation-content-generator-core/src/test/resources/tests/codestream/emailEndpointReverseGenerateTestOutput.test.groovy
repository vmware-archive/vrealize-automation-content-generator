import com.vmware.devops.client.codestream.stubs.Endpoint
import com.vmware.devops.model.codestream.EmailEndpoint

return EmailEndpoint.builder()
        .name("test")
        .project("project")
        .outboundHost("outboundHost")
        .outboundPort(25)
        .outboundUsername("outboundUsername")
        .outboundPassword("outboundPassword")
        .outboundProtocol(Endpoint.EmailProtocol.SMTP)
        .senderAddress("senderAddress")
        .encryptionMethod(Endpoint.EmailEncryptionMethod.TLS)
        .cloudProxy("proxy")
        .build()