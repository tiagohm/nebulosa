package nebulosa.adql

import adql.query.constraint.ADQLConstraint
import adql.query.operand.ADQLColumn
import adql.query.constraint.IsNull as ADQLIsNull

data class IsNull internal constructor(override val constraint: ADQLConstraint) : WhereConstraint {

    constructor(operand: Operand<ADQLColumn>) : this(ADQLIsNull(operand.operand))

    override operator fun not() = IsNotNull(!(constraint as ADQLIsNull))
}
