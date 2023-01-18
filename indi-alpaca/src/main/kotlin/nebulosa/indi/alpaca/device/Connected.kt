package nebulosa.indi.alpaca.device

import nebulosa.indi.alpaca.AlpacaINDIConnection
import nebulosa.indi.alpaca.BoolCommand
import nebulosa.indi.alpaca.device.camera.Camera

internal class Connected(
    connection: AlpacaINDIConnection,
    device: Device,
) : BoolCommand(connection, device) {

    override fun set(value: Boolean) {
        if (device is Camera) connection.client.camera.connect(device.number, value)
    }

    override fun run() {
        value = connection.client.camera.isConnected(device.number).value!!
    }
}
