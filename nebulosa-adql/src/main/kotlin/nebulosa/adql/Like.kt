package nebulosa.adql

import adql.query.constraint.ADQLConstraint
import adql.query.constraint.Comparison
import adql.query.constraint.ComparisonOperator

data class Like internal constructor(override val constraint: ADQLConstraint) : WhereConstraint {

    constructor(left: Operand<*>, right: Operand<*>) : this(Comparison(left.operand, ComparisonOperator.LIKE, right.operand))

    override operator fun not() = (constraint as Comparison).let { NotLike(Comparison(it.leftOperand, ComparisonOperator.NOTLIKE, it.rightOperand)) }
}
