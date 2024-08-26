package nebulosa.indi.client.device.auxiliary

import nebulosa.indi.client.INDIClient
import nebulosa.indi.client.device.DriverInfo
import nebulosa.indi.client.device.INDIDevice
import nebulosa.indi.client.device.handler.INDIGuideOutputHandler
import nebulosa.indi.device.DeviceType
import nebulosa.indi.device.guider.GuideOutput
import nebulosa.indi.protocol.INDIProtocol
import java.time.Duration

internal open class INDIGuideOutput(
    final override val sender: INDIClient,
    final override val driverInfo: DriverInfo,
) : INDIDevice(), GuideOutput {

    override val type
        get() = DeviceType.GUIDE_OUTPUT

    private val guideOutput = INDIGuideOutputHandler(this)

    final override val canPulseGuide
        get() = guideOutput.canPulseGuide

    final override val pulseGuiding
        get() = guideOutput.pulseGuiding

    override fun handleMessage(message: INDIProtocol) {
        guideOutput.handleMessage(message)
        super.handleMessage(message)
    }

    override fun guideNorth(duration: Duration) {
        guideOutput.guideNorth(duration)
    }

    override fun guideSouth(duration: Duration) {
        guideOutput.guideSouth(duration)
    }

    override fun guideEast(duration: Duration) {
        guideOutput.guideEast(duration)
    }

    override fun guideWest(duration: Duration) {
        guideOutput.guideWest(duration)
    }

    override fun close() = Unit

    override fun toString() = "GuideOutput(name=$name, connected=$connected, canPulseGuide=$canPulseGuide," +
            " pulseGuiding=$pulseGuiding)"
}
