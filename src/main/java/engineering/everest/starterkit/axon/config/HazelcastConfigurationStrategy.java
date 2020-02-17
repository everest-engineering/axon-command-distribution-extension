package engineering.everest.starterkit.axon.config;

import com.hazelcast.config.Config;

interface HazelcastConfigurationStrategy {

    boolean canApply();

    void apply(Config hazelcastConfiguration);
}
