package nebulosa.curve.fitting

import org.apache.commons.math3.fitting.WeightedObservedPoint

class CurvePoint(x: Double, y: Double) : WeightedObservedPoint(1.0, x, y) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CurvePoint) return false

        if (x != other.x) return false

        return y == other.y
    }

    override fun hashCode(): Int {
        var result = x.hashCode()
        result = 31 * result + y.hashCode()
        return result
    }

    override fun toString() = "CurvePoint(x=$x, y=$y)"

    companion object {

        @JvmStatic val ZERO = CurvePoint(0.0, 0.0)
    }
}
