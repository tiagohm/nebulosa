package nebulosa.adql

import adql.query.operand.StringConstant
import adql.query.operand.function.geometry.CircleFunction
import nebulosa.math.Angle
import nebulosa.math.toDegrees

data class Circle internal constructor(override val operand: CircleFunction) : Region {

    constructor(x: Operand<*>, y: Operand<*>, radius: Operand<*>) : this(CircleFunction(StringConstant("ICRS"), x.operand, y.operand, radius.operand))

    constructor(x: Operand<*>, y: Operand<*>, radius: Angle) : this(x, y, radius.toDegrees.operand)

    constructor(x: Operand<*>, y: Angle, radius: Operand<*>) : this(x, y.toDegrees.operand, radius)

    constructor(x: Operand<*>, y: Angle, radius: Angle) : this(x, y.toDegrees.operand, radius.toDegrees.operand)

    constructor(x: Angle, y: Operand<*>, radius: Operand<*>) : this(x.toDegrees.operand, y, radius)

    constructor(x: Angle, y: Operand<*>, radius: Angle) : this(x.toDegrees.operand, y, radius.toDegrees.operand)

    constructor(x: Angle, y: Angle, radius: Operand<*>) : this(x.toDegrees.operand, y.toDegrees.operand, radius)

    constructor(x: Angle, y: Angle, radius: Angle) : this(x.toDegrees.operand, y.toDegrees.operand, radius.toDegrees.operand)
}
