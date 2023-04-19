package nebulosa.indi.protocol

import java.io.PrintStream

@Suppress("CanSealedSubClassBeObject")
class NewSwitchVector : NewVector<OneSwitch>(), SwitchVector<OneSwitch> {

    override fun writeTo(stream: PrintStream) = stream.writeXML(
        "newSwitchVector", elements,
        "device", device,
        "name", name,
        "timestamp", timestamp,
    )
}
