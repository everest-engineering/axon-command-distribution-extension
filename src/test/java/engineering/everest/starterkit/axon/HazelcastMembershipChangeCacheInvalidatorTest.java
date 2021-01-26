package engineering.everest.starterkit.axon;

import com.hazelcast.cluster.Cluster;
import com.hazelcast.cluster.MembershipEvent;
import com.hazelcast.core.HazelcastInstance;
import engineering.everest.starterkit.axon.HazelcastMembershipChangeCacheInvalidator;
import org.ehcache.jsr107.EhcacheCachingProvider;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.cache.CacheManager;
import javax.cache.configuration.MutableConfiguration;

import static engineering.everest.starterkit.axon.config.AxonHazelcastConfig.AXON_AGGREGATES_CACHE;
import static java.util.UUID.randomUUID;
import static javax.cache.Caching.getCachingProvider;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HazelcastMembershipChangeCacheInvalidatorTest {

    private HazelcastMembershipChangeCacheInvalidator hazelcastMembershipChangeCacheInvalidator;

    @Mock
    private HazelcastInstance hazelcastInstance;
    @Mock
    private Cluster cluster;

    @BeforeEach
    void setUp() {
        getCacheManager().createCache(AXON_AGGREGATES_CACHE, new MutableConfiguration<>());
        when(hazelcastInstance.getCluster()).thenReturn(cluster);

        hazelcastMembershipChangeCacheInvalidator = new HazelcastMembershipChangeCacheInvalidator(hazelcastInstance);
    }

    @AfterEach
    void tearDown() {
        getCacheManager().destroyCache(AXON_AGGREGATES_CACHE);
    }

    @Test
    void registersItselfAsAHazelcastClusterMembershipListener() {
        verify(cluster).addMembershipListener(hazelcastMembershipChangeCacheInvalidator);
    }

    @Test
    void axonCacheIsCleared_WhenClusterMemberAdded() {
        getCacheManager().getCache(AXON_AGGREGATES_CACHE).put(randomUUID(), "a cached object");

        hazelcastMembershipChangeCacheInvalidator.memberAdded(mock(MembershipEvent.class));

        assertFalse(getCacheManager().getCache(AXON_AGGREGATES_CACHE).iterator().hasNext());
    }

    @Test
    void axonCacheIsCleared_WhenClusterMemberRemoved() {
        getCacheManager().getCache(AXON_AGGREGATES_CACHE).put(randomUUID(), "a cached object");

        hazelcastMembershipChangeCacheInvalidator.memberRemoved(mock(MembershipEvent.class));

        assertFalse(getCacheManager().getCache(AXON_AGGREGATES_CACHE).iterator().hasNext());
    }

    private CacheManager getCacheManager() {
        return getCachingProvider(EhcacheCachingProvider.class.getCanonicalName()).getCacheManager();
    }
}