package nebulosa.adql

import adql.query.constraint.ADQLConstraint
import adql.query.constraint.Comparison
import adql.query.constraint.ComparisonOperator

data class GreaterOrEqual internal constructor(override val constraint: ADQLConstraint) : WhereConstraint {

    constructor(left: Operand<*>, right: Operand<*>) : this(Comparison(left.operand, ComparisonOperator.GREATER_OR_EQUAL, right.operand))

    override operator fun not() =
        (constraint as Comparison).let { LessThan(Comparison(it.leftOperand, ComparisonOperator.LESS_THAN, it.rightOperand)) }
}
