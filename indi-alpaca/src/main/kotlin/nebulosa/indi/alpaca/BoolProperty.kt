package nebulosa.indi.alpaca

import nebulosa.indi.alpaca.device.Device
import org.json.JSONObject

internal abstract class BoolProperty(
    connection: AlpacaINDIConnection,
    device: Device,
) : Property<Boolean>(connection, device) {

    init {
        period = 30L
    }

    final override fun parseBody(json: JSONObject) = json.getBoolean("Value")
}
