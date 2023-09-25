package nebulosa.adql

import adql.query.constraint.ADQLConstraint
import adql.query.constraint.Comparison
import adql.query.constraint.ComparisonOperator

data class NotEqual internal constructor(override val constraint: ADQLConstraint) : WhereConstraint {

    constructor(left: Operand<*>, right: Operand<*>) : this(Comparison(left.operand, ComparisonOperator.NOT_EQUAL, right.operand))

    override operator fun not() = (constraint as Comparison).let { Equal(Comparison(it.leftOperand, ComparisonOperator.EQUAL, it.rightOperand)) }
}
