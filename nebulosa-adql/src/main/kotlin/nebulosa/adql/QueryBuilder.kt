package nebulosa.adql

import adql.query.ADQLQuery
import adql.query.ClauseConstraints
import adql.query.ClauseSelect
import java.util.*

class QueryBuilder : LinkedList<QueryClause>() {

    fun build(): Query {
        val query = ADQLQuery()

        val select = filterIsInstance<Operand<*>>()
        query.select = ClauseSelect()
        query.select.setDistinctColumns(Distinct in this)
        query.select.limit = filterIsInstance<Limit>().firstOrNull()?.value ?: -1
        select.forEach { query.select.add(it.operand) }

        val from = filterIsInstance<Table>().first()
        query.from = from.table

        query.where = ClauseConstraints("WHERE", "AND")
        val whereConstraints = filterIsInstance<WhereConstraint>()
        whereConstraints.forEach { query.where.add(it.constraint) }

        filterIsInstance<SortBy>().forEach { query.orderBy.add(it.order) }

        return Query(query)
    }
}

