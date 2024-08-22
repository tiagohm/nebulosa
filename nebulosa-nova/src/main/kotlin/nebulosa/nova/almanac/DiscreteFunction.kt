package nebulosa.nova.almanac

fun interface DiscreteFunction {

    operator fun invoke(x: Double): Int
}
