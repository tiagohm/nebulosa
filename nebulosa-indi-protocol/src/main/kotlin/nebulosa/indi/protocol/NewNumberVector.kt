package nebulosa.indi.protocol

import java.io.PrintStream

@Suppress("CanSealedSubClassBeObject")
class NewNumberVector : NewVector<OneNumber>(), NumberVector<OneNumber>, MinMaxVector<OneNumber> {

    override fun writeTo(stream: PrintStream) = stream.writeXML(
        "newNumberVector", elements,
        "device", device,
        "name", name,
        "timestamp", timestamp,
    )
}
