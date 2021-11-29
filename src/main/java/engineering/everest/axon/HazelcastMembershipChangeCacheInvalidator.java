package engineering.everest.axon;

import com.hazelcast.cluster.MembershipEvent;
import com.hazelcast.cluster.MembershipListener;
import com.hazelcast.core.HazelcastInstance;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.cache.CacheManager;

import static engineering.everest.axon.config.AxonHazelcastConfig.AXON_AGGREGATES_CACHE;

/**
 * Invalidates the Axon aggregate cache when cluster membership changes in order to avoid routing
 * commands to a stale aggregate.
 * <p>
 * This situation is only likely to arise when cluster membership is frequently changing due to,
 * for example, network issues or a rolling deployment.
 */
@Component
class HazelcastMembershipChangeCacheInvalidator implements MembershipListener {

    private final CacheManager cacheManager;

    public HazelcastMembershipChangeCacheInvalidator(HazelcastInstance hazelcastInstance,
                                                     @Qualifier("axon-cache-manager") CacheManager cacheManager) {
        hazelcastInstance.getCluster().addMembershipListener(this);
        this.cacheManager = cacheManager;
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
        cacheManager.getCache(AXON_AGGREGATES_CACHE).clear();
    }
}
