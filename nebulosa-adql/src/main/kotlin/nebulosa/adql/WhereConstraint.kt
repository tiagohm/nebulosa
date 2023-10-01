package nebulosa.adql

sealed interface WhereConstraint : Constraint {

    operator fun not(): WhereConstraint
}
