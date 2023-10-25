package nebulosa.indi.client.device.camera

import nebulosa.indi.client.device.DeviceProtocolHandler
import nebulosa.indi.protocol.INDIProtocol
import nebulosa.indi.protocol.NumberVector

internal class SimCamera(
    handler: DeviceProtocolHandler,
    name: String,
) : CameraDevice(handler, name) {

    override fun handleMessage(message: INDIProtocol) {
        when (message) {
            is NumberVector<*> -> {
                when (message.name) {
                    "CCD_GAIN" -> {
                        processGain(message, message["GAIN"]!!)
                    }
                    "CCD_OFFSET" -> {
                        processOffset(message, message["OFFSET"]!!)
                    }
                }
            }
            else -> Unit
        }

        super.handleMessage(message)
    }

    override fun gain(value: Int) {
        sendNewNumber("CCD_GAIN", "GAIN" to value.toDouble())
    }

    override fun offset(value: Int) {
        sendNewNumber("CCD_OFFSET", "OFFSET" to value.toDouble())
    }
}
