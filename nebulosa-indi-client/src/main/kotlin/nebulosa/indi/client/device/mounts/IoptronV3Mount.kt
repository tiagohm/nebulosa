package nebulosa.indi.client.device.mounts

import nebulosa.indi.client.INDIClient
import nebulosa.indi.device.mount.MountCanHomeChanged
import nebulosa.indi.protocol.INDIProtocol
import nebulosa.indi.protocol.SwitchVector

internal class IoptronV3Mount(
    provider: INDIClient,
    name: String,
) : INDIMount(provider, name) {

    override fun handleMessage(message: INDIProtocol) {
        when (message) {
            is SwitchVector<*> -> {
                when (message.name) {
                    "HOME" -> {
                        canHome = true
                        sender.fireOnEventReceived(MountCanHomeChanged(this))
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
