package nebulosa.indi.protocol

import java.io.PrintStream

@Suppress("CanSealedSubClassBeObject")
class DefLightVector : DefVector<DefLight>(), LightVector<DefLight> {

    override fun writeTo(stream: PrintStream) = stream.writeXML(
        "defLightVector", elements,
        "device", device,
        "name", name,
        "label", label,
        "group", group,
        "state", state,
        "timestamp", timestamp,
        "message", message,
    )
}
