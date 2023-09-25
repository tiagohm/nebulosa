package nebulosa.adql

import adql.query.ClauseADQL
import adql.query.constraint.ADQLConstraint
import adql.query.operand.ADQLOperand
import adql.query.constraint.In as ADQLIn

data class In internal constructor(override val constraint: ADQLConstraint) : WhereConstraint {

    constructor(operand: Operand<*>, values: Array<out Operand<*>>) : this(ADQLIn(operand.operand, values.list()))

    constructor(operand: Operand<*>, values: Iterable<Operand<*>>) : this(ADQLIn(operand.operand, values.list()))

    override operator fun not() = NotIn(!(constraint as ADQLIn))

    companion object {

        @JvmStatic
        private fun Array<out Operand<*>>.list(): ClauseADQL<ADQLOperand> {
            val clause = ClauseADQL<ADQLOperand>()
            forEach { clause.add(it.operand) }
            return clause
        }

        @JvmStatic
        private fun Iterable<Operand<*>>.list(): ClauseADQL<ADQLOperand> {
            val clause = ClauseADQL<ADQLOperand>()
            forEach { clause.add(it.operand) }
            return clause
        }
    }
}
