package nebulosa.indi.protocol

import java.io.PrintStream

@Suppress("CanSealedSubClassBeObject")
class NewBLOBVector : NewVector<OneBLOB>(), BLOBVector<OneBLOB> {

    override fun writeTo(stream: PrintStream) = stream.writeXML(
        "newBLOBVector", elements,
        "device", device,
        "name", name,
        "timestamp", timestamp,
    )
}
