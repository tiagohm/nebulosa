package nebulosa.adql

import adql.query.constraint.ADQLConstraint
import adql.query.constraint.Exists
import adql.query.constraint.NotConstraint

data class Exists(override val constraint: ADQLConstraint) : WhereConstraint {

    constructor(query: Query) : this(Exists(query.query))

    override fun not(): NotExists {
        return NotExists(if (constraint is NotConstraint) constraint.constraint else constraint)
    }
}
