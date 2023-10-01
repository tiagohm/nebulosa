package nebulosa.adql

import adql.query.operand.ADQLOperand

sealed interface Operand<out T : ADQLOperand> : QueryClause {

    val operand: T
}
