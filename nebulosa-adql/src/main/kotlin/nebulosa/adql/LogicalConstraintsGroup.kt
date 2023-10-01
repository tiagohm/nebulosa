package nebulosa.adql

import adql.query.ClauseConstraints
import adql.query.constraint.ADQLConstraint

internal class LogicalConstraintsGroup(logicalSep: String) : ClauseConstraints(null, logicalSep), ADQLConstraint {

    override fun getCopy() = LogicalConstraintsGroup(defaultSeparator)

    override fun toADQL() = "(${super.toADQL()})"
}
