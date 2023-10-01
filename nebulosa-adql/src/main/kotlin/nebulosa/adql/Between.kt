package nebulosa.adql

import adql.query.constraint.ADQLConstraint
import adql.query.constraint.Between as ADQLBetween

data class Between internal constructor(override val constraint: ADQLConstraint) : WhereConstraint {

    constructor(operand: Operand<*>, min: Operand<*>, max: Operand<*>) : this(ADQLBetween(operand.operand, min.operand, max.operand))

    override operator fun not() = NotBetween(!(constraint as ADQLBetween))
}
