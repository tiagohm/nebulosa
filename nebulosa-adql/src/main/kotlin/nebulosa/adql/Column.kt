package nebulosa.adql

import adql.query.operand.ADQLColumn

data class Column internal constructor(override val operand: ADQLColumn) : Operand<ADQLColumn> {

    constructor(columnRef: String) : this(ADQLColumn(columnRef))

    constructor(tableName: String?, columnName: String) : this(ADQLColumn(tableName, columnName))

    constructor(schema: String?, tableName: String?, columnName: String) : this(ADQLColumn(schema, tableName, columnName))

    constructor(catalog: String?, schema: String?, tableName: String?, columnName: String) : this(ADQLColumn(catalog, schema, tableName, columnName))

    fun alias(alias: String) = Column(operand.catalogName, operand.schemaName, operand.tableName, "${operand.columnName} AS $alias")
}
