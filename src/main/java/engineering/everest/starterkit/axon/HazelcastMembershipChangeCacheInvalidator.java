package engineering.everest.starterkit.axon;

import com.hazelcast.cluster.MembershipEvent;
import com.hazelcast.cluster.MembershipListener;
import com.hazelcast.core.HazelcastInstance;
import org.ehcache.jsr107.EhcacheCachingProvider;
import org.springframework.stereotype.Component;

import static engineering.everest.starterkit.axon.config.AxonHazelcastConfig.AXON_AGGREGATES_CACHE;
import static javax.cache.Caching.getCachingProvider;

/**
 * Invalidates the Axon aggregate cache when cluster membership changes in order to avoid routing
 * commands to a stale aggregate.
 * <p>
 * This situation is only likely to arise when cluster membership is frequently changing due to,
 * for example, network issues or a rolling deployment.
 */
@Component
class HazelcastMembershipChangeCacheInvalidator implements MembershipListener {

    public HazelcastMembershipChangeCacheInvalidator(HazelcastInstance hazelcastInstance) {
        hazelcastInstance.getCluster().addMembershipListener(this);
    }

    @Override
    public void memberAdded(MembershipEvent membershipEvent) {
        clearLocalAxonCache();
    }

    @Override
    public void memberRemoved(MembershipEvent membershipEvent) {
        clearLocalAxonCache();
    }

    private void clearLocalAxonCache() {
        var cacheManager = getCachingProvider(EhcacheCachingProvider.class.getCanonicalName()).getCacheManager();
        cacheManager.getCache(AXON_AGGREGATES_CACHE).clear();
    }
}
