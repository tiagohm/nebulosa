import nebulosa.adql.*
import nebulosa.simbad.SimbadService

data class StarIdFetcher(
    private val lastId: Int = 0,
    private val maxId: Int = -1,
    private val limit: Int = 200,
) : Fetcher<IntArray> {

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

    override fun fetch(service: SimbadService): IntArray {
        val rows = service.query(builder.build()).execute().body() ?: return IntArray(0)
        return IntArray(rows.size) { rows[it].getField("oidref").toInt() }
    }

    companion object {

        @JvmStatic private val OTYPES_TABLE = From("otypes").alias("o")
        @JvmStatic private val FLUX_TABLE = From("allfluxes").alias("f")
        @JvmStatic private val MAG_V = FLUX_TABLE.column("V")
        @JvmStatic private val MAG_B = FLUX_TABLE.column("B")
    }
}
