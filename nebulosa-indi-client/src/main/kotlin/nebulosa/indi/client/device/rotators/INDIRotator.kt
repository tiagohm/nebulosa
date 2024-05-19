package nebulosa.indi.client.device.rotators

import nebulosa.indi.client.INDIClient
import nebulosa.indi.client.device.INDIDevice
import nebulosa.indi.device.rotator.Rotator
import nebulosa.indi.protocol.INDIProtocol

// https://github.com/indilib/indi/blob/master/libs/indibase/indirotator.cpp

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
    @Volatile final override var minAngle = 0.0
    @Volatile final override var maxAngle = 0.0

    override fun moveRotator(angle: Double) {
        TODO("Not yet implemented")
    }

    override fun syncRotator(angle: Double) {
        TODO("Not yet implemented")
    }

    override fun homeRotator() {
        TODO("Not yet implemented")
    }

    override fun reverseRotator() {
        TODO("Not yet implemented")
    }

    override fun abortRotator() {
        TODO("Not yet implemented")
    }

    override fun handleMessage(message: INDIProtocol) {
        super.handleMessage(message)
    }

    override fun close() = Unit

    override fun toString() = "INDIRotator(name=$name, canAbort=$canAbort, canHome=$canHome, " +
            "canSync=$canSync, canReverse=$canReverse, hasBacklashCompensation=$hasBacklashCompensation, " +
            "backslash=$backslash, minAngle=$minAngle, maxAngle=$maxAngle)"
}
