import com.vmware.devops.client.servicebroker.stubs.Policy.EnforcementType
import com.vmware.devops.model.servicebroker.LeasePolicy

return LeasePolicy.builder()
        .name("test")
        .enforcementType(EnforcementType.HARD)
        .maxLease(1)
        .maxTotalLease(2)
        .gracePeriod(3)
        .build()