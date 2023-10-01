import nebulosa.adql.*
import nebulosa.simbad.SimbadService

data class DsoIdFetcher(
    private val catalogType: String,
    private val lastId: Long = 0L,
    private val maxId: Long = -1L,
    private val limit: Int = 200,
) : Fetcher<LongArray> {

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

    override fun fetch(service: SimbadService): LongArray {
        val rows = service.query(builder.build()).execute().body() ?: return LongArray(0)
        return LongArray(rows.size) { rows[it].getField("oidref").toLong() }
    }

    companion object {

        @JvmStatic private val IDENT_TABLE = From("ident").alias("i")
    }
}
