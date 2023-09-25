package nebulosa.adql

import adql.query.constraint.ADQLConstraint
import adql.query.constraint.NotConstraint

data class NotOr internal constructor(override val constraint: ADQLConstraint) : WhereConstraint {

    constructor(vararg contraints: WhereConstraint) : this(NotConstraint(LogicalConstraintsGroup("OR"))) {
        val logicalConstraint = constraint as LogicalConstraintsGroup
        contraints.forEach { logicalConstraint.add(it.constraint) }
    }

    override fun not() = Or((constraint as NotConstraint).constraint)
}
