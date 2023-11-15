package nebulosa.api.guiding

import nebulosa.api.sequencer.SequenceStepEvent
import nebulosa.api.sequencer.SequenceTaskletEvent

sealed interface GuidePulseEvent : SequenceTaskletEvent, SequenceStepEvent {

    override val tasklet: GuidePulseTasklet
}
