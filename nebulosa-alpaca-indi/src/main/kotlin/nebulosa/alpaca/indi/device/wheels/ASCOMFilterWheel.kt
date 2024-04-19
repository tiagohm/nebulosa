package nebulosa.alpaca.indi.device.wheels

import nebulosa.alpaca.api.AlpacaFilterWheelService
import nebulosa.alpaca.api.ConfiguredDevice
import nebulosa.alpaca.indi.client.AlpacaClient
import nebulosa.alpaca.indi.device.ASCOMDevice
import nebulosa.indi.device.Device
import nebulosa.indi.device.filterwheel.*
import nebulosa.indi.protocol.INDIProtocol

@Suppress("RedundantModalityModifier")
data class ASCOMFilterWheel(
    override val device: ConfiguredDevice,
    override val service: AlpacaFilterWheelService,
    override val sender: AlpacaClient,
) : ASCOMDevice(), FilterWheel {

    @Volatile final override var count = 0
    @Volatile final override var position = 0
    @Volatile final override var moving = false
    @Volatile final override var names = emptyList<String>()

    @Volatile private var targetPosition = 0

    override val snoopedDevices = emptyList<Device>()

    override fun onConnected() {
        processPosition()
        processNames()
    }

    override fun onDisconnected() {}

    override fun moveTo(position: Int) {
        val newPosition = position - 1

        if (newPosition >= 0 && position != this.position) {
            targetPosition = newPosition

            if (service.position(device.number, newPosition).doRequest() != null) {
                moving = true
                sender.fireOnEventReceived(FilterWheelMovingChanged(this))
            }
        }
    }

    override fun refresh(elapsedTimeInSeconds: Long) {
        super.refresh(elapsedTimeInSeconds)

        if (connected) {
            processPosition()
        }
    }

    override fun names(names: Iterable<String>) {
        this.names = names.toList()
        sender.fireOnEventReceived(FilterWheelNamesChanged(this))
    }

    override fun snoop(devices: Iterable<Device?>) {}

    override fun handleMessage(message: INDIProtocol) {}

    private fun processPosition() {
        service.position(device.number).doRequest {
            val value = it.value + 1

            if (value > 0 && value != position) {
                val prevPosition = position
                position = value
                sender.fireOnEventReceived(FilterWheelPositionChanged(this, prevPosition))

                if (moving && value == targetPosition) {
                    moving = false
                    sender.fireOnEventReceived(FilterWheelMovingChanged(this))
                }
            }
        }
    }

    private fun processNames() {
        service.names(device.number).doRequest {
            if (it.value.size != names.size) {
                count = it.value.size
                sender.fireOnEventReceived(FilterWheelCountChanged(this))
            }

            if (it.value != names) {
                names = it.value
                sender.fireOnEventReceived(FilterWheelNamesChanged(this))
            }
        }
    }
}
