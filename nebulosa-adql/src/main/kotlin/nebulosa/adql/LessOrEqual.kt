package nebulosa.adql

import adql.query.constraint.ADQLConstraint
import adql.query.constraint.Comparison
import adql.query.constraint.ComparisonOperator

data class LessOrEqual(override val constraint: ADQLConstraint) : WhereConstraint {

    constructor(left: Operand<*>, right: Operand<*>) : this(Comparison(left.operand, ComparisonOperator.LESS_OR_EQUAL, right.operand))

    override operator fun not() =
        (constraint as Comparison).let { GreaterThan(Comparison(it.leftOperand, ComparisonOperator.GREATER_THAN, it.rightOperand)) }
}
