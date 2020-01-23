package engineering.everest.starterkit.axon.config;

import com.hazelcast.config.Config;
import com.hazelcast.config.ExecutorConfig;
import com.hazelcast.config.JoinConfig;
import io.kubernetes.client.util.ClientBuilder;
import lombok.extern.log4j.Log4j2;
import org.axonframework.common.caching.JCacheAdapter;
import org.axonframework.modelling.command.AnnotationCommandTargetResolver;
import org.ehcache.jsr107.EhcacheCachingProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.cache.CacheManager;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.expiry.CreatedExpiryPolicy;
import java.io.IOException;

import static javax.cache.Caching.getCachingProvider;
import static javax.cache.expiry.Duration.FIVE_MINUTES;

@Configuration
@Log4j2
public class AxonHazelcastConfig {
    public static final String AXON_COMMAND_DISPATCHER = "axon-command-dispatcher";

    private static final String AXON_AGGREGATES_CACHE = "axonAggregates";
    private static final String KUBERNETES_NAMESPACE = "default";
    private static final String KUBERNETES_SERVICE_NAME = "web-app";

    @Bean
    public Config hazelcastConfiguration() {
        var executorConfig = new ExecutorConfig();
        executorConfig.setName(AXON_COMMAND_DISPATCHER);

        var hazelcastConfiguration = new Config();
        hazelcastConfiguration.setInstanceName("axon")
                .addExecutorConfig(executorConfig);

        // TODO: Spring 5.2 introduced @ConditionalOnCloudPlatform annotation
        if (isRunningInKubernetes()) {
            LOGGER.info("**********************************************************");
            LOGGER.info("************* HAZELCAST DETECTED KUBERNETES **************");
            LOGGER.info("**********************************************************");
            JoinConfig joinConfig = hazelcastConfiguration.getNetworkConfig().getJoin();
            joinConfig.getMulticastConfig().setEnabled(false);
            joinConfig.getKubernetesConfig()
                    .setEnabled(true)
                    .setProperty("namespace", KUBERNETES_NAMESPACE)
                    .setProperty("service-name", KUBERNETES_SERVICE_NAME);
        } else {
            LOGGER.info("Hazelcast will use multicast for service discovery");
        }

        return hazelcastConfiguration;
    }

    @Bean
    @Qualifier("axon-aggregates-cache-adapter")
    @SuppressWarnings("PMD.CloseResource")
    public JCacheAdapter cacheAdapter() {
        CacheManager cacheManager = getCachingProvider(EhcacheCachingProvider.class.getCanonicalName()).getCacheManager();
        var config = new MutableConfiguration<String, Object>()
                .setTypes(String.class, Object.class)
                .setStoreByValue(false)
                .setStatisticsEnabled(true)
                .setManagementEnabled(true)
                .setExpiryPolicyFactory(CreatedExpiryPolicy.factoryOf(FIVE_MINUTES));

        var cache = cacheManager.createCache(AXON_AGGREGATES_CACHE, config);
        return new JCacheAdapter(cache);
    }

    @Bean
    public AnnotationCommandTargetResolver annotationCommandTargetResolver() {
        return AnnotationCommandTargetResolver.builder().build();
    }

    private boolean isRunningInKubernetes() {
        try {
            ClientBuilder.cluster().build();
        } catch (IOException e) {
            return false; // Throws if service account token not present
        }
        return true;
    }
}
