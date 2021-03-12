package engineering.everest.axon;


import lombok.AllArgsConstructor;
import lombok.Data;
import org.axonframework.commandhandling.gateway.DefaultCommandGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;

import java.io.Serializable;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class AxonDistributableCommandTest {

    @Mock
    private ApplicationContext applicationContext;
    @Mock
    private HazelcastApplicationContextHolder hazelcastApplicationContextHolder;
    @Mock
    private DefaultCommandGateway defaultCommandGateway;

    @BeforeEach
    void setUp() {
        new HazelcastApplicationContextHolder().setApplicationContext(applicationContext);
        when(applicationContext.getBean(DefaultCommandGateway.class)).thenReturn(defaultCommandGateway);
    }

    @Test
    void call_WillUseApplicationContextDefaultCommandGatewayToSendCommandAndWait() {
        var serializableCommand = new TestCommand("message");
        var distributableCommand = new AxonDistributableCommand<String>(serializableCommand);
        var result = distributableCommand.call();

        verify(defaultCommandGateway).sendAndWait(serializableCommand);
    }


    @Data
    @AllArgsConstructor
    private static class TestCommand implements Serializable {
        private String message;
    }
}