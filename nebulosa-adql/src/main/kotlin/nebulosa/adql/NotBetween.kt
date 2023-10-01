package nebulosa.adql

import adql.query.constraint.ADQLConstraint
import adql.query.constraint.Between as ADQLBetween

data class NotBetween internal constructor(override val constraint: ADQLConstraint) : WhereConstraint {

    constructor(operand: Operand<*>, min: Operand<*>, max: Operand<*>) : this(ADQLBetween(operand.operand, min.operand, max.operand, true))

    override operator fun not() = Between(!(constraint as ADQLBetween))
}
