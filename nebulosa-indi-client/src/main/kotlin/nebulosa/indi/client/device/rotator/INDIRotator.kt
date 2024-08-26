package nebulosa.indi.client.device.rotator

import nebulosa.indi.client.INDIClient
import nebulosa.indi.client.device.DriverInfo
import nebulosa.indi.client.device.INDIDevice
import nebulosa.indi.device.firstOnSwitch
import nebulosa.indi.device.rotator.*
import nebulosa.indi.protocol.*
import nebulosa.indi.protocol.Vector.Companion.isBusy

// https://github.com/indilib/indi/blob/master/libs/indibase/indirotatorinterface.cpp

internal open class INDIRotator(
    final override val sender: INDIClient,
    final override val driverInfo: DriverInfo,
) : INDIDevice(), Rotator {

    @Volatile final override var moving = false
    @Volatile final override var canAbort = false
    @Volatile final override var canHome = false
    @Volatile final override var canSync = false
    @Volatile final override var canReverse = false
    @Volatile final override var reversed = false
    @Volatile final override var hasBacklashCompensation = false
    @Volatile final override var backslash = 0
    @Volatile final override var angle = 0.0
    @Volatile final override var minAngle = 0.0
    @Volatile final override var maxAngle = 0.0

    override fun handleMessage(message: INDIProtocol) {
        when (message) {
            is DefNumberVector -> {
                when (message.name) {
                    "ABS_ROTATOR_ANGLE" -> {
                        val angle = message["ANGLE"] ?: return
                        minAngle = angle.min
                        maxAngle = angle.max

                        sender.fireOnEventReceived(RotatorMinMaxAngleChanged(this))

                        if (angle.value != 0.0) {
                            this.angle = angle.value
                            sender.fireOnEventReceived(RotatorAngleChanged(this))
                        }
                    }
                    "SYNC_ROTATOR_ANGLE" -> {
                        canSync = true
                        sender.fireOnEventReceived(RotatorCanSyncChanged(this))
                    }
                }
            }
            is DefSwitchVector -> {
                when (message.name) {
                    "ROTATOR_ABORT_MOTION" -> {
                        canAbort = true
                        sender.fireOnEventReceived(RotatorCanAbortChanged(this))
                    }
                    "ROTATOR_HOME" -> {
                        canHome = true
                        sender.fireOnEventReceived(RotatorCanHomeChanged(this))
                    }
                    "ROTATOR_REVERSE" -> {
                        canReverse = true
                        sender.fireOnEventReceived(RotatorCanReverseChanged(this))

                        handleReversed(message)
                    }
                }
            }
            is SetNumberVector -> {
                when (message.name) {
                    "ABS_ROTATOR_ANGLE" -> {
                        val value = message["ANGLE"]?.value ?: return

                        if (moving != message.isBusy) {
                            this.moving = message.isBusy
                            sender.fireOnEventReceived(RotatorMovingChanged(this))
                        }

                        if (value != angle) {
                            angle = value
                            sender.fireOnEventReceived(RotatorAngleChanged(this))
                        }
                    }
                }
            }
            is SetSwitchVector -> {
                when (message.name) {
                    "ROTATOR_REVERSE" -> {
                        handleReversed(message)
                    }
                }
            }
            else -> Unit
        }

        super.handleMessage(message)
    }

    private fun handleReversed(message: SwitchVector<*>) {
        val reversed = message.firstOnSwitch().name == "INDI_ENABLED"

        if (reversed != this.reversed) {
            this.reversed = reversed
            sender.fireOnEventReceived(RotatorReversedChanged(this))
        }
    }

    override fun moveRotator(angle: Double) {
        sendNewNumber("ABS_ROTATOR_ANGLE", "ANGLE" to angle)
    }

    override fun syncRotator(angle: Double) {
        if (canSync) {
            sendNewNumber("SYNC_ROTATOR_ANGLE", "ANGLE" to angle)
        }
    }

    override fun homeRotator() {
        if (canHome) {
            sendNewSwitch("ROTATOR_HOME", "HOME" to true)
        }
    }

    override fun reverseRotator(enable: Boolean) {
        if (canReverse) {
            sendNewSwitch("ROTATOR_REVERSE", (if (enable) "INDI_ENABLED" else "INDI_DISABLED") to true)
        }
    }

    override fun abortRotator() {
        if (canAbort) {
            sendNewSwitch("ROTATOR_ABORT_MOTION", "ABORT" to true)
        }
    }

    override fun close() = Unit

    override fun toString() = "INDIRotator(name=$name, canAbort=$canAbort, canHome=$canHome, " +
            "canSync=$canSync, canReverse=$canReverse, hasBacklashCompensation=$hasBacklashCompensation, " +
            "backslash=$backslash, minAngle=$minAngle, maxAngle=$maxAngle)"
}
