package nebulosa.indi.alpaca.device.camera

import nebulosa.indi.alpaca.AlpacaINDIConnection
import nebulosa.indi.alpaca.device.Device

internal class Camera(
    connection: AlpacaINDIConnection,
    id: String, name: String,
) : Device(connection, id, name)
