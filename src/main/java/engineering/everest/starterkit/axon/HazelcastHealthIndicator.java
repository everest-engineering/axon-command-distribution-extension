package engineering.everest.starterkit.axon;

import com.hazelcast.core.HazelcastInstance;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

/**
 * Health indicator based on the hazelcast lifecycle state.
 * <p>
 * Application instances are deemed unhealthy when Hazelcast shuts down, allowing an orchestrator
 * to terminate that the instance and replace it.
 */
@Component
public class HazelcastHealthIndicator implements HealthIndicator {
    private final HazelcastInstance hazelcastInstance;

    public HazelcastHealthIndicator(HazelcastInstance hazelcastInstance) {
        this.hazelcastInstance = hazelcastInstance;
    }

    @Override
    public Health health() {
        var partitionService = hazelcastInstance.getPartitionService();
        var cluster = hazelcastInstance.getCluster();

        Health.Builder builder = hazelcastInstance.getLifecycleService().isRunning() ? Health.up() : Health.down();
        return builder
                .withDetail("instance-name", hazelcastInstance.getName())
                .withDetail("cluster-size", cluster.getMembers().size())
                .withDetail("cluster-state", cluster.getClusterState())
                .withDetail("cluster-time", cluster.getClusterTime())
                .withDetail("cluster-version", cluster.getClusterVersion())
                .withDetail("cluster-safe", partitionService.isClusterSafe())  // Unsafe doesn't mean down yet
                .build();
    }
}
