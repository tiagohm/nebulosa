package nebulosa.indi.client.device

import nebulosa.indi.device.filterwheel.*
import nebulosa.indi.protocol.DefNumberVector
import nebulosa.indi.protocol.INDIProtocol
import nebulosa.indi.protocol.NumberVector
import nebulosa.indi.protocol.PropertyState

internal open class FilterWheelDevice(
    handler: DeviceProtocolHandler,
    name: String,
) : AbstractDevice(handler, name), FilterWheel {

    override var count = 0
    override var position = -1
    override var moving = false

    override fun handleMessage(message: INDIProtocol) {
        when (message) {
            is NumberVector<*> -> {
                when (message.name) {
                    "FILTER_SLOT" -> {
                        val slot = message["FILTER_SLOT_VALUE"]!!

                        if (message is DefNumberVector) {
                            count = slot.max.toInt() - slot.min.toInt() + 1
                            handler.fireOnEventReceived(FilterWheelCountChanged(this))
                        }

                        if (message.state == PropertyState.ALERT) {
                            handler.fireOnEventReceived(FilterWheelMoveFailed(this))
                        }

                        val prevPosition = position
                        position = slot.value.toInt()

                        if (prevPosition != position) {
                            handler.fireOnEventReceived(FilterWheelPositionChanged(this, prevPosition))
                        }

                        val prevIsMoving = moving
                        moving = message.isBusy

                        if (prevIsMoving != moving) {
                            handler.fireOnEventReceived(FilterWheelMovingChanged(this))
                        }
                    }
                }
            }
            else -> Unit
        }

        super.handleMessage(message)
    }

    override fun moveTo(position: Int) {
        if (position in 1..count) {
            sendNewNumber("FILTER_SLOT", "FILTER_SLOT_VALUE" to position.toDouble())
        }
    }

    override fun syncNames(names: Iterable<String>) {
        sendNewText("FILTER_NAME", names.mapIndexed { i, name -> "FILTER_SLOT_NAME_${i + 1}" to name })
    }

    override fun close() = Unit

    override fun toString(): String {
        return "FilterWheel(name=$name, slotCount=$count, position=$position," +
                " moving=$moving)"
    }
}
