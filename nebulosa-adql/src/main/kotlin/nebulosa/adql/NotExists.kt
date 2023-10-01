package nebulosa.adql

import adql.query.constraint.ADQLConstraint
import adql.query.constraint.NotConstraint
import adql.query.constraint.Exists as ADQLExists

data class NotExists internal constructor(override val constraint: ADQLConstraint) : WhereConstraint {

    constructor(query: Query) : this(NotConstraint(ADQLExists(query.query)))

    override fun not(): Exists {
        return Exists(if (constraint is NotConstraint) constraint.constraint else constraint)
    }
}
