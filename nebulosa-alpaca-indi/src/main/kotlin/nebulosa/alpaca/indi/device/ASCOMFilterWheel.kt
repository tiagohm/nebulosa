package nebulosa.alpaca.indi.device

import nebulosa.alpaca.api.AlpacaFilterWheelService
import nebulosa.alpaca.api.ConfiguredDevice
import nebulosa.alpaca.indi.client.AlpacaClient
import nebulosa.indi.device.Device
import nebulosa.indi.device.filterwheel.FilterWheel
import nebulosa.indi.device.filterwheel.FilterWheelPositionChanged
import nebulosa.indi.protocol.INDIProtocol

class ASCOMFilterWheel(
    override val device: ConfiguredDevice,
    override val service: AlpacaFilterWheelService,
    override val sender: AlpacaClient,
) : ASCOMDevice(), FilterWheel {

    @Volatile override var count = 0
        private set
    @Volatile override var position = -1
        private set
    @Volatile override var moving = false
        private set

    override fun onConnected() {
        processPosition()
    }

    override fun onDisconnected() {}

    override fun moveTo(position: Int) {
        if (position != this.position) {
            service.position(device.number, position).doRequest()
        }
    }

    override fun refresh(elapsedTimeInSeconds: Long) {
        super.refresh(elapsedTimeInSeconds)

        if (connected) {
            processPosition()
            processMoving()
        }
    }

    override fun names(names: Iterable<String>) {}

    override fun snoop(devices: Iterable<Device?>) {}

    override fun handleMessage(message: INDIProtocol) {}

    private fun processMoving() {
    }

    private fun processPosition() {
        service.position(device.number).doRequest {
            if (it.value != position) {
                val prevPosition = position
                position = it.value

                sender.fireOnEventReceived(FilterWheelPositionChanged(this, prevPosition))
            }
        }
    }
}
