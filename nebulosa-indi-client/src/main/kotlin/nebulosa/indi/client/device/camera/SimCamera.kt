package nebulosa.indi.client.device.camera

import nebulosa.indi.client.INDIClient
import nebulosa.indi.protocol.INDIProtocol
import nebulosa.indi.protocol.NumberVector

internal class SimCamera(
    provider: INDIClient,
    name: String,
) : CameraDevice(provider, name) {

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
