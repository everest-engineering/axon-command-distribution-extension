package engineering.everest.axon.config;

import com.hazelcast.config.Config;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

/**
 * Hazelcast configuration strategy that uses multicast to detect cluster members.
 * <p>
 * This configuration strategy will typically not work in cloud provider environments or within an orchestrated container environment such
 * as Kubernetes.
 *
 * @see KubernetesHazelcastConfigurationStrategy
 * @see TcpIpConfigHazelcastConfigurationStrategy
 */
@Component
@Log4j2
class DefaultMulticastHazelcastConfigurationStrategy implements HazelcastConfigurationStrategy {

    @Override
    public boolean canApply() {
        return true;
    }

    @Override
    public void apply(Config hazelcastConfiguration) {
        LOGGER.info("Hazelcast using use multicast service discovery");
    }
}
