package engineering.everest.axon.config;

import com.hazelcast.config.Config;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.util.ReflectionTestUtils.setField;

class TcpIpConfigHazelcastConfigurationStrategyTest {

    private static final List<String> CLUSTER_MEMBERS = List.of("host1", "host2", "host3");

    private TcpIpConfigHazelcastConfigurationStrategy tcpIpConfigHazelcastConfigurationStrategy;
    private Config configuration;

    @BeforeEach
    void setUp() {
        tcpIpConfigHazelcastConfigurationStrategy = new TcpIpConfigHazelcastConfigurationStrategy();
        setField(tcpIpConfigHazelcastConfigurationStrategy, "hazelcastMembers", CLUSTER_MEMBERS);

        configuration = new Config();
        tcpIpConfigHazelcastConfigurationStrategy.apply(configuration);
    }

    @Test
    void canApply_WillBeFalseWhenHazelcastMembersNotConfigured() {
        setField(tcpIpConfigHazelcastConfigurationStrategy, "hazelcastMembers", emptyList());
        assertFalse(tcpIpConfigHazelcastConfigurationStrategy.canApply());
    }

    @Test
    void canApply_WillBeTrueWhenHazelcastMembersConfigured() {
        assertTrue(tcpIpConfigHazelcastConfigurationStrategy.canApply());
    }

    @Test
    void apply_WillDisableMulticastConfiguration() {
        assertFalse(configuration.getNetworkConfig().getJoin().getMulticastConfig().isEnabled());
    }

    @Test
    void apply_WillEnableTcpIpConfiguration() {
        assertTrue(configuration.getNetworkConfig().getJoin().getTcpIpConfig().isEnabled());
    }

    @Test
    void apply_WillSetClusterMembers() {
        assertEquals(CLUSTER_MEMBERS, configuration.getNetworkConfig().getJoin().getTcpIpConfig().getMembers());
    }
}