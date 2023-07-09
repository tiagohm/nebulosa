package nebulosa.api.data.responses

@Suppress("ArrayInDataClass")
data class TwilightResponse(
    val civilDusk: DoubleArray,
    val nauticalDusk: DoubleArray,
    val astronomicalDusk: DoubleArray,
    val night: DoubleArray,
    val astronomicalDawn: DoubleArray,
    val nauticalDawn: DoubleArray,
    val civilDawn: DoubleArray,
)
