package nebulosa.indi.protocol

import java.io.PrintStream

@Suppress("CanSealedSubClassBeObject")
class NewTextVector : NewVector<OneText>(), TextVector<OneText> {

    override fun writeTo(stream: PrintStream) = stream.writeXML(
        "newTextVector", elements,
        "device", device,
        "name", name,
        "timestamp", timestamp,
    )
}
