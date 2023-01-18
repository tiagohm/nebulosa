package nebulosa.indi.alpaca

import nebulosa.indi.alpaca.device.Device

internal abstract class BoolCommand(
    connection: AlpacaINDIConnection,
    device: Device,
) : Command<Boolean>(connection, device) {

    @Volatile @JvmField protected var value = false

    final override fun get() = value
}
