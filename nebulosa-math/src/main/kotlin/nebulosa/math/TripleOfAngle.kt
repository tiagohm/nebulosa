package nebulosa.math

data class TripleOfAngle(val first: Angle, val second: Angle, val third: Angle) {

    companion object {

        @JvmStatic val ZERO = TripleOfAngle(Angle.ZERO, Angle.ZERO, Angle.ZERO)
    }
}
