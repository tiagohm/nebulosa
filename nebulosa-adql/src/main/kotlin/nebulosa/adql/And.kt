package nebulosa.adql

import adql.query.constraint.ADQLConstraint
import adql.query.constraint.NotConstraint

data class And internal constructor(override val constraint: ADQLConstraint) : WhereConstraint {

    constructor(vararg contraints: WhereConstraint) : this(LogicalConstraintsGroup("AND")) {
        val logicalConstraint = constraint as LogicalConstraintsGroup
        contraints.forEach { logicalConstraint.add(it.constraint) }
    }

    override fun not(): And {
        return And(if (constraint is NotConstraint) constraint.constraint else NotConstraint(constraint))
    }
}
