package nebulosa.adql

import adql.query.operand.NumericConstant
import adql.query.operand.function.geometry.PolygonFunction
import nebulosa.math.toDegrees

data class Polygon internal constructor(override val operand: PolygonFunction) : Region {

    constructor(points: DoubleArray) : this(PolygonFunction(Region.ICRS, points.map { NumericConstant(it.toDegrees) }))
}
