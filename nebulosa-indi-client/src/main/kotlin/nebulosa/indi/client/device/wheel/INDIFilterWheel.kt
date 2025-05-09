package nebulosa.indi.client.device.wheel

import nebulosa.indi.client.INDIClient
import nebulosa.indi.client.device.INDIDriverInfo
import nebulosa.indi.client.device.INDIDevice
import nebulosa.indi.device.filterwheel.FilterWheel
import nebulosa.indi.device.filterwheel.FilterWheelCountChanged
import nebulosa.indi.device.filterwheel.FilterWheelMoveFailed
import nebulosa.indi.device.filterwheel.FilterWheelMovingChanged
import nebulosa.indi.device.filterwheel.FilterWheelNamesChanged
import nebulosa.indi.device.filterwheel.FilterWheelPositionChanged
import nebulosa.indi.protocol.DefNumberVector
import nebulosa.indi.protocol.INDIProtocol
import nebulosa.indi.protocol.NumberVector
import nebulosa.indi.protocol.PropertyState
import nebulosa.indi.protocol.TextVector
import nebulosa.indi.protocol.Vector.Companion.isBusy

// https://github.com/indilib/indi/blob/master/libs/indibase/indifilterwheel.cpp

internal open class INDIFilterWheel(
    final override val sender: INDIClient,
    final override val driver: INDIDriverInfo,
) : INDIDevice(), FilterWheel {

    @Volatile final override var count = 0
    @Volatile final override var position = 0
    @Volatile final override var moving = false

    final override val names = ArrayList<String>(8)

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
                            sender.fireOnEventReceived(FilterWheelPositionChanged(this))
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

                        for (i in 1..16) {
                            message["FILTER_SLOT_NAME_$i"]?.value?.also(names::add) ?: break
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

    override fun toString() = "FilterWheel(name=$name, connected=$connected, count=$count, position=$position, moving=$moving, names=$names)"
}
