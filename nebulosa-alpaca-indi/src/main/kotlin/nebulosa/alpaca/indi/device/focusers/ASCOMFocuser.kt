package nebulosa.alpaca.indi.device.focusers

import nebulosa.alpaca.api.AlpacaFocuserService
import nebulosa.alpaca.api.ConfiguredDevice
import nebulosa.alpaca.indi.client.AlpacaClient
import nebulosa.alpaca.indi.device.ASCOMDevice
import nebulosa.indi.device.Device
import nebulosa.indi.device.focuser.Focuser
import nebulosa.indi.device.focuser.FocuserCanAbsoluteMoveChanged
import nebulosa.indi.device.focuser.FocuserMaxPositionChanged
import nebulosa.indi.device.focuser.FocuserMovingChanged
import nebulosa.indi.device.focuser.FocuserPositionChanged
import nebulosa.indi.device.focuser.FocuserTemperatureChanged
import nebulosa.indi.protocol.INDIProtocol

@Suppress("RedundantModalityModifier")
data class ASCOMFocuser(
    override val device: ConfiguredDevice,
    override val service: AlpacaFocuserService,
    override val sender: AlpacaClient,
) : ASCOMDevice(), Focuser {

    @Volatile final override var moving = false
    @Volatile final override var position = 0
    @Volatile final override var canAbsoluteMove = false
    @Volatile final override var canRelativeMove = true
    @Volatile final override var canAbort = true
    @Volatile final override var canReverse = false
    @Volatile final override var reversed = false
    @Volatile final override var canSync = false
    @Volatile final override var hasBacklash = false
    @Volatile final override var maxPosition = 0

    @Volatile final override var hasThermometer = false
    @Volatile final override var temperature = 0.0

    @Volatile private var internalMoving = false

    override val snoopedDevices = emptyList<Device>()

    override fun moveFocusIn(steps: Int) {
        internalMoving = if (canAbsoluteMove) {
            service.move(device.number, position + steps).doRequest { }
        } else {
            service.move(device.number, steps).doRequest { }
        }
    }

    override fun moveFocusOut(steps: Int) {
        internalMoving = if (canAbsoluteMove) {
            service.move(device.number, position - steps).doRequest { }
        } else {
            service.move(device.number, -steps).doRequest { }
        }
    }

    override fun moveFocusTo(steps: Int) {
        internalMoving = service.move(device.number, steps).doRequest { }
    }

    override fun abortFocus() {
        service.halt(device.number).doRequest()
    }

    override fun reverseFocus(enable: Boolean) = Unit

    override fun syncFocusTo(steps: Int) = Unit

    override fun snoop(devices: Iterable<Device?>) = Unit

    override fun handleMessage(message: INDIProtocol) = Unit

    override fun onConnected() {
        processCapabilities()
        processPosition()
    }

    override fun onDisconnected() = Unit

    override fun reset() {
        super.reset()

        moving = false
        internalMoving = false
        position = 0
        canAbsoluteMove = false
        canRelativeMove = false
        canAbort = false
        canReverse = false
        reversed = false
        canSync = false
        hasBacklash = false
        maxPosition = 0
        hasThermometer = false
        temperature = 0.0
    }

    override fun close() {
        if (hasThermometer) {
            hasThermometer = false
            sender.unregisterThermometer(this)
        }

        super.close()
    }

    override fun refresh(elapsedTimeInSeconds: Long) {
        super.refresh(elapsedTimeInSeconds)

        if (connected) {
            processMoving()
            processPosition()
            processTemperature(false)
        }
    }

    private fun processCapabilities() {
        service.canAbsolute(device.number).doRequest {
            if (it.value) {
                canAbsoluteMove = true
                sender.fireOnEventReceived(FocuserCanAbsoluteMoveChanged(this))
            }
        }

        service.maxStep(device.number).doRequest {
            maxPosition = it.value
            sender.fireOnEventReceived(FocuserMaxPositionChanged(this))
        }

        processTemperature(true)
    }

    private fun processMoving() {
        service.isMoving(device.number).doRequest {
            val value = it.value || internalMoving

            if (value != moving) {
                moving = value
                sender.fireOnEventReceived(FocuserMovingChanged(this))
            }
        }
    }

    private fun processPosition() {
        service.position(device.number).doRequest {
            if (it.value != position) {
                position = it.value
                sender.fireOnEventReceived(FocuserPositionChanged(this))
            } else if (internalMoving && moving) {
                moving = false
                internalMoving = false
                sender.fireOnEventReceived(FocuserMovingChanged(this))
            }
        }
    }

    private fun processTemperature(init: Boolean) {
        if (hasThermometer || init) {
            service.temperature(device.number).doRequest {
                if (!hasThermometer) {
                    hasThermometer = true
                    sender.registerThermometer(this)
                }

                if (it.value != temperature) {
                    temperature = it.value
                    sender.fireOnEventReceived(FocuserTemperatureChanged(this))
                }
            }
        }
    }

    override fun toString() = "Focuser(name=$name, moving=$moving, position=$position," +
            " canAbsoluteMove=$canAbsoluteMove, canRelativeMove=$canRelativeMove," +
            " canAbort=$canAbort, canReverse=$canReverse, reversed=$reversed," +
            " canSync=$canSync, hasBacklash=$hasBacklash," +
            " maxPosition=$maxPosition, hasThermometer=$hasThermometer," +
            " temperature=$temperature)"
}
