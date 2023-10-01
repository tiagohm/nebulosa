package nebulosa.api.atlas

@Suppress("ArrayInDataClass")
data class Twilight(
    val civilDusk: DoubleArray,
    val nauticalDusk: DoubleArray,
    val astronomicalDusk: DoubleArray,
    val night: DoubleArray,
    val astronomicalDawn: DoubleArray,
    val nauticalDawn: DoubleArray,
    val civilDawn: DoubleArray,
)
