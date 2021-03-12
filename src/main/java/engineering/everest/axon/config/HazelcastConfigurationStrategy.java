package engineering.everest.axon.config;

import com.hazelcast.config.Config;

public interface HazelcastConfigurationStrategy {

    boolean canApply();

    void apply(Config hazelcastConfiguration);
}
