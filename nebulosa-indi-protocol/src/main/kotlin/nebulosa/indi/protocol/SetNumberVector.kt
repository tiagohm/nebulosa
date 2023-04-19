package nebulosa.indi.protocol

import java.io.PrintStream

@Suppress("CanSealedSubClassBeObject")
class SetNumberVector : SetVector<OneNumber>(), NumberVector<OneNumber> {

    override fun writeTo(stream: PrintStream) = stream.writeXML(
        "setNumberVector", elements,
        "device", device,
        "name", name,
        "state", state,
        "timeout", timeout,
        "timestamp", timestamp,
        "message", message,
    )
}
