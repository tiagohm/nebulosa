package nebulosa.nova.almanac

interface DiscreteFunction {

    val stepSize: Double

    fun compute(x: Double): Int
}
