package engineering.everest.axon;

import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
public class CompletableFutureFactory {

    public <R> CompletableFuture<R> create() {
        return new CompletableFuture<>();
    }
}
