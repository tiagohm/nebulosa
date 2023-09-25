package nebulosa.adql

import adql.query.constraint.ADQLConstraint
import adql.query.constraint.Comparison
import adql.query.constraint.ComparisonOperator

data class NotLike(override val constraint: ADQLConstraint) : WhereConstraint {

    constructor(left: Operand<*>, right: Operand<*>) : this(Comparison(left.operand, ComparisonOperator.NOTLIKE, right.operand))

    override operator fun not() = (constraint as Comparison).let { Like(Comparison(it.leftOperand, ComparisonOperator.LIKE, it.rightOperand)) }
}
