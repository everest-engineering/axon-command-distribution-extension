package engineering.everest.starterkit.axon.config;

import com.hazelcast.config.Config;
import com.hazelcast.config.JoinConfig;
import io.kubernetes.client.util.ClientBuilder;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import java.io.IOException;

import static io.kubernetes.client.util.Config.ENV_SERVICE_HOST;
import static io.kubernetes.client.util.Config.ENV_SERVICE_PORT;

@Component
@Log4j2
public class KubernetesHazelcastConfigurationStrategy implements HazelcastConfigurationStrategy {

    private static final String KUBERNETES_NAMESPACE = "default";     // TODO: make configurable
    private static final String KUBERNETES_SERVICE_NAME = "web-app";  // TODO: make configurable

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
                .setProperty("namespace", KUBERNETES_NAMESPACE)
                .setProperty("service-name", KUBERNETES_SERVICE_NAME);
    }

    private boolean isRunningInKubernetes() {
        return System.getenv(ENV_SERVICE_HOST) != null && System.getenv(ENV_SERVICE_PORT) != null;
    }
}
