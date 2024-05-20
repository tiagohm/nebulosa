package nebulosa.alpaca.indi.device.rotators

import nebulosa.alpaca.api.AlpacaRotatorService
import nebulosa.alpaca.api.ConfiguredDevice
import nebulosa.alpaca.indi.client.AlpacaClient
import nebulosa.alpaca.indi.device.ASCOMDevice
import nebulosa.indi.device.Device
import nebulosa.indi.device.rotator.*
import nebulosa.indi.protocol.INDIProtocol

@Suppress("RedundantModalityModifier")
data class ASCOMRotator(
    override val device: ConfiguredDevice,
    override val service: AlpacaRotatorService,
    override val sender: AlpacaClient,
) : ASCOMDevice(), Rotator {

    @Volatile final override var angle = 0.0
    @Volatile final override var minAngle = 0.0
    @Volatile final override var maxAngle = 360.0
    @Volatile final override var moving = false
    @Volatile final override var canAbort = true
    @Volatile final override var canHome = false
    @Volatile final override var canSync = true
    @Volatile final override var canReverse = false
    @Volatile final override var reversed = false
    @Volatile final override var hasBacklashCompensation = false
    @Volatile final override var backslash = 0

    override val snoopedDevices = emptyList<Device>()

    override fun onConnected() {
        processCapabilities()
        processPosition()
    }

    override fun onDisconnected() = Unit

    override fun refresh(elapsedTimeInSeconds: Long) {
        super.refresh(elapsedTimeInSeconds)

        if (connected) {
            processPosition()
            processMoving()
            processReversed()
        }
    }

    override fun snoop(devices: Iterable<Device?>) = Unit

    override fun moveRotator(angle: Double) {
        service.moveTo(device.number, angle).doRequest()
    }

    override fun syncRotator(angle: Double) {
        if (canSync) {
            service.sync(device.number, angle).doRequest()
        }
    }

    override fun homeRotator() = Unit

    override fun reverseRotator(enable: Boolean) {
        if (canReverse) {
            service.reverse(device.number, enable).doRequest()
        }
    }

    override fun abortRotator() {
        if (canAbort) {
            service.halt(device.number).doRequest()
        }
    }

    override fun handleMessage(message: INDIProtocol) = Unit

    private fun processCapabilities() {
        service.canReverse(device.number).doRequest {
            if (it.value != canReverse) {
                canReverse = it.value
                sender.fireOnEventReceived(RotatorCanReverseChanged(this))
            }
        }
    }

    private fun processPosition() {
        service.position(device.number).doRequest {
            if (it.value != angle) {
                angle = it.value
                sender.fireOnEventReceived(RotatorAngleChanged(this))
            }
        }
    }

    private fun processMoving() {
        service.isMoving(device.number).doRequest {
            if (it.value != moving) {
                moving = it.value
                sender.fireOnEventReceived(RotatorMovingChanged(this))
            }
        }
    }

    private fun processReversed() {
        service.isReversed(device.number).doRequest {
            if (it.value != reversed) {
                reversed = it.value
                sender.fireOnEventReceived(RotatorReversedChanged(this))
            }
        }
    }
}
