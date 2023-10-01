package nebulosa.adql

import adql.query.operand.function.geometry.DistanceFunction
import adql.query.operand.function.geometry.GeometryFunction

data class Distance internal constructor(override val operand: GeometryFunction) : Operand<GeometryFunction> {

    constructor(a: SkyPoint, b: SkyPoint) : this(
        DistanceFunction(
            GeometryFunction.GeometryValue(a.operand),
            GeometryFunction.GeometryValue(b.operand),
        )
    )
}
