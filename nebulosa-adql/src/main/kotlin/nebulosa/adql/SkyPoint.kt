package nebulosa.adql

import adql.query.operand.StringConstant
import adql.query.operand.function.geometry.PointFunction
import nebulosa.math.Angle
import nebulosa.math.PairOfAngle

data class SkyPoint internal constructor(override val operand: PointFunction) : Region {

    constructor(x: Operand<*>, y: Operand<*>) : this(PointFunction(StringConstant("ICRS"), x.operand, y.operand))

    constructor(x: Angle, y: Angle) : this(x.degrees.operand, y.degrees.operand)

    constructor(x: Operand<*>, y: Angle) : this(x, y.degrees.operand)

    constructor(x: Angle, y: Operand<*>) : this(x.degrees.operand, y)

    constructor(point: PairOfAngle) : this(point.first, point.second)
}
