package nebulosa.adql

import adql.query.operand.StringConstant
import adql.query.operand.function.geometry.BoxFunction
import nebulosa.math.Angle
import nebulosa.math.toDegrees

data class Box internal constructor(override val operand: BoxFunction) : Region {

    constructor(x: Operand<*>, y: Operand<*>, width: Operand<*>, height: Operand<*>)
            : this(BoxFunction(StringConstant("ICRS"), x.operand, y.operand, width.operand, height.operand))

    constructor(x: Operand<*>, y: Angle, width: Angle, height: Angle = width)
            : this(x, y.toDegrees.operand, width.toDegrees.operand, height.toDegrees.operand)

    constructor(x: Operand<*>, y: Operand<*>, width: Angle, height: Angle = width)
            : this(x, y, width.toDegrees.operand, height.toDegrees.operand)

    constructor(x: Operand<*>, y: Operand<*>, width: Operand<*>, height: Angle)
            : this(x, y, width, height.toDegrees.operand)

    constructor(x: Angle, y: Angle, width: Angle, height: Angle = width)
            : this(x.toDegrees.operand, y.toDegrees.operand, width.toDegrees.operand, height.toDegrees.operand)
}
