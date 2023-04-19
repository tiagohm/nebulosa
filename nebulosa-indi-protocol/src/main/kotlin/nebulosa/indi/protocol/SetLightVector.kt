package nebulosa.indi.protocol

import java.io.PrintStream

@Suppress("CanSealedSubClassBeObject")
class SetLightVector : SetVector<OneLight>(), LightVector<OneLight> {

    override fun writeTo(stream: PrintStream) = stream.writeXML(
        "setLightVector", elements,
        "device", device,
        "name", name,
        "state", state,
        "timestamp", timestamp,
        "message", message,
    )
}
