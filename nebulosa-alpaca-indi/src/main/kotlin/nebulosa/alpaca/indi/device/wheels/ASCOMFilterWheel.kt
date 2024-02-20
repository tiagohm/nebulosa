package nebulosa.alpaca.indi.device.wheels

import nebulosa.alpaca.api.AlpacaFilterWheelService
import nebulosa.alpaca.api.ConfiguredDevice
import nebulosa.alpaca.indi.client.AlpacaClient
import nebulosa.alpaca.indi.device.ASCOMDevice
import nebulosa.indi.device.Device
import nebulosa.indi.device.filterwheel.FilterWheel
import nebulosa.indi.device.filterwheel.FilterWheelNamesChanged
import nebulosa.indi.device.filterwheel.FilterWheelPositionChanged
import nebulosa.indi.protocol.INDIProtocol

data class ASCOMFilterWheel(
    override val device: ConfiguredDevice,
    override val service: AlpacaFilterWheelService,
    override val sender: AlpacaClient,
) : ASCOMDevice(), FilterWheel {

    @Volatile override var count = 0
        private set
    @Volatile override var position = 0
        private set
    @Volatile override var moving = false
        private set
    @Volatile override var names: List<String> = emptyList()
        private set

    override fun onConnected() {
        processPosition()
        processNames()
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

    override fun names(names: Iterable<String>) {
        this.names = names.toList()
    }

    override fun snoop(devices: Iterable<Device?>) {}

    override fun handleMessage(message: INDIProtocol) {}

    private fun processMoving() {}

    private fun processPosition() {
        service.position(device.number).doRequest {
            if (it.value != position) {
                val prevPosition = position
                position = it.value

                sender.fireOnEventReceived(FilterWheelPositionChanged(this, prevPosition))
            }
        }
    }

    private fun processNames() {
        service.names(device.number).doRequest {
            if (it.value != names) {
                names = it.value

                sender.fireOnEventReceived(FilterWheelNamesChanged(this))
            }
        }
    }
}
