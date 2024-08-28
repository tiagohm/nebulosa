package nebulosa.indi.client.device.lightbox

import nebulosa.indi.client.INDIClient
import nebulosa.indi.client.device.DriverInfo
import nebulosa.indi.client.device.INDIDevice
import nebulosa.indi.device.lightbox.LightBox
import nebulosa.indi.device.lightbox.LightBoxEnabledChanged
import nebulosa.indi.device.lightbox.LightBoxIntensityChanged
import nebulosa.indi.device.lightbox.LightBoxIntensityMinMaxChanged
import nebulosa.indi.protocol.DefNumberVector
import nebulosa.indi.protocol.INDIProtocol
import nebulosa.indi.protocol.NumberVector
import nebulosa.indi.protocol.SwitchVector

internal open class INDILightBox(
    override val sender: INDIClient,
    override val driverInfo: DriverInfo,
) : INDIDevice(), LightBox {

    @Volatile final override var enabled = false
    @Volatile final override var intensity = 0.0
    @Volatile final override var intensityMax = 0.0
    @Volatile final override var intensityMin = 0.0

    override fun handleMessage(message: INDIProtocol) {
        when (message) {
            is SwitchVector<*> -> {
                when (message.name) {
                    "FLAT_LIGHT_CONTROL" -> {
                        enabled = message["FLAT_LIGHT_ON"]?.value == true

                        sender.fireOnEventReceived(LightBoxEnabledChanged(this))
                    }
                }
            }
            is NumberVector<*> -> {
                when (message.name) {
                    "FLAT_LIGHT_INTENSITY" -> {
                        val element = message["FLAT_LIGHT_INTENSITY_VALUE"]!!

                        if (message is DefNumberVector) {
                            intensityMin = element.min
                            intensityMax = element.max

                            sender.fireOnEventReceived(LightBoxIntensityMinMaxChanged(this))
                        }

                        intensity = element.value

                        sender.fireOnEventReceived(LightBoxIntensityChanged(this))
                    }
                }
            }
            else -> Unit
        }

        super.handleMessage(message)
    }

    override fun enable() {
        sendNewSwitch("FLAT_LIGHT_CONTROL", "FLAT_LIGHT_ON" to true)
    }

    override fun disable() {
        sendNewSwitch("FLAT_LIGHT_CONTROL", "FLAT_LIGHT_OFF" to true)
    }

    override fun brightness(intensity: Double) {
        if (enabled) {
            sendNewNumber("FLAT_LIGHT_INTENSITY", "FLAT_LIGHT_INTENSITY_VALUE" to intensity)
        }
    }

    override fun close() = Unit

    override fun toString() = "LightBox(name=$name, connected=$connected, enabled=$enabled," +
            " intensityMin=$intensityMin, intensityMax=$intensityMax, intensity=$intensity)"
}
