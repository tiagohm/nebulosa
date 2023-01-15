package nebulosa.indi.alpaca.device

import nebulosa.indi.alpaca.AlpacaINDIConnection
import nebulosa.indi.alpaca.BoolProperty

internal class Connected(
    connection: AlpacaINDIConnection,
    device: Device,
) : BoolProperty(connection, device) {

    override fun call() = requestGet("telescope/0/connected")
}
