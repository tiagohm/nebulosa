package nebulosa.api.alignment.polar.darv

import nebulosa.indi.device.camera.Camera
import nebulosa.indi.device.guide.GuideOutput

sealed interface DARVPolarAlignmentEvent {

    val camera: Camera

    val guideOutput: GuideOutput
}
