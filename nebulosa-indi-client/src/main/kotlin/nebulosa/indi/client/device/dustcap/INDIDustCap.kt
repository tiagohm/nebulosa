package nebulosa.indi.client.device.dustcap

import nebulosa.indi.client.INDIClient
import nebulosa.indi.client.device.INDIDriverInfo
import nebulosa.indi.client.device.INDIDevice
import nebulosa.indi.device.dustcap.DustCap
import nebulosa.indi.device.dustcap.DustCapCanParkChanged
import nebulosa.indi.device.dustcap.DustCapParkChanged
import nebulosa.indi.device.firstOnSwitchOrNull
import nebulosa.indi.protocol.DefSwitchVector
import nebulosa.indi.protocol.DefVector.Companion.isNotReadOnly
import nebulosa.indi.protocol.INDIProtocol
import nebulosa.indi.protocol.SwitchVector
import nebulosa.indi.protocol.Vector.Companion.isBusy

internal open class INDIDustCap(
    override val sender: INDIClient,
    override val driver: INDIDriverInfo,
) : INDIDevice(), DustCap {

    @Volatile final override var canPark = false
    @Volatile final override var parking = false
    @Volatile final override var parked = false

    override fun handleMessage(message: INDIProtocol) {
        when (message) {
            is SwitchVector<*> -> {
                when (message.name) {
                    "CAP_PARK" -> {
                        if (message is DefSwitchVector) {
                            canPark = message.isNotReadOnly

                            sender.fireOnEventReceived(DustCapCanParkChanged(this))
                        }

                        parking = message.isBusy
                        parked = message.firstOnSwitchOrNull()?.name == "PARK"

                        sender.fireOnEventReceived(DustCapParkChanged(this))
                    }
                }
            }
            else -> Unit
        }

        super.handleMessage(message)
    }

    override fun park() {
        if (canPark) {
            sendNewSwitch("CAP_PARK", "PARK" to true)
        }
    }

    override fun unpark() {
        if (canPark) {
            sendNewSwitch("CAP_PARK", "UNPARK" to true)
        }
    }

    override fun close() = Unit

    override fun toString() = "DustCap(name=$name, connected=$connected, canPark=$canPark, parking=$parking, parked=$parked)"
}
