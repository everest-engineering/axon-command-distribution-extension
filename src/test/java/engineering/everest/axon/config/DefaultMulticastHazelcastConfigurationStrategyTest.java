package engineering.everest.axon.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class DefaultMulticastHazelcastConfigurationStrategyTest {

    private DefaultMulticastHazelcastConfigurationStrategy defaultMulticastHazelcastConfigurationStrategy;

    @BeforeEach
    void setUp() {
        defaultMulticastHazelcastConfigurationStrategy = new DefaultMulticastHazelcastConfigurationStrategy();
    }

    @Test
    void canApply_WillOnlyReturnTrue() {
        assertTrue(defaultMulticastHazelcastConfigurationStrategy.canApply());
    }
}
