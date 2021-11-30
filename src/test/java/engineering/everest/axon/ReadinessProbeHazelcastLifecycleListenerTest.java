package engineering.everest.axon;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.LifecycleEvent;
import com.hazelcast.core.LifecycleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.availability.AvailabilityChangeEvent;
import org.springframework.context.ApplicationEventPublisher;

import static com.hazelcast.core.LifecycleEvent.LifecycleState.MERGED;
import static com.hazelcast.core.LifecycleEvent.LifecycleState.MERGE_FAILED;
import static com.hazelcast.core.LifecycleEvent.LifecycleState.MERGING;
import static com.hazelcast.core.LifecycleEvent.LifecycleState.SHUTDOWN;
import static com.hazelcast.core.LifecycleEvent.LifecycleState.SHUTTING_DOWN;
import static com.hazelcast.core.LifecycleEvent.LifecycleState.STARTED;
import static com.hazelcast.core.LifecycleEvent.LifecycleState.STARTING;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.boot.availability.LivenessState.BROKEN;
import static org.springframework.boot.availability.LivenessState.CORRECT;
import static org.springframework.boot.availability.ReadinessState.ACCEPTING_TRAFFIC;
import static org.springframework.boot.availability.ReadinessState.REFUSING_TRAFFIC;

@ExtendWith(MockitoExtension.class)
class ReadinessProbeHazelcastLifecycleListenerTest {

    private ReadinessProbeHazelcastLifecycleListener readinessProbeHazelcastLifecycleListener;

    @Captor
    private ArgumentCaptor<AvailabilityChangeEvent> captor;

    @Mock
    private HazelcastInstance hazelcastInstance;
    @Mock
    private LifecycleService lifecycleService;
    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @BeforeEach
    void setUp() {
        when(hazelcastInstance.getLifecycleService()).thenReturn(lifecycleService);
        readinessProbeHazelcastLifecycleListener =
            new ReadinessProbeHazelcastLifecycleListener(hazelcastInstance, applicationEventPublisher);
    }

    @Test
    void constructorWillSubscribeProbeToHazelcastLifeCycleEvents() {
        verify(lifecycleService).addLifecycleListener(readinessProbeHazelcastLifecycleListener);
    }

    @Test
    void probeWillReturnCorrectStateAndAcceptingTraffic_WhenHazelcastStateBecomesStarted() {
        readinessProbeHazelcastLifecycleListener.stateChanged(new LifecycleEvent(STARTED));

        verify(applicationEventPublisher, times(2)).publishEvent(captor.capture());

        var events = captor.getAllValues();
        assertEquals(CORRECT, events.get(0).getState());
        assertEquals(readinessProbeHazelcastLifecycleListener, events.get(0).getSource());
        assertEquals(ACCEPTING_TRAFFIC, events.get(1).getState());
        assertEquals(readinessProbeHazelcastLifecycleListener, events.get(1).getSource());
    }

    @Test
    void probeWillReturnCorrectStateAndAcceptingTraffic_WhenHazelcastStateBecomesMerged() {
        readinessProbeHazelcastLifecycleListener.stateChanged(new LifecycleEvent(MERGED));

        verify(applicationEventPublisher, times(2)).publishEvent(captor.capture());

        var events = captor.getAllValues();
        assertEquals(CORRECT, events.get(0).getState());
        assertEquals(readinessProbeHazelcastLifecycleListener, events.get(0).getSource());
        assertEquals(ACCEPTING_TRAFFIC, events.get(1).getState());
        assertEquals(readinessProbeHazelcastLifecycleListener, events.get(1).getSource());
    }

    @Test
    void probeWillReturnCorrectStateAndRejectingTraffic_WhenHazelcastStateBecomesMergeFailed() {
        readinessProbeHazelcastLifecycleListener.stateChanged(new LifecycleEvent(MERGE_FAILED));

        verify(applicationEventPublisher).publishEvent(captor.capture());

        var events = captor.getAllValues();
        assertEquals(REFUSING_TRAFFIC, events.get(0).getState());
        assertEquals(readinessProbeHazelcastLifecycleListener, events.get(0).getSource());
    }

    @Test
    void probeWillReturnCorrectStateAndRefusingTraffic_WhenHazelcastStateBecomesMerging() {
        readinessProbeHazelcastLifecycleListener.stateChanged(new LifecycleEvent(MERGING));

        verify(applicationEventPublisher).publishEvent(captor.capture());

        var events = captor.getAllValues();
        assertEquals(REFUSING_TRAFFIC, events.get(0).getState());
        assertEquals(readinessProbeHazelcastLifecycleListener, events.get(0).getSource());
    }

    @Test
    void probeWillReturnCorrectStateAndRefusingTraffic_WhenHazelcastIsStarting() {
        readinessProbeHazelcastLifecycleListener.stateChanged(new LifecycleEvent(STARTING));

        verify(applicationEventPublisher).publishEvent(captor.capture());

        var events = captor.getAllValues();
        assertEquals(REFUSING_TRAFFIC, events.get(0).getState());
        assertEquals(readinessProbeHazelcastLifecycleListener, events.get(0).getSource());
    }

    @Test
    void probeWillReturnBrokenStateAndRefusingTraffic_WhenHazelcastIsShuttingDown() {
        readinessProbeHazelcastLifecycleListener.stateChanged(new LifecycleEvent(SHUTTING_DOWN));

        verify(applicationEventPublisher, times(2)).publishEvent(captor.capture());

        var events = captor.getAllValues();
        assertEquals(BROKEN, events.get(0).getState());
        assertEquals(readinessProbeHazelcastLifecycleListener, events.get(0).getSource());
        assertEquals(REFUSING_TRAFFIC, events.get(1).getState());
        assertEquals(readinessProbeHazelcastLifecycleListener, events.get(1).getSource());
    }

    @Test
    void probeWillReturnBrokenStateAndRefusingTraffic_WhenHazelcastHasShutdown() {
        readinessProbeHazelcastLifecycleListener.stateChanged(new LifecycleEvent(SHUTDOWN));

        verify(applicationEventPublisher, times(2)).publishEvent(captor.capture());

        var events = captor.getAllValues();
        assertEquals(BROKEN, events.get(0).getState());
        assertEquals(readinessProbeHazelcastLifecycleListener, events.get(0).getSource());
        assertEquals(REFUSING_TRAFFIC, events.get(1).getState());
        assertEquals(readinessProbeHazelcastLifecycleListener, events.get(1).getSource());
    }
}
