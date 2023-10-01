package nebulosa.adql

import adql.query.constraint.ADQLConstraint

interface Constraint : QueryClause {

    val constraint: ADQLConstraint
}
