package nebulosa.indi.devices.filterwheels

import nebulosa.indi.INDIClient
import nebulosa.indi.devices.AbstractDevice
import nebulosa.indi.devices.DeviceProtocolHandler
import nebulosa.indi.protocol.DefNumberVector
import nebulosa.indi.protocol.INDIProtocol
import nebulosa.indi.protocol.NumberVector
import nebulosa.indi.protocol.PropertyState

internal open class FilterWheelBase(
    client: INDIClient,
    handler: DeviceProtocolHandler,
    name: String,
) : AbstractDevice(client, handler, name), FilterWheel {

    override var slotCount = 0
    override var position = -1
    override var isMoving = false

    override fun handleMessage(message: INDIProtocol) {
        when (message) {
            is NumberVector<*> -> {
                when (message.name) {
                    "FILTER_SLOT" -> {
                        val slotValue = message["FILTER_SLOT_VALUE"]!!

                        if (message is DefNumberVector) {
                            slotCount = slotValue.max.toInt() - slotValue.min.toInt() + 1
                            handler.fireOnEventReceived(FilterWheelSlotCountChanged(this))
                        } else {
                            isMoving = message.state == PropertyState.BUSY

                            handler.fireOnEventReceived(FilterWheelIsMoving(this))
                        }

                        val position = slotValue.value.toInt()
                        val previous = this.position
                        this.position = position

                        handler.fireOnEventReceived(FilterWheelPositionChanged(this, previous))
                    }
                }
            }
            else -> Unit
        }

        super.handleMessage(message)
    }

    override fun moveTo(slot: Int) {
        if (slot in 1..slotCount) {
            sendNewNumber("FILTER_SLOT", "FILTER_SLOT_VALUE" to slot.toDouble())
        }
    }

    override fun close() {}
}
