package nebulosa.indi.client.device.camera

import nebulosa.indi.client.INDIClient
import nebulosa.indi.protocol.INDIProtocol
import nebulosa.indi.protocol.NumberVector

internal class SVBonyCamera(
    provider: INDIClient,
    name: String,
) : CameraDevice(provider, name) {

    @Volatile private var legacyProperties = false

    override fun handleMessage(message: INDIProtocol) {
        when (message) {
            is NumberVector<*> -> {
                when (message.name) {
                    "CCD_GAIN" -> {
                        legacyProperties = true
                        processGain(message, message["GAIN"]!!)
                    }
                    "CCD_OFFSET" -> {
                        legacyProperties = true
                        processOffset(message, message["OFFSET"]!!)
                    }
                    "CCD_CONTROLS" -> {
                        if ("Gain" in message) {
                            legacyProperties = false
                            processGain(message, message["Gain"]!!)
                        }
                        if ("Offset" in message) {
                            legacyProperties = false
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
        if (legacyProperties) sendNewNumber("CCD_GAIN", "GAIN" to value.toDouble())
        else sendNewNumber("CCD_CONTROLS", "Gain" to value.toDouble())
    }

    override fun offset(value: Int) {
        if (legacyProperties) sendNewNumber("CCD_OFFSET", "OFFSET" to value.toDouble())
        else sendNewNumber("CCD_CONTROLS", "Offset" to value.toDouble())
    }
}
