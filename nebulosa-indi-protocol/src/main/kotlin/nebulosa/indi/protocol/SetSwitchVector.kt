package nebulosa.indi.protocol

import java.io.PrintStream

@Suppress("CanSealedSubClassBeObject")
class SetSwitchVector : SetVector<OneSwitch>(), SwitchVector<OneSwitch> {

    override fun writeTo(stream: PrintStream) = stream.writeXML(
        "setSwitchVector", elements,
        "device", device,
        "name", name,
        "state", state,
        "timeout", timeout,
        "timestamp", timestamp,
        "message", message,
    )
}
