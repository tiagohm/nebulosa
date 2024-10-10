package nebulosa.adql

import adql.query.constraint.ADQLConstraint
import adql.query.constraint.Comparison
import adql.query.constraint.ComparisonOperator

data class GreaterThan(override val constraint: ADQLConstraint) : WhereConstraint {

    constructor(left: Operand<*>, right: Operand<*>) : this(Comparison(left.operand, ComparisonOperator.GREATER_THAN, right.operand))

    override operator fun not() =
        (constraint as Comparison).let { LessOrEqual(Comparison(it.leftOperand, ComparisonOperator.LESS_OR_EQUAL, it.rightOperand)) }
}
