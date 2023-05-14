package nebulosa.indi.client.device.mount

import nebulosa.indi.client.device.DeviceProtocolHandler
import nebulosa.indi.device.MessageSender
import nebulosa.indi.device.mount.MountCanHomeChanged
import nebulosa.indi.protocol.INDIProtocol
import nebulosa.indi.protocol.SwitchVector

internal class IoptronV3Mount(
    sender: MessageSender,
    handler: DeviceProtocolHandler,
    name: String,
) : MountDevice(sender, handler, name) {

    override fun handleMessage(message: INDIProtocol) {
        when (message) {
            is SwitchVector<*> -> {
                when (message.name) {
                    "HOME" -> {
                        canHome = true
                        handler.fireOnEventReceived(MountCanHomeChanged(this))
                    }
                }
            }
            else -> Unit
        }

        super.handleMessage(message)
    }

    override fun home() {
        sendNewSwitch("HOME", "GoToHome" to true)
    }
}
