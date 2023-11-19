package nebulosa.api.sequencer

import org.springframework.batch.core.step.tasklet.Tasklet

interface SequenceTaskletEvent {

    val tasklet: Tasklet

    val progress: Double
}
