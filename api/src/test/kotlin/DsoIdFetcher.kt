import nebulosa.adql.*
import nebulosa.simbad.SimbadService

data class DsoIdFetcher(
    private val catalogType: String,
    private val lastId: Int = 0,
    private val maxId: Int = -1,
    private val limit: Int = 200,
) : Fetcher<IntArray> {

    private val builder = QueryBuilder()

    init {
        val oidRef = IDENT_TABLE.column("oidref")
        val id = IDENT_TABLE.column("id")
        builder.add(Distinct)
        builder.add(Limit(limit))
        builder.add(oidRef.alias("oidref"))
        builder.add(IDENT_TABLE)
        builder.add((oidRef greaterThan lastId) and (id like catalogType))
        if (maxId > 0) builder.add(oidRef lessOrEqual maxId)
        builder.add(SortBy(oidRef))
    }

    override fun fetch(service: SimbadService): IntArray {
        val rows = service.query(builder.build()).execute().body() ?: return IntArray(0)
        return IntArray(rows.size) { rows[it].getField("oidref").toInt() }
    }

    companion object {

        @JvmStatic private val IDENT_TABLE = From("ident").alias("i")
    }
}
