package nebulosa.desktop.view.atlas

data class Twilight(
    override val start: Double,
    override val endInclusive: Double,
) : ClosedFloatingPointRange<Double> {

    override fun isEmpty() = start >= endInclusive

    override fun lessThanOrEquals(a: Double, b: Double) = a <= b

    companion object {

        @JvmStatic val EMPTY = Twilight(0.0, 0.0)
    }
}
