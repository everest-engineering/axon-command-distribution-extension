package engineering.everest.axon;

import org.axonframework.commandhandling.gateway.DefaultCommandGateway;

import java.io.Serializable;
import java.util.concurrent.Callable;

class AxonDistributableCommand<R> implements Callable<R>, Serializable {

    private final Serializable command;

    public AxonDistributableCommand(Serializable command) {
        this.command = command;
    }

    @Override
    public R call() {
        DefaultCommandGateway commandGateway = HazelcastApplicationContextHolder.getApplicationContext()
            .getBean(DefaultCommandGateway.class);
        return commandGateway.sendAndWait(command);
    }
}
