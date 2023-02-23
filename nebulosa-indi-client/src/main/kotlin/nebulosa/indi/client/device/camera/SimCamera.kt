package nebulosa.indi.client.device.camera

import nebulosa.indi.client.device.DeviceProtocolHandler
import nebulosa.indi.device.MessageSender
import nebulosa.indi.device.camera.CameraGainChanged
import nebulosa.indi.device.camera.CameraGainMinMaxChanged
import nebulosa.indi.protocol.DefNumberVector
import nebulosa.indi.protocol.INDIProtocol
import nebulosa.indi.protocol.NumberVector

internal class SimCamera(
    sender: MessageSender,
    handler: DeviceProtocolHandler,
    name: String,
) : CameraDevice(sender, handler, name) {

    override fun handleMessage(message: INDIProtocol) {
        when (message) {
            is NumberVector<*> -> {
                when (message.name) {
                    "CCD_GAIN" -> {
                        val element = message["GAIN"]!!

                        if (message is DefNumberVector) {
                            gainMin = element.min.toInt()
                            gainMax = element.max.toInt()

                            handler.fireOnEventReceived(CameraGainMinMaxChanged(this))
                        }

                        gain = element.value.toInt()

                        handler.fireOnEventReceived(CameraGainChanged(this))
                    }
                    "CCD_OFFSET" -> {
                        val element = message["OFFSET"]!!

                        if (message is DefNumberVector) {
                            offsetMin = element.min.toInt()
                            offsetMax = element.max.toInt()

                            handler.fireOnEventReceived(CameraGainMinMaxChanged(this))
                        }

                        offset = element.value.toInt()

                        handler.fireOnEventReceived(CameraGainChanged(this))
                    }
                }
            }
            else -> Unit
        }

        super.handleMessage(message)
    }

    override fun cooler(enable: Boolean) {
        if (hasCoolerControl && cooler != enable) {
            sendNewSwitch("CCD_COOLER", (if (enable) "COOLER_ON" else "COOLER_OFF") to true)
        }
    }

    override fun gain(value: Int) {
        sendNewNumber("CCD_GAIN", "GAIN" to value.toDouble())
    }

    override fun offset(value: Int) {
        sendNewNumber("CCD_OFFSET", "OFFSET" to value.toDouble())
    }
}
