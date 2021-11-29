package engineering.everest.axon;

import com.hazelcast.cluster.Cluster;
import com.hazelcast.cluster.MembershipEvent;
import com.hazelcast.core.HazelcastInstance;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.cache.Cache;
import javax.cache.CacheManager;

import static engineering.everest.axon.config.AxonHazelcastConfig.AXON_AGGREGATES_CACHE;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HazelcastMembershipChangeCacheInvalidatorTest {

    private HazelcastMembershipChangeCacheInvalidator hazelcastMembershipChangeCacheInvalidator;

    @Mock
    private HazelcastInstance hazelcastInstance;
    @Mock
    private CacheManager cacheManager;
    @Mock
    private Cache cache;
    @Mock
    private Cluster cluster;

    @BeforeEach
    void setUp() {
        lenient().when(cacheManager.getCache(AXON_AGGREGATES_CACHE)).thenReturn(cache);
        when(hazelcastInstance.getCluster()).thenReturn(cluster);

        hazelcastMembershipChangeCacheInvalidator = new HazelcastMembershipChangeCacheInvalidator(hazelcastInstance, cacheManager);
    }

    @Test
    void registersItselfAsAHazelcastClusterMembershipListener() {
        verify(cluster).addMembershipListener(hazelcastMembershipChangeCacheInvalidator);
    }

    @Test
    void axonCacheIsCleared_WhenClusterMemberAdded() {
        hazelcastMembershipChangeCacheInvalidator.memberAdded(mock(MembershipEvent.class));

        verify(cache).clear();
    }

    @Test
    void axonCacheIsCleared_WhenClusterMemberRemoved() {
        hazelcastMembershipChangeCacheInvalidator.memberRemoved(mock(MembershipEvent.class));

        verify(cache).clear();
    }
}
