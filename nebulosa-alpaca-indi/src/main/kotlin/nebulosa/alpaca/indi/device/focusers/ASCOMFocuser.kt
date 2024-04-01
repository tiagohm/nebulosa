package nebulosa.alpaca.indi.device.focusers

import nebulosa.alpaca.api.AlpacaFocuserService
import nebulosa.alpaca.api.ConfiguredDevice
import nebulosa.alpaca.indi.client.AlpacaClient
import nebulosa.alpaca.indi.device.ASCOMDevice
import nebulosa.indi.device.Device
import nebulosa.indi.device.focuser.Focuser
import nebulosa.indi.device.focuser.FocuserCanAbsoluteMoveChanged
import nebulosa.indi.device.focuser.FocuserMovingChanged
import nebulosa.indi.device.focuser.FocuserPositionChanged
import nebulosa.indi.device.thermometer.ThermometerAttached
import nebulosa.indi.device.thermometer.ThermometerDetached
import nebulosa.indi.device.thermometer.ThermometerTemperatureChanged
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
    @Volatile final override var canRelativeMove = false
    @Volatile final override var canAbort = false
    @Volatile final override var canReverse = false
    @Volatile final override var reversed = false
    @Volatile final override var canSync = false
    @Volatile final override var hasBacklash = false
    @Volatile final override var maxPosition = 0

    @Volatile final override var hasThermometer = false
    @Volatile final override var temperature = 0.0

    override val snoopedDevices = emptyList<Device>()

    override fun moveFocusIn(steps: Int) {
        if (canAbsoluteMove) {
            service.move(device.number, position + steps).doRequest()
        } else {
            service.move(device.number, steps).doRequest()
        }
    }

    override fun moveFocusOut(steps: Int) {
        if (canAbsoluteMove) {
            service.move(device.number, position - steps).doRequest()
        } else {
            service.move(device.number, -steps).doRequest()
        }
    }

    override fun moveFocusTo(steps: Int) {
    }

    override fun abortFocus() {
        service.halt(device.number).doRequest()
    }

    override fun reverseFocus(enable: Boolean) {
    }

    override fun syncFocusTo(steps: Int) {
    }

    override fun snoop(devices: Iterable<Device?>) {
    }

    override fun handleMessage(message: INDIProtocol) {
    }

    override fun onConnected() {
        processCapabilities()
        processPosition()
        processTemperature()
    }

    override fun onDisconnected() {
    }

    override fun reset() {
        super.reset()

        moving = false
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
        super.close()

        if (hasThermometer) {
            hasThermometer = false
            sender.fireOnEventReceived(ThermometerDetached(this))
        }
    }

    override fun refresh(elapsedTimeInSeconds: Long) {
        super.refresh(elapsedTimeInSeconds)

        processMoving()
        processPosition()
        processTemperature()
    }

    private fun processCapabilities() {
        service.canAbsolute(device.number).doRequest {
            if (it.value) {
                canAbsoluteMove = true
                sender.fireOnEventReceived(FocuserCanAbsoluteMoveChanged(this))
            }
        }

        service.temperature(device.number).doRequest {
            hasThermometer = true
            sender.fireOnEventReceived(ThermometerAttached(this))
        }
    }

    private fun processMoving() {
        service.isMoving(device.number).doRequest {
            if (it.value != moving) {
                moving = it.value

                sender.fireOnEventReceived(FocuserMovingChanged(this))
            }
        }
    }

    private fun processPosition() {
        service.position(device.number).doRequest {
            if (it.value != position) {
                position = it.value

                sender.fireOnEventReceived(FocuserPositionChanged(this))
            }
        }
    }

    private fun processTemperature() {
        if (hasThermometer) {
            service.temperature(device.number).doRequest {
                if (it.value != temperature) {
                    temperature = it.value

                    sender.fireOnEventReceived(ThermometerTemperatureChanged(this))
                }
            }
        }
    }
}
