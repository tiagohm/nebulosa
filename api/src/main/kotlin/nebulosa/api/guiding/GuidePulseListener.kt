package nebulosa.api.guiding

import nebulosa.guiding.internal.GuideDirection
import nebulosa.indi.device.guide.GuideOutput
import kotlin.time.Duration

interface GuidePulseListener {

    fun onGuidePulseStarted(guideOutput: GuideOutput, direction: GuideDirection, duration: Duration) {}

    fun onGuidePulseFinished(guideOutput: GuideOutput, direction: GuideDirection, duration: Duration) {}
}
