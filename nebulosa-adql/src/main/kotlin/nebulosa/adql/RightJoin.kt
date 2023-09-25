package nebulosa.adql

import adql.query.ClauseConstraints
import adql.query.from.ADQLJoin
import adql.query.from.OuterJoin
import adql.query.from.OuterJoin as ADQLOuterJoin

data class RightJoin internal constructor(override val table: ADQLJoin) : Table {

    constructor(left: Table, right: Table) : this(ADQLOuterJoin(left.table, right.table, OuterJoin.OuterType.RIGHT))

    constructor(left: Table, right: Table, condition: Array<out WhereConstraint>)
            : this(ADQLOuterJoin(left.table, right.table, OuterJoin.OuterType.RIGHT)) {
        table.joinCondition = ClauseConstraints(null, "AND")
        condition.forEach { table.joinCondition.add(it.constraint) }
    }

    constructor(left: Table, right: Table, columns: Array<out Column>) : this(ADQLOuterJoin(left.table, right.table, OuterJoin.OuterType.RIGHT)) {
        table.setJoinedColumns(columns.map(Column::operand))
    }
}
