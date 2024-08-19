package nebulosa.indi.client.device.handler

import nebulosa.indi.device.INDIDeviceProvider
import nebulosa.indi.device.guider.GuideOutput
import nebulosa.indi.device.guider.GuideOutputCanPulseGuideChanged
import nebulosa.indi.device.guider.GuideOutputPulsingChanged
import nebulosa.indi.protocol.DefNumberVector
import nebulosa.indi.protocol.INDIProtocol
import nebulosa.indi.protocol.NumberVector
import nebulosa.indi.protocol.Vector.Companion.isBusy
import nebulosa.indi.protocol.parser.INDIProtocolHandler
import java.time.Duration

data class INDIGuideOutputHandler(private val device: GuideOutput) : INDIProtocolHandler {

    private val sender = device.sender as INDIDeviceProvider

    @Volatile var canPulseGuide = false
        private set

    @Volatile var pulseGuiding = false
        private set

    override fun handleMessage(message: INDIProtocol) {
        when (message) {
            is NumberVector<*> -> {
                when (message.name) {
                    "TELESCOPE_TIMED_GUIDE_NS",
                    "TELESCOPE_TIMED_GUIDE_WE" -> {
                        if (!canPulseGuide && message is DefNumberVector) {
                            canPulseGuide = true
                            sender.fireOnEventReceived(GuideOutputCanPulseGuideChanged(device))
                        }

                        if (canPulseGuide) {
                            val prevIsPulseGuiding = pulseGuiding
                            pulseGuiding = message.isBusy

                            if (pulseGuiding != prevIsPulseGuiding) {
                                sender.fireOnEventReceived(GuideOutputPulsingChanged(device))
                            }
                        }
                    }
                }
            }
            else -> Unit
        }
    }

    fun guideNorth(duration: Duration) {
        if (canPulseGuide) {
            device.sendNewNumber("TELESCOPE_TIMED_GUIDE_NS", "TIMED_GUIDE_N" to duration.toMillis().toDouble(), "TIMED_GUIDE_S" to 0.0)
        }
    }

    fun guideSouth(duration: Duration) {
        if (canPulseGuide) {
            device.sendNewNumber("TELESCOPE_TIMED_GUIDE_NS", "TIMED_GUIDE_S" to duration.toMillis().toDouble(), "TIMED_GUIDE_N" to 0.0)
        }
    }

    fun guideEast(duration: Duration) {
        if (canPulseGuide) {
            device.sendNewNumber("TELESCOPE_TIMED_GUIDE_WE", "TIMED_GUIDE_E" to duration.toMillis().toDouble(), "TIMED_GUIDE_W" to 0.0)
        }
    }

    fun guideWest(duration: Duration) {
        if (canPulseGuide) {
            device.sendNewNumber("TELESCOPE_TIMED_GUIDE_WE", "TIMED_GUIDE_W" to duration.toMillis().toDouble(), "TIMED_GUIDE_E" to 0.0)
        }
    }
}
