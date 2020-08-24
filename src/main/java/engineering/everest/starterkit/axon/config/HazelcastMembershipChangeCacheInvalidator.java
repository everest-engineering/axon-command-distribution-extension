package engineering.everest.starterkit.axon.config;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.MemberAttributeEvent;
import com.hazelcast.core.MembershipEvent;
import com.hazelcast.core.MembershipListener;
import org.ehcache.jsr107.EhcacheCachingProvider;
import org.springframework.stereotype.Component;

import static engineering.everest.starterkit.axon.config.AxonHazelcastConfig.AXON_AGGREGATES_CACHE;
import static javax.cache.Caching.getCachingProvider;

@Component
public class HazelcastMembershipChangeCacheInvalidator implements MembershipListener {

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

    @Override
    public void memberAttributeChanged(MemberAttributeEvent memberAttributeEvent) {
    }

    private void clearLocalAxonCache() {
        var cacheManager = getCachingProvider(EhcacheCachingProvider.class.getCanonicalName()).getCacheManager();
        cacheManager.getCache(AXON_AGGREGATES_CACHE).clear();
    }
}