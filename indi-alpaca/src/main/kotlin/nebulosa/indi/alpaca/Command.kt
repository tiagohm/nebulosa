package nebulosa.indi.alpaca

import nebulosa.indi.alpaca.device.Device

internal sealed class Command<T>(
    @JvmField protected val connection: AlpacaINDIConnection,
    @JvmField protected val device: Device,
) : Runnable {

    @JvmField internal var period = 5L

    abstract fun get(): T

    abstract fun set(value: T)
}
