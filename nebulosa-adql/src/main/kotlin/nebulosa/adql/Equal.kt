package nebulosa.adql

import adql.query.constraint.ADQLConstraint
import adql.query.constraint.Comparison
import adql.query.constraint.ComparisonOperator

data class Equal(override val constraint: ADQLConstraint) : WhereConstraint {

    constructor(left: Operand<*>, right: Operand<*>) : this(Comparison(left.operand, ComparisonOperator.EQUAL, right.operand))

    override operator fun not() =
        (constraint as Comparison).let { NotEqual(Comparison(it.leftOperand, ComparisonOperator.NOT_EQUAL, it.rightOperand)) }
}
