package engineering.everest.starterkit.axon;


import com.hazelcast.core.Cluster;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.LifecycleService;
import com.hazelcast.core.Member;
import com.hazelcast.core.PartitionService;
import com.hazelcast.version.Version;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.Set;

import static com.hazelcast.cluster.ClusterState.IN_TRANSITION;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.boot.actuate.health.Status.DOWN;
import static org.springframework.boot.actuate.health.Status.UP;

@ExtendWith(MockitoExtension.class)
class HazelcastHealthIndicatorTest {

    public static final String INSTANCE_NAME = "instance-name";
    public static final Version CLUSTER_VERSION = new Version();
    public static final Set<Member> CLUSTER_MEMBERS = Set.of(mock(Member.class), mock(Member.class), mock(Member.class));
    private HazelcastHealthIndicator hazelcastHealthIndicator;

    @Mock
    private HazelcastInstance hazelcastInstance;
    @Mock
    private PartitionService partitionService;
    @Mock
    private LifecycleService lifecycleService;
    @Mock
    private Cluster cluster;

    @BeforeEach
    void setUp() {
        hazelcastHealthIndicator = new HazelcastHealthIndicator(hazelcastInstance);

        when(hazelcastInstance.getPartitionService()).thenReturn(partitionService);
        when(hazelcastInstance.getLifecycleService()).thenReturn(lifecycleService);
        when(hazelcastInstance.getCluster()).thenReturn(cluster);
        when(hazelcastInstance.getName()).thenReturn(INSTANCE_NAME);
        when(cluster.getMembers()).thenReturn(CLUSTER_MEMBERS);
        when(cluster.getClusterState()).thenReturn(IN_TRANSITION);
        when(cluster.getClusterVersion()).thenReturn(CLUSTER_VERSION);
        when(partitionService.isClusterSafe()).thenReturn(true);
        when(lifecycleService.isRunning()).thenReturn(true);
    }

    @Test
    void healthWillReturnDetailsAboutHazelcastInstance() {
        var health = hazelcastHealthIndicator.health();
        assertEquals(UP, health.getStatus());

        var expectedHealthDetails = Map.of(
                "instance-name", INSTANCE_NAME,
                "cluster-size", CLUSTER_MEMBERS.size(),
                "cluster-time", 0L,
                "cluster-state", IN_TRANSITION,
                "cluster-version", CLUSTER_VERSION,
                "cluster-safe", true);
        assertEquals(expectedHealthDetails, health.getDetails());
    }

    @Test
    void healthWillBeDown_WhenHazelcastIsShuttingDown() {
        when(lifecycleService.isRunning()).thenReturn(false);

        assertEquals(DOWN, hazelcastHealthIndicator.health().getStatus());
    }
}
