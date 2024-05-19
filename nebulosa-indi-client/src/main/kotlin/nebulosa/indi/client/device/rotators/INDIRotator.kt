package nebulosa.indi.client.device.rotators

import nebulosa.indi.client.INDIClient
import nebulosa.indi.client.device.INDIDevice
import nebulosa.indi.device.rotator.*
import nebulosa.indi.protocol.DefNumberVector
import nebulosa.indi.protocol.DefSwitchVector
import nebulosa.indi.protocol.INDIProtocol
import nebulosa.indi.protocol.SetNumberVector

// https://github.com/indilib/indi/blob/master/libs/indibase/indirotatorinterface.cpp

internal open class INDIRotator(
    override val sender: INDIClient,
    override val name: String,
) : INDIDevice(), Rotator {

    @Volatile final override var canAbort = false
    @Volatile final override var canHome = false
    @Volatile final override var canSync = false
    @Volatile final override var canReverse = false
    @Volatile final override var hasBacklashCompensation = false
    @Volatile final override var backslash = 0
    @Volatile final override var angle = 0.0
    @Volatile final override var minAngle = 0.0
    @Volatile final override var maxAngle = 0.0

    override fun handleMessage(message: INDIProtocol) {
        when (message) {
            is DefNumberVector -> {
                when (message.name) {
                    "ROTATOR_LIMITS_VALUE" -> {

                    }
                }
            }
            is DefSwitchVector -> {
                when (message.name) {
                    "ABORT" -> {
                        if ("ABS_ROTATOR_ANGLE" in message) {
                            canAbort = true
                            sender.fireOnEventReceived(RotatorCanAbortChanged(this))
                        }

                        if ("SYNC_ROTATOR_ANGLE" in message) {
                            canSync = true
                            sender.fireOnEventReceived(RotatorCanSyncChanged(this))
                        }
                    }
                    "HOME" -> {
                        canHome = true
                        sender.fireOnEventReceived(RotatorCanHomeChanged(this))
                    }
                    "ROTATOR_REVERSE" -> {
                        canReverse = true
                        sender.fireOnEventReceived(RotatorCanReverseChanged(this))
                    }
                }
            }
            is SetNumberVector -> {
                when (message.name) {
                    "ANGLE" -> {
                        angle = (message["ABS_ROTATOR_ANGLE"] ?: return).value
                        sender.fireOnEventReceived(RotatorAngleChanged(this))
                    }
                }
            }
            else -> Unit
        }

        super.handleMessage(message)
    }

    override fun moveRotator(angle: Double) {
        sendNewNumber("ANGLE", "ABS_ROTATOR_ANGLE" to angle)
    }

    override fun syncRotator(angle: Double) {
        if (canSync) {
            sendNewNumber("ANGLE", "SYNC_ROTATOR_ANGLE" to angle)
        }
    }

    override fun homeRotator() {
        if (canHome) {
            sendNewSwitch("HOME", "ROTATOR_HOME" to true)
        }
    }

    override fun reverseRotator(enable: Boolean) {
        if (canReverse) {
            sendNewSwitch("ROTATOR_REVERSE", (if (enable) "INDI_ENABLED" else "INDI_DISABLED") to true)
        }
    }

    override fun abortRotator() {
        if (canAbort) {
            sendNewSwitch("ABORT", "ROTATOR_ABORT_MOTION" to true)
        }
    }

    override fun close() = Unit

    override fun toString() = "INDIRotator(name=$name, canAbort=$canAbort, canHome=$canHome, " +
            "canSync=$canSync, canReverse=$canReverse, hasBacklashCompensation=$hasBacklashCompensation, " +
            "backslash=$backslash, minAngle=$minAngle, maxAngle=$maxAngle)"
}
