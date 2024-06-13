package nebulosa.watney.platesolver

data class PlateConstants(
    @JvmField val a: Double, @JvmField val b: Double, @JvmField val c: Double,
    @JvmField val d: Double, @JvmField val e: Double, @JvmField val f: Double,
) {

    val isValid
        get() = a.isFinite() && b.isFinite() && c.isFinite() &&
                d.isFinite() && e.isFinite() && f.isFinite()

    companion object {

        @JvmStatic val EMPTY = PlateConstants(0.0, 0.0, 0.0, 0.0, 0.0, 0.0)
    }
}
