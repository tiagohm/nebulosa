package nebulosa.time

interface Spline<T> {

    val lower: T

    val upper: T

    val width: T

    operator fun get(index: Int): T

    val derivative: Spline<T>

    fun compute(value: Double): Double
}
