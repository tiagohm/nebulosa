package nebulosa.indi.protocol

import java.io.PrintStream

@Suppress("CanSealedSubClassBeObject")
class SetBLOBVector : SetVector<OneBLOB>(), BLOBVector<OneBLOB> {

    override fun writeTo(stream: PrintStream) = stream.writeXML(
        "setBLOBVector", elements,
        "device", device,
        "name", name,
        "state", state,
        "timeout", timeout,
        "timestamp", timestamp,
        "message", message,
    )
}
