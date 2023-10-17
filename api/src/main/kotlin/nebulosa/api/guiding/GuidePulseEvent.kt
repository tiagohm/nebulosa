package nebulosa.api.guiding

import nebulosa.api.sequencer.SequenceStepEvent
import nebulosa.api.sequencer.SequenceTaskletEvent
import nebulosa.api.services.MessageEvent

sealed interface GuidePulseEvent : MessageEvent, SequenceTaskletEvent, SequenceStepEvent {

    override val tasklet: GuidePulseTasklet
}
