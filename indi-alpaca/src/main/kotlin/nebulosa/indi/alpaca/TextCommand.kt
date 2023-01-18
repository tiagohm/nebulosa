package nebulosa.indi.alpaca

import nebulosa.indi.alpaca.device.Device

internal abstract class TextCommand(
    connection: AlpacaINDIConnection,
    device: Device,
) : Command<String>(connection, device) {

    @Volatile @JvmField protected var value = ""

    final override fun get() = value
}
