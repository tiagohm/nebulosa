package nebulosa.indi.protocol

import java.io.PrintStream

@Suppress("CanSealedSubClassBeObject")
class DefNumberVector : DefVector<DefNumber>(), NumberVector<DefNumber> {

    override fun writeTo(stream: PrintStream) = stream.writeXML(
        "defNumberVector", elements,
        "device", device,
        "name", name,
        "label", label,
        "group", group,
        "state", state,
        "perm", perm,
        "timeout", timeout,
        "timestamp", timestamp,
        "message", message,
    )
}
