package nebulosa.adql

import adql.query.constraint.ADQLConstraint
import adql.query.constraint.Comparison
import adql.query.constraint.ComparisonOperator

data class LessThan internal constructor(override val constraint: ADQLConstraint) : WhereConstraint {

    constructor(left: Operand<*>, right: Operand<*>) : this(Comparison(left.operand, ComparisonOperator.LESS_THAN, right.operand))

    override operator fun not() =
        (constraint as Comparison).let { GreaterOrEqual(Comparison(it.leftOperand, ComparisonOperator.GREATER_OR_EQUAL, it.rightOperand)) }
}
