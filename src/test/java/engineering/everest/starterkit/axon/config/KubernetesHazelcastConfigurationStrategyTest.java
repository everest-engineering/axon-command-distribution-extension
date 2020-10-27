package engineering.everest.starterkit.axon.config;


import com.hazelcast.config.Config;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static com.github.stefanbirkner.systemlambda.SystemLambda.withEnvironmentVariable;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class KubernetesHazelcastConfigurationStrategyTest {

    private Config configuration;

    @BeforeEach
    void setUp() {
        configuration = new Config();
        new KubernetesHazelcastConfigurationStrategy().apply(configuration);
    }

    @Test
    void apply_WillDisableMulticastConfiguration() {
        assertFalse(configuration.getNetworkConfig().getJoin().getMulticastConfig().isEnabled());
    }

    @Test
    void apply_WillEnableKubernetesConfiguration() {
        assertTrue(configuration.getNetworkConfig().getJoin().getKubernetesConfig().isEnabled());
    }

    @Test
    void apply_WillSetNamespaceAndServiceName() {
        var properties = configuration.getNetworkConfig().getJoin().getKubernetesConfig().getProperties();
        assertEquals(Map.of("namespace", "default", "service-name", "web-app"), properties);
    }

    @Test
    void canApply_WillBeTrue_WhenKubernetesEnvironmentVariablesSet() throws Exception {
        var canApply = withEnvironmentVariable("KUBERNETES_SERVICE_HOST", "localhost")
                .and("KUBERNETES_SERVICE_PORT", "1234")
                .execute(() -> new KubernetesHazelcastConfigurationStrategy().canApply());
        assertTrue(canApply);
    }

    @Test
    void canApply_WillBeFalse_WhenKubernetesPortEnvironmentVariableMissing() throws Exception {
        var canApply = withEnvironmentVariable("KUBERNETES_SERVICE_HOST", "localhost")
                .execute(() -> new KubernetesHazelcastConfigurationStrategy().canApply());
        assertFalse(canApply);
    }

    @Test
    void canApply_WillBeFalse_WhenKubernetesHostEnvironmentVariableMissing() throws Exception {
        var canApply = withEnvironmentVariable("KUBERNETES_SERVICE_PORT", "1234")
                .execute(() -> new KubernetesHazelcastConfigurationStrategy().canApply());
        assertFalse(canApply);
    }
}