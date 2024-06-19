package nebulosa.curve.fitting

import org.apache.commons.math3.fitting.WeightedObservedPoint

class CurvePoint(x: Double, y: Double, weight: Double = 1.0) : WeightedObservedPoint(weight, x, y) {

    operator fun component1() = x

    operator fun component2() = y

    operator fun component3() = weight

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CurvePoint) return false

        if (x != other.x) return false
        if (y != other.y) return false

        return weight == other.weight
    }

    override fun hashCode(): Int {
        var result = x.hashCode()
        result = 31 * result + y.hashCode()
        result = 31 * result + weight.hashCode()
        return result
    }

    override fun toString() = "CurvePoint(x=$x, y=$y, weight=$weight)"

    companion object {

        @JvmStatic val ZERO = CurvePoint(0.0, 0.0)

        @JvmStatic
        infix fun CurvePoint.midPoint(point: CurvePoint) = CurvePoint((x + point.x) / 2, (y + point.y) / 2)
    }
}
