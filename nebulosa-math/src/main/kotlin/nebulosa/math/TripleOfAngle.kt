package nebulosa.math

data class TripleOfAngle(@JvmField val first: Angle, @JvmField val second: Angle, @JvmField val third: Angle) {

    companion object {

        @JvmStatic val ZERO = TripleOfAngle(0.0, 0.0, 0.0)
    }
}
