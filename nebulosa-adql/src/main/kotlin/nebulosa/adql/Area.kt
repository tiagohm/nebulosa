package nebulosa.adql

import adql.query.operand.function.geometry.AreaFunction
import adql.query.operand.function.geometry.GeometryFunction

data class Area internal constructor(override val operand: GeometryFunction) : Operand<GeometryFunction> {

    constructor(region: Region) : this(AreaFunction(GeometryFunction.GeometryValue(region.operand)))
}
