package engineering.everest.starterkit.axon;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.LifecycleEvent;
import com.hazelcast.core.LifecycleListener;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.Set;

import static com.hazelcast.core.LifecycleEvent.LifecycleState.MERGED;
import static com.hazelcast.core.LifecycleEvent.LifecycleState.MERGE_FAILED;
import static com.hazelcast.core.LifecycleEvent.LifecycleState.MERGING;
import static com.hazelcast.core.LifecycleEvent.LifecycleState.SHUTDOWN;
import static com.hazelcast.core.LifecycleEvent.LifecycleState.SHUTTING_DOWN;
import static com.hazelcast.core.LifecycleEvent.LifecycleState.STARTED;
import static com.hazelcast.core.LifecycleEvent.LifecycleState.STARTING;
import static org.springframework.boot.availability.AvailabilityChangeEvent.publish;
import static org.springframework.boot.availability.LivenessState.BROKEN;
import static org.springframework.boot.availability.LivenessState.CORRECT;
import static org.springframework.boot.availability.ReadinessState.ACCEPTING_TRAFFIC;
import static org.springframework.boot.availability.ReadinessState.REFUSING_TRAFFIC;

/**
 * Spring availability state publisher that listens to Hazelcast lifecycle events to determine if an
 * application instance is ready to receive and process API requests.
 */
@Component
@Log4j2
public class ReadinessProbeHazelcastLifecycleListener implements LifecycleListener {
    public static final Set<LifecycleEvent.LifecycleState> LIVE_AND_READY_STATES = Set.of(STARTED, MERGED);
    public static final Set<LifecycleEvent.LifecycleState> LIVE_BUT_NOT_READY_STATES = Set.of(MERGE_FAILED, MERGING, STARTING);
    public static final Set<LifecycleEvent.LifecycleState> DEAD_STATES = Set.of(SHUTTING_DOWN, SHUTDOWN);

    private final ApplicationEventPublisher applicationEventPublisher;

    public ReadinessProbeHazelcastLifecycleListener(HazelcastInstance hazelcastInstance, ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
        hazelcastInstance.getLifecycleService().addLifecycleListener(this);
    }

    @Override
    public void stateChanged(LifecycleEvent event) {
        if (LIVE_AND_READY_STATES.contains(event.getState())) {
            LOGGER.debug("Hazelcast transitioned to {} state; this node is now accepting traffic", event.getState());
            publish(applicationEventPublisher, this, CORRECT);
            publish(applicationEventPublisher, this, ACCEPTING_TRAFFIC);
        } else if (LIVE_BUT_NOT_READY_STATES.contains(event.getState())) {
            LOGGER.debug("Hazelcast transitioned to {} state; this node is refusing traffic", event.getState());
            publish(applicationEventPublisher, this, REFUSING_TRAFFIC);
        } else if (DEAD_STATES.contains(event.getState())) {
            LOGGER.debug("Hazelcast transitioned to {} state; this node is now broken and refusing traffic", event.getState());
            publish(applicationEventPublisher, this, BROKEN);
            publish(applicationEventPublisher, this, REFUSING_TRAFFIC);
        }
    }
}
