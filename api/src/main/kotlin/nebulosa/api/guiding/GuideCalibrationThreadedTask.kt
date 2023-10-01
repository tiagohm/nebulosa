package nebulosa.api.guiding

import nebulosa.api.beans.annotations.ThreadedTask
import org.springframework.stereotype.Component

@Component
@ThreadedTask
class GuideCalibrationThreadedTask(
    private val guideCalibrationRepository: GuideCalibrationRepository,
) : Runnable {

    override fun run() {
    }
}
