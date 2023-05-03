package nebulosa.indi.protocol

import java.io.PrintStream

@Suppress("CanSealedSubClassBeObject")
class DefBLOBVector : DefVector<DefBLOB>(), BLOBVector<DefBLOB> {

    override fun writeTo(stream: PrintStream) = stream.writeXML(
        "defBLOBVector", elements,
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
