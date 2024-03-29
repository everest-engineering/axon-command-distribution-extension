package engineering.everest.axon.config;

import com.hazelcast.config.Config;
import com.hazelcast.config.ExecutorConfig;
import lombok.extern.log4j.Log4j2;
import org.axonframework.common.caching.JCacheAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.cache.CacheManager;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.expiry.CreatedExpiryPolicy;
import java.util.List;

import static javax.cache.expiry.Duration.FIVE_MINUTES;

@Configuration
@Log4j2
public class AxonHazelcastConfig {
    public static final String AXON_COMMAND_DISPATCHER = "axon-command-dispatcher";
    public static final String AXON_AGGREGATES_CACHE = "axonAggregates";

    private final CacheManager cacheManager;
    private final List<HazelcastConfigurationStrategy> configurationStrategies;

    @Autowired
    public AxonHazelcastConfig(@Qualifier("axon-cache-manager") CacheManager cacheManager,
                               DefaultMulticastHazelcastConfigurationStrategy defaultMulticastHazelcastConfigurationStrategy,
                               KubernetesHazelcastConfigurationStrategy kubernetesHazelcastConfigurationStrategy,
                               TcpIpConfigHazelcastConfigurationStrategy tcpIpConfigHazelcastConfigurationStrategy) {
        this.cacheManager = cacheManager;
        this.configurationStrategies = List.of(
            tcpIpConfigHazelcastConfigurationStrategy,
            kubernetesHazelcastConfigurationStrategy,
            defaultMulticastHazelcastConfigurationStrategy);
    }

    @Bean
    public Config hazelcastConfiguration() {
        var hazelcastConfiguration = new Config();
        hazelcastConfiguration.setInstanceName("axon")
            .addExecutorConfig(new ExecutorConfig().setName(AXON_COMMAND_DISPATCHER));

        configurationStrategies.stream()
            .filter(HazelcastConfigurationStrategy::canApply)
            .findFirst().orElseThrow()
            .apply(hazelcastConfiguration);

        return hazelcastConfiguration;
    }

    @Bean
    @Qualifier("axon-aggregates-cache-adapter")
    @SuppressWarnings("PMD.CloseResource")
    public JCacheAdapter cacheAdapter() {
        var config = new MutableConfiguration<>()
            .setStoreByValue(false)
            .setStatisticsEnabled(true)
            .setManagementEnabled(true)
            .setExpiryPolicyFactory(CreatedExpiryPolicy.factoryOf(FIVE_MINUTES));

        return new JCacheAdapter(cacheManager.createCache(AXON_AGGREGATES_CACHE, config));
    }
}
