package nebulosa.api.alignment.polar.darv

import nebulosa.api.sequencer.SequenceJobEvent
import nebulosa.indi.device.camera.Camera
import nebulosa.indi.device.guide.GuideOutput

sealed interface DARVPolarAlignmentEvent : SequenceJobEvent {

    val camera: Camera

    val guideOutput: GuideOutput

    val state: DARVPolarAlignmentState
}
