package nebulosa.math

data class PairOfAngle(@JvmField val first: Angle, @JvmField val second: Angle) {

    companion object {

        @JvmStatic val ZERO = PairOfAngle(0.0, 0.0)
    }
}
