package nebulosa.adql

import adql.query.constraint.ADQLConstraint
import adql.query.constraint.Comparison
import adql.query.constraint.ComparisonOperator
import adql.query.operand.NumericConstant
import adql.query.operand.function.geometry.ContainsFunction
import adql.query.operand.function.geometry.GeometryFunction

data class Contains internal constructor(override val constraint: ADQLConstraint) : WhereConstraint {

    private constructor(left: GeometryFunction.GeometryValue<GeometryFunction>, right: GeometryFunction.GeometryValue<GeometryFunction>) : this(
        Comparison(ContainsFunction(left, right), ComparisonOperator.EQUAL, NumericConstant(1L))
    )

    constructor(left: Column, right: Region) : this(GeometryFunction.GeometryValue(left.operand), GeometryFunction.GeometryValue(right.operand))

    constructor(left: Region, right: Region) : this(GeometryFunction.GeometryValue(left.operand), GeometryFunction.GeometryValue(right.operand))

    override operator fun not(): NotContains {
        val comparison = constraint as Comparison
        return NotContains(Comparison(comparison.leftOperand, ComparisonOperator.EQUAL, NumericConstant(0L)))
    }
}
