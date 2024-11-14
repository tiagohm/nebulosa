package nebulosa.indi.client.device.focuser

import nebulosa.indi.client.INDIClient
import nebulosa.indi.client.device.DriverInfo
import nebulosa.indi.client.device.INDIDevice
import nebulosa.indi.device.firstOnSwitch
import nebulosa.indi.device.focuser.Focuser
import nebulosa.indi.device.focuser.FocuserCanAbortChanged
import nebulosa.indi.device.focuser.FocuserCanAbsoluteMoveChanged
import nebulosa.indi.device.focuser.FocuserCanRelativeMoveChanged
import nebulosa.indi.device.focuser.FocuserCanReverseChanged
import nebulosa.indi.device.focuser.FocuserCanSyncChanged
import nebulosa.indi.device.focuser.FocuserMaxPositionChanged
import nebulosa.indi.device.focuser.FocuserMoveFailed
import nebulosa.indi.device.focuser.FocuserMovingChanged
import nebulosa.indi.device.focuser.FocuserPositionChanged
import nebulosa.indi.device.focuser.FocuserReverseChanged
import nebulosa.indi.device.focuser.FocuserTemperatureChanged
import nebulosa.indi.protocol.DefNumberVector
import nebulosa.indi.protocol.DefSwitchVector
import nebulosa.indi.protocol.INDIProtocol
import nebulosa.indi.protocol.NumberVector
import nebulosa.indi.protocol.PropertyState
import nebulosa.indi.protocol.SwitchVector
import nebulosa.indi.protocol.Vector.Companion.isBusy

// https://github.com/indilib/indi/blob/master/libs/indibase/indifocuser.cpp

internal open class INDIFocuser(
    final override val sender: INDIClient,
    final override val driverInfo: DriverInfo,
) : INDIDevice(), Focuser {

    @Volatile final override var moving = false
    @Volatile final override var position = 0
    @Volatile final override var canAbsoluteMove = false
    @Volatile final override var canRelativeMove = false
    @Volatile final override var canAbort = false
    @Volatile final override var canReverse = false
    @Volatile final override var reversed = false
    @Volatile final override var canSync = false
    @Volatile final override var hasBacklash = false
    @Volatile final override var maxPosition = 0

    @Volatile final override var hasThermometer = false
    @Volatile final override var temperature = 0.0

    override fun handleMessage(message: INDIProtocol) {
        when (message) {
            is SwitchVector<*> -> {
                when (message.name) {
                    "FOCUS_ABORT_MOTION" -> {
                        if (message is DefSwitchVector) {
                            canAbort = true
                            sender.fireOnEventReceived(FocuserCanAbortChanged(this))
                        }
                    }
                    "FOCUS_REVERSE_MOTION" -> {
                        if (message is DefSwitchVector) {
                            canReverse = true
                            sender.fireOnEventReceived(FocuserCanReverseChanged(this))
                        }

                        reversed = message.firstOnSwitch().name == "INDI_ENABLED"

                        sender.fireOnEventReceived(FocuserReverseChanged(this))
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

                            sender.fireOnEventReceived(FocuserCanRelativeMoveChanged(this))

                            if (prevMaxPosition != maxPosition) {
                                sender.fireOnEventReceived(FocuserMaxPositionChanged(this))
                            }
                        }

                        if (message.state == PropertyState.ALERT) {
                            sender.fireOnEventReceived(FocuserMoveFailed(this))
                        }

                        val prevIsMoving = moving
                        moving = message.isBusy

                        if (prevIsMoving != moving) {
                            sender.fireOnEventReceived(FocuserMovingChanged(this))
                        }
                    }
                    "ABS_FOCUS_POSITION" -> {
                        if (message is DefNumberVector) {
                            canAbsoluteMove = true
                            val prevMaxPosition = maxPosition
                            maxPosition = message["FOCUS_ABSOLUTE_POSITION"]!!.max.toInt()

                            sender.fireOnEventReceived(FocuserCanAbsoluteMoveChanged(this))

                            if (prevMaxPosition != maxPosition) {
                                sender.fireOnEventReceived(FocuserMaxPositionChanged(this))
                            }
                        }

                        if (message.state == PropertyState.ALERT) {
                            sender.fireOnEventReceived(FocuserMoveFailed(this))
                        }

                        val prevPosition = position
                        position = message["FOCUS_ABSOLUTE_POSITION"]!!.value.toInt()

                        if (prevPosition != position) {
                            sender.fireOnEventReceived(FocuserPositionChanged(this))
                        }

                        val prevIsMoving = moving
                        moving = message.isBusy

                        if (prevIsMoving != moving) {
                            sender.fireOnEventReceived(FocuserMovingChanged(this))
                        }
                    }
                    "FOCUS_SYNC" -> {
                        if (message is DefNumberVector) {
                            canSync = true

                            sender.fireOnEventReceived(FocuserCanSyncChanged(this))
                        }
                    }
                    "FOCUS_BACKLASH_STEPS" -> {

                    }
                    "FOCUS_TEMPERATURE" -> {
                        if (!hasThermometer && message is DefNumberVector) {
                            hasThermometer = true
                            sender.registerThermometer(this)
                        }

                        val value = message["TEMPERATURE"]!!.value

                        if (temperature != value) {
                            temperature = value
                            sender.fireOnEventReceived(FocuserTemperatureChanged(this))
                        }
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
            sender.unregisterThermometer(this)
        }
    }

    override fun toString() = "Focuser(name=$name, moving=$moving, position=$position," +
            " canAbsoluteMove=$canAbsoluteMove, canRelativeMove=$canRelativeMove," +
            " canAbort=$canAbort, canReverse=$canReverse, reversed=$reversed," +
            " canSync=$canSync, hasBacklash=$hasBacklash," +
            " maxPosition=$maxPosition, hasThermometer=$hasThermometer," +
            " temperature=$temperature)"
}
