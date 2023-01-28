package nebulosa.indi.device.focuser

import nebulosa.indi.device.AbstractDevice
import nebulosa.indi.device.DeviceProtocolHandler
import nebulosa.indi.device.MessageSender
import nebulosa.indi.device.firstOnSwitch
import nebulosa.indi.device.thermometer.ThermometerAttached
import nebulosa.indi.device.thermometer.ThermometerDetached
import nebulosa.indi.protocol.*

internal open class FocuserBase(
    sender: MessageSender,
    handler: DeviceProtocolHandler,
    name: String,
) : AbstractDevice(sender, handler, name), Focuser {

    override var moving = false
    override var position = 0
    override var canAbsoluteMove = false
    override var canRelativeMove = false
    override var canAbort = false
    override var canReverse = false
    override var reverse = false
    override var canSync = false
    override var hasBackslash = false
    override var maxPosition = 0

    override var hasThermometer = false
    override var temperature = 0.0

    override fun handleMessage(message: INDIProtocol) {
        when (message) {
            is SwitchVector<*> -> {
                when (message.name) {
                    "FOCUS_ABORT_MOTION" -> {
                        if (message is DefSwitchVector) {
                            canAbort = true

                            handler.fireOnEventReceived(FocuserCanAbortChanged(this))
                        }
                    }
                    "FOCUS_REVERSE_MOTION" -> {
                        if (message is DefSwitchVector) {
                            canReverse = true

                            handler.fireOnEventReceived(FocuserCanReverseChanged(this))
                        }

                        reverse = message.firstOnSwitch().name == "INDI_ENABLED"

                        handler.fireOnEventReceived(FocuserReverseChanged(this))
                    }
                    "FOCUS_BACKLASH_TOGGLE" -> {

                    }
                }
            }
            is NumberVector<*> -> {
                when (message.name) {
                    "REL_FOCUS_POSITION" -> {
                        if (message is DefNumberVector) {
                            canRelativeMove = true
                            val prevMaxPosition = maxPosition
                            maxPosition = message["FOCUS_RELATIVE_POSITION"]!!.max.toInt()

                            handler.fireOnEventReceived(FocuserCanRelativeMoveChanged(this))

                            if (prevMaxPosition != maxPosition) {
                                handler.fireOnEventReceived(FocuserMaxPositionChanged(this))
                            }
                        }

                        val prevIsMoving = moving
                        moving = message.isBusy

                        if (prevIsMoving != moving) {
                            handler.fireOnEventReceived(FocuserMovingChanged(this))
                        }
                    }
                    "ABS_FOCUS_POSITION" -> {
                        if (message is DefNumberVector) {
                            canAbsoluteMove = true
                            val prevMaxPosition = maxPosition
                            maxPosition = message["FOCUS_ABSOLUTE_POSITION"]!!.max.toInt()

                            handler.fireOnEventReceived(FocuserCanAbsoluteMoveChanged(this))

                            if (prevMaxPosition != maxPosition) {
                                handler.fireOnEventReceived(FocuserMaxPositionChanged(this))
                            }
                        }

                        if (message.state == PropertyState.ALERT) {
                            handler.fireOnEventReceived(FocuserMoveFailed(this))
                        }

                        val prevPosition = position
                        position = message["FOCUS_ABSOLUTE_POSITION"]!!.value.toInt()

                        if (prevPosition != position) {
                            handler.fireOnEventReceived(FocuserPositionChanged(this))
                        }

                        val prevIsMoving = moving
                        moving = message.isBusy

                        if (prevIsMoving != moving) {
                            handler.fireOnEventReceived(FocuserMovingChanged(this))
                        }
                    }
                    "FOCUS_SYNC" -> {
                        if (message is DefNumberVector) {
                            canSync = true

                            handler.fireOnEventReceived(FocuserCanSyncChanged(this))
                        }
                    }
                    "FOCUS_BACKLASH_STEPS" -> {

                    }
                    "FOCUS_TEMPERATURE" -> {
                        if (message is DefNumberVector) {
                            hasThermometer = true
                            handler.fireOnEventReceived(ThermometerAttached(this))
                        }

                        temperature = message["TEMPERATURE"]!!.value

                        handler.fireOnEventReceived(FocuserTemperatureChanged(this))
                    }
                }
            }
            else -> Unit
        }

        super.handleMessage(message)
    }

    override fun moveFocusIn(steps: Int) {
        if (canRelativeMove) {
            sendNewSwitch("FOCUS_MOTION", "FOCUS_INWARD" to true)
            sendNewNumber("REL_FOCUS_POSITION", "FOCUS_RELATIVE_POSITION" to steps.toDouble())
        }
    }

    override fun moveFocusOut(steps: Int) {
        if (canRelativeMove) {
            sendNewSwitch("FOCUS_MOTION", "FOCUS_OUTWARD" to true)
            sendNewNumber("REL_FOCUS_POSITION", "FOCUS_RELATIVE_POSITION" to steps.toDouble())
        }
    }

    override fun moveFocusTo(steps: Int) {
        if (canAbsoluteMove) {
            sendNewNumber("ABS_FOCUS_POSITION", "FOCUS_ABSOLUTE_POSITION" to steps.toDouble())
        }
    }

    override fun abortFocus() {
        if (canAbort) {
            sendNewSwitch("FOCUS_ABORT_MOTION", "ABORT" to true)
        }
    }

    override fun reverseFocus(enable: Boolean) {
        if (canReverse) {
            sendNewSwitch("FOCUS_REVERSE_MOTION", (if (enable) "INDI_ENABLED" else "INDI_DISABLED") to true)
        }
    }

    override fun syncFocusTo(steps: Int) {
        if (canSync) {
            sendNewNumber("FOCUS_SYNC", "FOCUS_SYNC_VALUE" to steps.toDouble())
        }
    }

    override fun close() {
        if (hasThermometer) {
            hasThermometer = false
            handler.fireOnEventReceived(ThermometerDetached(this))
        }
    }

    override fun toString(): String {
        return "Focuser(name=$name, moving=$moving, position=$position," +
                " canAbsoluteMove=$canAbsoluteMove, canRelativeMove=$canRelativeMove," +
                " canAbort=$canAbort, canReverse=$canReverse, reverse=$reverse," +
                " canSync=$canSync, hasBackslash=$hasBackslash," +
                " maxPosition=$maxPosition, hasThermometer=$hasThermometer," +
                " temperature=$temperature)"
    }
}
