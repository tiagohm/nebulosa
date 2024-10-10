package nebulosa.adql

import adql.query.operand.StringConstant
import adql.query.operand.function.geometry.PointFunction
import nebulosa.math.Angle
import nebulosa.math.toDegrees

data class SkyPoint(override val operand: PointFunction) : Region {

    constructor(x: Operand<*>, y: Operand<*>) : this(PointFunction(StringConstant("ICRS"), x.operand, y.operand))

    constructor(x: Angle, y: Angle) : this(x.toDegrees.operand, y.toDegrees.operand)

    constructor(x: Operand<*>, y: Angle) : this(x, y.toDegrees.operand)

    constructor(x: Angle, y: Operand<*>) : this(x.toDegrees.operand, y)

    constructor(point: DoubleArray) : this(point[0], point[1])
}
