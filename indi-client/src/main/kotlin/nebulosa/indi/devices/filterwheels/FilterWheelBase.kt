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
                            isMoving = message.isBusy

                            handler.fireOnEventReceived(FilterWheelMovingChanged(this))
                        }

                        if (message.state == PropertyState.ALERT) {
                            handler.fireOnEventReceived(FilterWheelMoveFailed(this))
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

    override fun moveTo(position: Int) {
        if (position in 1..slotCount) {
            sendNewNumber("FILTER_SLOT", "FILTER_SLOT_VALUE" to position.toDouble())
        }
    }

    override fun filterNames(names: Iterable<String>) {
        sendNewText("FILTER_NAME", names.mapIndexed { i, name -> "FILTER_SLOT_NAME_${i + 1}" to name })
    }

    override fun close() {}

    override fun toString(): String {
        return "FilterWheel(name=$name, slotCount=$slotCount, position=$position," +
                " isMoving=$isMoving)"
    }
}
