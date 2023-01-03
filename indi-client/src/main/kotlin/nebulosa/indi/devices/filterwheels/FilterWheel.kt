package nebulosa.indi.devices.filterwheels

import nebulosa.indi.INDIClient
import nebulosa.indi.devices.Device
import nebulosa.indi.devices.DeviceProtocolHandler
import nebulosa.indi.protocol.DefNumberVector
import nebulosa.indi.protocol.INDIProtocol
import nebulosa.indi.protocol.NumberVector
import nebulosa.indi.protocol.PropertyState

open class FilterWheel(
    client: INDIClient,
    handler: DeviceProtocolHandler,
    name: String,
) : Device(client, handler, name) {

    @Volatile @JvmField var slotCount = 0
    @Volatile @JvmField var position = -1
    @Volatile @JvmField var isMoving = false

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

                            handler.fireOnEventReceived(FilterWheelFilterIsMovingChanged(this))
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

    fun moveTo(slot: Int) {
        if (slot in 1..slotCount) {
            sendNewNumber("FILTER_SLOT", "FILTER_SLOT_VALUE" to slot.toDouble())
        }
    }

    override fun toString() = name

    companion object {

        @JvmStatic val DRIVERS = setOf(
            "indi_apogee_wheel",
            "indi_asi_wheel",
            "indi_atik_wheel",
            "indi_fli_wheel",
            // "indi_manual_wheel",
            "indi_optec_wheel",
            "indi_qhycfw1_wheel",
            "indi_qhycfw2_wheel",
            "indi_qhycfw3_wheel",
            "indi_quantum_wheel",
            "indi_simulator_wheel",
            "indi_sx_wheel",
            "indi_trutech_wheel",
            "indi_xagyl_wheel",
        )
    }
}
