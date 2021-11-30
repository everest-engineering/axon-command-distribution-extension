package engineering.everest.axon;

import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class CompletableFutureFactoryTest {

    @Test
    void willReturnACompletableFuture() {
        CompletableFuture<String> future = new CompletableFutureFactory().create();
        assertNotNull(future);
    }
}
