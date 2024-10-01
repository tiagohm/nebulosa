package nebulosa.adql

import adql.query.from.ADQLJoin
import adql.query.from.CrossJoin as ADQLCrossJoin

data class CrossJoin(override val table: ADQLJoin) : Table {

    constructor(left: Table, right: Table) : this(ADQLCrossJoin(left.table, right.table))
}
