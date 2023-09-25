package nebulosa.adql

import adql.query.operand.function.geometry.PolygonFunction

data class Polygon internal constructor(override val operand: PolygonFunction) : Region {

    constructor(points: Array<out SkyPoint>) : this(PolygonFunction(Region.ICRS, points.map { it.operand }))
}
