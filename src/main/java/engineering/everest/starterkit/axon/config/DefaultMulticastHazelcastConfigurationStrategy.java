package engineering.everest.starterkit.axon.config;

import com.hazelcast.config.Config;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

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
