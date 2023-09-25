package nebulosa.adql

import adql.query.ClauseConstraints
import adql.query.from.ADQLJoin
import adql.query.from.InnerJoin as ADQLInnerJoin

data class InnerJoin internal constructor(override val table: ADQLJoin) : Table {

    constructor(left: Table, right: Table) : this(ADQLInnerJoin(left.table, right.table))

    constructor(left: Table, right: Table, condition: Array<out WhereConstraint>) : this(ADQLInnerJoin(left.table, right.table)) {
        table.joinCondition = ClauseConstraints(null, "AND")
        condition.forEach { table.joinCondition.add(it.constraint) }
    }

    constructor(left: Table, right: Table, columns: Array<out Column>) : this(ADQLInnerJoin(left.table, right.table)) {
        table.setJoinedColumns(columns.map(Column::operand))
    }
}
