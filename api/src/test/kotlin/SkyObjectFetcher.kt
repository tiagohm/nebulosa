import de.siegmar.fastcsv.reader.NamedCsvRow
import nebulosa.adql.*
import nebulosa.simbad.SimbadService

@Suppress("ArrayInDataClass")
data class SkyObjectFetcher(
    private val ids: LongArray = LongArray(0),
    private val names: List<String> = emptyList(),
    private val magnitudeMax: Double = Double.NaN,
) : Fetcher<List<NamedCsvRow>> {

    private val builder = QueryBuilder()

    init {
        var join: Table = LeftJoin(BASIC_TABLE, IDS_TABLE, arrayOf(OID equal IDS_TABLE.column("oidref")))
        join = LeftJoin(join, FLUX_TABLE, arrayOf(OID equal FLUX_TABLE.column("oidref")))

        if (names.isNotEmpty()) {
            join = InnerJoin(join, IDENT_TABLE, arrayOf(OID equal IDENT_TABLE.column("oidref")))
        }

        builder.add(Distinct)
        builder.addAll(arrayOf(OID, MAIN_ID, OTYPE, RA, DEC, PM_RA, PM_DEC, PLX, RAD_VEL, REDSHIFT))
        builder.addAll(arrayOf(MAG_V, MAG_B, MAG_U, MAG_R, MAG_I, MAG_J, MAG_H, MAG_K))
        builder.addAll(arrayOf(MAJOR_AXIS, MINOR_AXIS, ORIENT, SP_TYPE, IDS_TABLE.column("ids")))
        builder.addAll(arrayOf(RA.isNotNull, DEC.isNotNull))
        builder.add(join)
        builder.add(if (magnitudeMax.isFinite()) (MAG_V lessOrEqual magnitudeMax) or (MAG_B lessOrEqual magnitudeMax) else Ignored)
        if (ids.isNotEmpty()) builder.add(OID includes ids)
        if (names.isNotEmpty()) builder.add(IDENT_TABLE.column("id") includes names)
        builder.add(SortBy(OID))
    }

    override fun fetch(service: SimbadService): List<NamedCsvRow> {
        return service.query(builder.build()).execute().body() ?: emptyList()
    }

    companion object {

        @JvmStatic private val BASIC_TABLE = From("basic").alias("b")
        @JvmStatic private val IDS_TABLE = From("ids").alias("i")
        @JvmStatic private val IDENT_TABLE = From("ident")
        @JvmStatic private val FLUX_TABLE = From("allfluxes").alias("f")
        @JvmStatic private val OID = BASIC_TABLE.column("oid")
        @JvmStatic private val MAIN_ID = BASIC_TABLE.column("main_id")
        @JvmStatic private val OTYPE = BASIC_TABLE.column("otype")
        @JvmStatic private val SP_TYPE = BASIC_TABLE.column("sp_type")
        @JvmStatic private val RA = BASIC_TABLE.column("ra")
        @JvmStatic private val DEC = BASIC_TABLE.column("dec")
        @JvmStatic private val PM_RA = BASIC_TABLE.column("pmra")
        @JvmStatic private val PM_DEC = BASIC_TABLE.column("pmdec")
        @JvmStatic private val PLX = BASIC_TABLE.column("plx_value")
        @JvmStatic private val RAD_VEL = BASIC_TABLE.column("rvz_radvel")
        @JvmStatic private val REDSHIFT = BASIC_TABLE.column("rvz_redshift")
        @JvmStatic private val MAJOR_AXIS = BASIC_TABLE.column("galdim_majaxis")
        @JvmStatic private val MINOR_AXIS = BASIC_TABLE.column("galdim_minaxis")
        @JvmStatic private val ORIENT = BASIC_TABLE.column("galdim_angle")
        @JvmStatic private val MAG_V = FLUX_TABLE.column("V")
        @JvmStatic private val MAG_B = FLUX_TABLE.column("B")
        @JvmStatic private val MAG_U = FLUX_TABLE.column("U")
        @JvmStatic private val MAG_R = FLUX_TABLE.column("R")
        @JvmStatic private val MAG_I = FLUX_TABLE.column("I")
        @JvmStatic private val MAG_J = FLUX_TABLE.column("J")
        @JvmStatic private val MAG_H = FLUX_TABLE.column("H")
        @JvmStatic private val MAG_K = FLUX_TABLE.column("K")
    }
}
