package nebulosa.indi.protocol

import java.io.PrintStream

@Suppress("CanSealedSubClassBeObject")
class NewLightVector : NewVector<OneLight>(), LightVector<OneLight> {

    override fun writeTo(stream: PrintStream) = stream.writeXML(
        "newLightVector", elements,
        "device", device,
        "name", name,
        "timestamp", timestamp,
    )
}
