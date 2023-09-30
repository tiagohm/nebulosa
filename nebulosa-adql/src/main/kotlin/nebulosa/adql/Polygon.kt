package nebulosa.adql

import adql.query.operand.NumericConstant
import adql.query.operand.function.geometry.PolygonFunction
import nebulosa.math.PairOfAngle
import nebulosa.math.toDegrees

data class Polygon internal constructor(override val operand: PolygonFunction) : Region {

    constructor(points: Array<out PairOfAngle>) : this(PolygonFunction(Region.ICRS, points.numericPoints()))

    companion object {

        @JvmStatic
        private fun Array<out PairOfAngle>.numericPoints(): List<NumericConstant> {
            val res = ArrayList<NumericConstant>(size * 2)

            forEach {
                res.add(NumericConstant(it.first.toDegrees))
                res.add(NumericConstant(it.second.toDegrees))
            }

            return res
        }
    }
}
