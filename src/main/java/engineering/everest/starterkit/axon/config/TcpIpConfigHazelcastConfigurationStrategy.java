package engineering.everest.starterkit.axon.config;

import com.hazelcast.config.Config;
import com.hazelcast.config.JoinConfig;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Hazelcast configuration strategy that explicitly defines cluster members if the
 * {@code axon.hazelcast.members} property has been defined.
 *
 * @see DefaultMulticastHazelcastConfigurationStrategy
 * @see KubernetesHazelcastConfigurationStrategy
 */
@Component
@Log4j2
class TcpIpConfigHazelcastConfigurationStrategy implements HazelcastConfigurationStrategy {

    @Value("${axon.hazelcast.members:}")
    private List<String> hazelcastMembers;

    @Override
    public boolean canApply() {
        return !hazelcastMembers.isEmpty();
    }

    @Override
    public void apply(Config hazelcastConfiguration) {
        LOGGER.info("Hazelcast using explicit member list; service discovery disabled");
        JoinConfig joinConfig = hazelcastConfiguration.getNetworkConfig().getJoin();
        joinConfig.getMulticastConfig().setEnabled(false);
        joinConfig.getTcpIpConfig()
                .setMembers(hazelcastMembers)
                .setEnabled(true);
    }
}
