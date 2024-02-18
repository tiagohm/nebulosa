package nebulosa.indi.client.device.camera

import nebulosa.indi.client.INDIClient
import nebulosa.indi.protocol.INDIProtocol
import nebulosa.indi.protocol.NumberVector

internal class AsiCamera(
    provider: INDIClient,
    name: String,
) : CameraDevice(provider, name) {

    override fun handleMessage(message: INDIProtocol) {
        when (message) {
            is NumberVector<*> -> {
                when (message.name) {
                    "CCD_CONTROLS" -> {
                        if ("Gain" in message) {
                            processGain(message, message["Gain"]!!)
                        }
                        if ("Offset" in message) {
                            processOffset(message, message["Offset"]!!)
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
