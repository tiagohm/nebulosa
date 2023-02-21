package nebulosa.indi.client.device.camera

import nebulosa.indi.client.device.DeviceProtocolHandler
import nebulosa.indi.device.MessageSender
import nebulosa.indi.device.camera.*
import nebulosa.indi.protocol.DefNumberVector
import nebulosa.indi.protocol.INDIProtocol
import nebulosa.indi.protocol.NumberVector
import nebulosa.indi.protocol.SwitchVector

internal class AsiCamera(
    sender: MessageSender,
    handler: DeviceProtocolHandler,
    name: String,
) : CameraDevice(sender, handler, name) {

    override fun handleMessage(message: INDIProtocol) {
        when (message) {
            is SwitchVector<*> -> {
                when (message.name) {
                    "CCD_COOLER" -> {
                        hasCoolerControl = true
                        cooler = message["COOLER_ON"]?.value ?: false

                        handler.fireOnEventReceived(CameraCoolerControlChanged(this))
                        handler.fireOnEventReceived(CameraCoolerChanged(this))
                    }
                }
            }
            is NumberVector<*> -> {
                when (message.name) {
                    "CCD_CONTROLS" -> {
                        if ("Gain" in message) {
                            val element = message["Gain"]!!

                            if (message is DefNumberVector) {
                                gainMin = element.min.toInt()
                                gainMax = element.max.toInt()

                                handler.fireOnEventReceived(CameraGainMinMaxChanged(this))
                            }

                            gain = element.value.toInt()

                            handler.fireOnEventReceived(CameraGainChanged(this))
                        }
                        if ("Offset" in message) {
                            val element = message["Offset"]!!

                            if (message is DefNumberVector) {
                                gainMin = element.min.toInt()
                                gainMax = element.max.toInt()

                                handler.fireOnEventReceived(CameraOffsetMinMaxChanged(this))
                            }

                            gain = element.value.toInt()

                            handler.fireOnEventReceived(CameraOffsetChanged(this))
                        }
                    }
                }
            }
            else -> Unit
        }

        super.handleMessage(message)
    }

    override fun gain(value: Int) {
        sendNewNumber("CCD_CONTROLS", "Gain" to value.toDouble())
    }

    override fun offset(value: Int) {
        sendNewNumber("CCD_CONTROLS", "Offset" to value.toDouble())
    }
}
