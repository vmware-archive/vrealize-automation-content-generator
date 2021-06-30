import com.vmware.devops.client.cloudassembly.extensibility.stubs.Subscription.EventTopic
import com.vmware.devops.client.cloudassembly.extensibility.stubs.Subscription.RunnableType
import com.vmware.devops.model.cloudassembly.extensibility.Criteria
import com.vmware.devops.model.cloudassembly.extensibility.Subscription

return Subscription.builder()
        .name("test")
        .runnableName("test-action")
        .runnableType(RunnableType.ACTION)
        .recoverRunnableName("test-recover-action")
        .recoverRunnableType(RunnableType.ACTION)
        .eventTopic(EventTopic.POST_COMPUTE_PROVISION)
        .blocking(true)
        .criteria(new Criteria("test.input == \"some-value\" && test.otherInput != \"some-other-value\""))
        .build()