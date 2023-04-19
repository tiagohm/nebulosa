package nebulosa.indi.protocol

import java.io.PrintStream

@Suppress("CanSealedSubClassBeObject")
class DefTextVector : DefVector<DefText>(), TextVector<DefText> {

    override fun writeTo(stream: PrintStream) = stream.writeXML(
        "defTextVector", elements,
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
