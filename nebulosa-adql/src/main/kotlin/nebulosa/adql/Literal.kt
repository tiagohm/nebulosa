package nebulosa.adql

import adql.query.operand.ADQLOperand
import adql.query.operand.NegativeOperand
import adql.query.operand.NumericConstant
import adql.query.operand.StringConstant

data class Literal(override val operand: ADQLOperand) : Operand<ADQLOperand> {

    constructor(value: String) : this(StringConstant(value))

    constructor(value: Double) : this(NumericConstant(value))

    constructor(value: Long) : this(NumericConstant(value))

    constructor(value: Int) : this(value.toLong())

    constructor(value: Float) : this(value.toDouble())

    operator fun unaryMinus(): Literal {
        return Literal(if (operand is NegativeOperand) operand.operand else NegativeOperand(operand))
    }
}
