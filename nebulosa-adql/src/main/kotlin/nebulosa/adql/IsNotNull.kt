package nebulosa.adql

import adql.query.constraint.ADQLConstraint
import adql.query.operand.ADQLColumn
import adql.query.constraint.IsNull as ADQLIsNull

data class IsNotNull(override val constraint: ADQLConstraint) : WhereConstraint {

    constructor(operand: Operand<ADQLColumn>) : this(ADQLIsNull(operand.operand, true))

    override operator fun not() = IsNull(!(constraint as ADQLIsNull))
}
