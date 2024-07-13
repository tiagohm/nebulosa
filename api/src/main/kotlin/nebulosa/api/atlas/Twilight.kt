package nebulosa.api.atlas

@Suppress("ArrayInDataClass")
data class Twilight(
    @JvmField val civilDusk: DoubleArray,
    @JvmField val nauticalDusk: DoubleArray,
    @JvmField val astronomicalDusk: DoubleArray,
    @JvmField val night: DoubleArray,
    @JvmField val astronomicalDawn: DoubleArray,
    @JvmField val nauticalDawn: DoubleArray,
    @JvmField val civilDawn: DoubleArray,
)
