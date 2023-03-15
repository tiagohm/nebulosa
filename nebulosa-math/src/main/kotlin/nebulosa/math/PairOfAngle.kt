package nebulosa.math

data class PairOfAngle(val first: Angle, val second: Angle) {

    companion object {

        @JvmStatic val ZERO = PairOfAngle(Angle.ZERO, Angle.ZERO)
    }
}
