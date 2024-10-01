package nebulosa.adql

import adql.query.constraint.ADQLConstraint
import adql.query.constraint.NotConstraint

data class Or(override val constraint: ADQLConstraint) : WhereConstraint {

    constructor(vararg contraints: WhereConstraint) : this(LogicalConstraintsGroup("OR")) {
        val logicalConstraint = constraint as LogicalConstraintsGroup
        contraints.forEach { logicalConstraint.add(it.constraint) }
    }

    constructor(contraints: Iterable<WhereConstraint>) : this(LogicalConstraintsGroup("OR")) {
        val logicalConstraint = constraint as LogicalConstraintsGroup
        contraints.forEach { logicalConstraint.add(it.constraint) }
    }

    override fun not() = NotOr(NotConstraint(constraint))
}
