package nebulosa.adql

import adql.query.from.ADQLTable

data class From(override val table: ADQLTable) : Table {

    constructor(table: String) : this(ADQLTable(table))

    constructor(schema: String, table: String) : this(ADQLTable(schema, table))

    constructor(catalog: String, schema: String, table: String) : this(ADQLTable(catalog, schema, table))

    constructor(query: Query) : this(ADQLTable(query.query))

    constructor(from: From) : this(ADQLTable(from.table))

    fun alias(alias: String) = apply { table.alias = alias }

    fun column(name: String) = Column(table.name, name)
}
