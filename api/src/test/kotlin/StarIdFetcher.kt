import nebulosa.adql.*
import nebulosa.simbad.SimbadService

data class StarIdFetcher(
    private val lastId: Long = 0L,
    private val maxId: Long = -1L,
    private val limit: Int = 200,
) : Fetcher<LongArray> {

    private val builder = QueryBuilder()

    init {
        val oidRef = OTYPES_TABLE.column("oidref")
        val otype = OTYPES_TABLE.column("otype")
        builder.add(Distinct)
        builder.add(Limit(limit))
        builder.add(oidRef.alias("oidref"))
        val join = InnerJoin(
            OTYPES_TABLE, FLUX_TABLE,
            arrayOf(
                oidRef equal FLUX_TABLE.column("oidref"),
                MAG_V lessOrEqual 7.4,
                oidRef greaterThan lastId,
                otype equal "*",
            )
        )
        builder.add(join)
        if (maxId > 0) builder.add(oidRef lessOrEqual maxId)
        builder.add(SortBy(oidRef))
    }

    override fun fetch(service: SimbadService): LongArray {
        val rows = service.query(builder.build()).execute().body() ?: return LongArray(0)
        return LongArray(rows.size) { rows[it].getField("oidref").toLong() }
    }

    companion object {

        @JvmStatic private val OTYPES_TABLE = From("otypes").alias("o")
        @JvmStatic private val FLUX_TABLE = From("allfluxes").alias("f")
        @JvmStatic private val MAG_V = FLUX_TABLE.column("V")
        @JvmStatic private val MAG_B = FLUX_TABLE.column("B")
    }
}
