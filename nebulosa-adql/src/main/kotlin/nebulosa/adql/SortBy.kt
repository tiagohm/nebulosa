package nebulosa.adql

import adql.query.ADQLOrder

data class SortBy(val order: ADQLOrder) : QueryClause {

    constructor(column: Column, direction: SortDirection = SortDirection.ASCENDING)
            : this(ADQLOrder(column.operand.columnName, direction == SortDirection.DESCENDING))
}
