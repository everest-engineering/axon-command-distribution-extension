package engineering.everest.axon.config;

import com.hazelcast.config.Config;
import com.hazelcast.config.JoinConfig;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import static io.kubernetes.client.util.Config.ENV_SERVICE_HOST;
import static io.kubernetes.client.util.Config.ENV_SERVICE_PORT;
import static org.apache.commons.lang.StringUtils.isNotBlank;

/**
 * Hazelcast configuration strategy for Kubernetes.
 * <p>
 * The default namespace and service name can be overridden by defining the
 * {@code axon.hazelcast.kubernetes.namespace} and {@code axon.hazelcast.kubernetes.service} properties.
 *
 * @see DefaultMulticastHazelcastConfigurationStrategy
 * @see TcpIpConfigHazelcastConfigurationStrategy
 */
@Component
@Log4j2
class KubernetesHazelcastConfigurationStrategy implements HazelcastConfigurationStrategy {

    @Value("${axon.hazelcast.kubernetes.namespace:default}")
    private String kubernetesNamespace;
    @Value("${axon.hazelcast.kubernetes.service:web-app}")
    private String kubernetesServiceName;

    @Override
    public boolean canApply() {
        return isRunningInKubernetes();
    }

    @Override
    public void apply(Config hazelcastConfiguration) {
        LOGGER.info("Hazelcast using Kubernetes service discovery");
        JoinConfig joinConfig = hazelcastConfiguration.getNetworkConfig().getJoin();
        joinConfig.getMulticastConfig().setEnabled(false);
        joinConfig.getKubernetesConfig()
                .setEnabled(true)
                .setProperty("namespace", kubernetesNamespace)
                .setProperty("service-name", kubernetesServiceName);
    }

    private boolean isRunningInKubernetes() {
        return isNotBlank(System.getenv(ENV_SERVICE_HOST)) && isNotBlank(System.getenv(ENV_SERVICE_PORT));
    }
}
