package nebulosa.indi.client.device.wheels

import nebulosa.indi.client.INDIClient
import nebulosa.indi.client.device.INDIDevice
import nebulosa.indi.device.filterwheel.*
import nebulosa.indi.protocol.*

internal open class INDIFilterWheel(
    override val sender: INDIClient,
    override val name: String,
) : INDIDevice(), FilterWheel {

    @Volatile final override var count = 0
        private set
    @Volatile final override var position = 0
        private set
    @Volatile final override var moving = false
        private set

    final override val names = ArrayList<String>(12)

    override fun handleMessage(message: INDIProtocol) {
        when (message) {
            is NumberVector<*> -> {
                when (message.name) {
                    "FILTER_SLOT" -> {
                        val slot = message["FILTER_SLOT_VALUE"]!!

                        if (message is DefNumberVector) {
                            count = slot.max.toInt() - slot.min.toInt() + 1
                            sender.fireOnEventReceived(FilterWheelCountChanged(this))
                        }

                        if (message.state == PropertyState.ALERT) {
                            sender.fireOnEventReceived(FilterWheelMoveFailed(this))
                        }

                        val prevPosition = position
                        position = slot.value.toInt()

                        if (prevPosition != position) {
                            sender.fireOnEventReceived(FilterWheelPositionChanged(this, prevPosition))
                        }

                        val prevIsMoving = moving
                        moving = message.isBusy

                        if (prevIsMoving != moving) {
                            sender.fireOnEventReceived(FilterWheelMovingChanged(this))
                        }
                    }
                }
            }
            is TextVector<*> -> {
                when (message.name) {
                    "FILTER_NAME" -> {
                        names.clear()

                        repeat(16) {
                            val key = "FILTER_SLOT_NAME_${it + 1}"

                            if (key in message) {
                                names.add(message[key]!!.value)
                            }
                        }

                        sender.fireOnEventReceived(FilterWheelNamesChanged(this))
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

    override fun names(names: Iterable<String>) {
        sendNewText("FILTER_NAME", names.mapIndexed { i, name -> "FILTER_SLOT_NAME_${i + 1}" to name })
    }

    override fun close() = Unit

    override fun toString(): String {
        return "FilterWheel(name=$name, slotCount=$count, position=$position," +
            " moving=$moving)"
    }
}
