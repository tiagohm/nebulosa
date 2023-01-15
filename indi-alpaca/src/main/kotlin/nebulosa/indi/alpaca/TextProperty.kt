package nebulosa.indi.alpaca

import nebulosa.indi.alpaca.device.Device
import org.json.JSONObject

internal abstract class TextProperty(
    connection: AlpacaINDIConnection,
    device: Device,
) : Property<String>(connection, device) {

    final override fun parseBody(json: JSONObject) = json.getString("Value") ?: ""
}
