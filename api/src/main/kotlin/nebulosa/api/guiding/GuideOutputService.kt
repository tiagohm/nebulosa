package nebulosa.api.guiding

import nebulosa.guiding.GuideDirection
import nebulosa.indi.device.guide.GuideOutput
import org.springframework.stereotype.Service
import java.time.Duration

@Service
class GuideOutputService {

    fun connect(guideOutput: GuideOutput) {
        guideOutput.connect()
    }

    fun disconnect(guideOutput: GuideOutput) {
        guideOutput.disconnect()
    }

    fun pulse(guideOutput: GuideOutput, direction: GuideDirection, duration: Duration) {
        if (guideOutput.canPulseGuide) {
            when (direction) {
                GuideDirection.NORTH -> guideOutput.guideNorth(duration)
                GuideDirection.SOUTH -> guideOutput.guideSouth(duration)
                GuideDirection.WEST -> guideOutput.guideWest(duration)
                GuideDirection.EAST -> guideOutput.guideEast(duration)
            }
        }
    }
}
