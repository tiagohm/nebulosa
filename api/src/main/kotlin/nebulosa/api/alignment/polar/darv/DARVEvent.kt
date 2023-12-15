package nebulosa.api.alignment.polar.darv

import nebulosa.api.messages.MessageEvent
import nebulosa.guiding.GuideDirection
import nebulosa.indi.device.camera.Camera
import nebulosa.indi.device.guide.GuideOutput
import java.time.Duration

sealed interface DARVEvent : MessageEvent {

    val camera: Camera

    val guideOutput: GuideOutput

    val remainingTime: Duration

    val progress: Double

    val direction: GuideDirection?

    val state: DARVState

    override val eventName
        get() = "DARV_POLAR_ALIGNMENT_ELAPSED"
}
