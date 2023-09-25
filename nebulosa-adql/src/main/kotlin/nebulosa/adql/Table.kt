package nebulosa.adql

import adql.query.from.FromContent

sealed interface Table : QueryClause {

    val table: FromContent
}
