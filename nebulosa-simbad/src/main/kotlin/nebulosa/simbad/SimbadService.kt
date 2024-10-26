package nebulosa.simbad

import de.siegmar.fastcsv.reader.CommentStrategy
import de.siegmar.fastcsv.reader.CsvReader
import de.siegmar.fastcsv.reader.NamedCsvRecord
import nebulosa.adql.*
import nebulosa.log.di
import nebulosa.log.loggerFor
import nebulosa.math.*
import nebulosa.retrofit.CSVRecordListConverterFactory
import nebulosa.retrofit.RetrofitService
import nebulosa.skycatalog.SkyObject
import nebulosa.skycatalog.SkyObjectType
import okhttp3.FormBody
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.create
import kotlin.math.max
import kotlin.math.min

/**
 * @see <a href="https://simbad.u-strasbg.fr/simbad/tap/tapsearch.html">Tables</a>
 * @see <a href="https://simbad.u-strasbg.fr/simbad/tap/help/adqlHelp.html">ADQL Cheat sheet</a>
 * @see <a href="http://simbad.u-strasbg.fr/guide/otypes.htx">Object types</a>
 */
class SimbadService(
    url: String = MAIN_URL,
    httpClient: OkHttpClient? = null,
) : RetrofitService(url, httpClient) {

    override val converterFactory = listOf(CSVRecordListConverterFactory(CSV_READER))

    private val service by lazy { retrofit.create<Simbad>() }

    fun query(query: Query): Call<List<NamedCsvRecord>> {
        return query("$query")
    }

    fun query(query: String): Call<List<NamedCsvRecord>> {
        val body = FormBody.Builder()
            .add("request", "doQuery")
            .add("lang", "adql")
            .add("format", "tsv")
            .add("query", query)
            .build()

        LOG.di("query: {}", query)

        return service.query(body)
    }

    fun search(query: Query): List<SimbadEntry> {
        val rows = query(query).execute().body() ?: return emptyList()
        val res = ArrayList<SimbadEntry>()

        fun matchName(name: String): String? {
            for (type in SimbadCatalogType.entries) {
                return type.match(name) ?: continue
            }

            return null
        }

        for (row in rows) {
            val name = row.getField("ids").split("|").mapNotNull(::matchName)
            if (name.isEmpty()) continue
            val id = row.getField("oid").toLong()
            val type = SkyObjectType.parse(row.getField("otype"))!!
            val rightAscensionJ2000 = row.getField("ra").deg
            val declinationJ2000 = row.getField("dec").deg
            val pmRA = row.getField("pmra").toDoubleOrNull()?.mas ?: 0.0
            val pmDEC = row.getField("pmdec").toDoubleOrNull()?.mas ?: 0.0
            val parallax = row.getField("plx_value").toDoubleOrNull() ?: 0.0
            val radialVelocity = row.getField("rvz_radvel").toDoubleOrNull()?.kms ?: 0.0
            val redshift = row.getField("rvz_redshift").toDoubleOrNull() ?: 0.0
            val majorAxis = row.getField("galdim_majaxis").toDoubleOrNull()?.arcmin ?: 0.0
            val minorAxis = row.getField("galdim_minaxis").toDoubleOrNull()?.arcmin ?: 0.0
            val orientation = row.getField("galdim_angle").toDoubleOrNull()?.deg ?: 0.0
            val spType = row.getField("sp_type") ?: ""

            var magnitude = row.getField("V").toDoubleOrNull()
                ?: row.getField("B").toDoubleOrNull()
                ?: row.getField("U").toDoubleOrNull()
                ?: SkyObject.MAGNITUDE_MAX

            if (magnitude >= SkyObject.MAGNITUDE_MAX || !magnitude.isFinite()) {
                magnitude = min(magnitude, row.getField("R").toDoubleOrNull() ?: SkyObject.MAGNITUDE_MAX)
                magnitude = min(magnitude, row.getField("I").toDoubleOrNull() ?: SkyObject.MAGNITUDE_MAX)
                magnitude = min(magnitude, row.getField("J").toDoubleOrNull() ?: SkyObject.MAGNITUDE_MAX)
                magnitude = min(magnitude, row.getField("H").toDoubleOrNull() ?: SkyObject.MAGNITUDE_MAX)
                magnitude = min(magnitude, row.getField("K").toDoubleOrNull() ?: SkyObject.MAGNITUDE_MAX)
            }

            val distance = SkyObject.distanceFor(parallax)
            val constellation = SkyObject.constellationFor(rightAscensionJ2000, declinationJ2000)

            val entity = SimbadEntry(
                id, name, magnitude,
                rightAscensionJ2000, declinationJ2000,
                type, spType, majorAxis, minorAxis, orientation,
                pmRA, pmDEC, parallax.mas, radialVelocity, redshift,
                distance.toLightYears, constellation,
            )

            res.add(entity)
        }

        return res
    }

    fun search(search: SimbadSearch): List<SimbadEntry> {
        val (id, text, rightAscension, declination, radius, types, magnitudeMin, magnitudeMax, constellation, ids, _, limit, sortType, sortDir) = search
        val builder = QueryBuilder()

        var join: Table = LeftJoin(BASIC_TABLE, FLUX_TABLE, arrayOf(OID equal FLUX_TABLE.column("oidref")))
        join = LeftJoin(join, IDS_TABLE, arrayOf(OID equal IDS_TABLE.column("oidref")))

        builder.add(Distinct)
        builder.add(Limit(max(1, min(if (ids.isEmpty()) limit else ids.size, DEFAULT_LIMIT))))
        builder.addAll(arrayOf(OID, MAIN_ID, OTYPE, RA, DEC, PM_RA, PM_DEC, PLX, RAD_VEL, REDSHIFT))
        builder.addAll(arrayOf(MAG_V, MAG_B, MAG_U, MAG_R, MAG_I, MAG_J, MAG_H, MAG_K))
        builder.addAll(arrayOf(MAJOR_AXIS, MINOR_AXIS, ORIENT, SP_TYPE, IDS))
        builder.addAll(arrayOf(RA.isNotNull, DEC.isNotNull))

        if (id > 0) {
            builder.add(OID equal id)
        } else {
            if (ids.isNotEmpty()) {
                builder.add(OID includes ids)
            } else if (!text.isNullOrBlank()) {
                join = LeftJoin(join, IDENT_TABLE, arrayOf(OID equal IDENT_TABLE.column("oidref")))
            }

            if (search.lastID > 0) builder.add(OID greaterThan search.lastID)
            if (radius > 0.0) builder.add(SkyPoint(RA, DEC) contains Circle(rightAscension, declination, radius))
            if (!types.isNullOrEmpty()) builder.add(Or(types.map { OTYPE equal "${it.codes[0]}.." }))
            if (magnitudeMin > -30.0) builder.add((MAG_V greaterOrEqual magnitudeMin) or (MAG_B greaterOrEqual magnitudeMin))
            if (magnitudeMax < 30.0) builder.add((MAG_V lessOrEqual magnitudeMax) or (MAG_B lessOrEqual magnitudeMax))
            if (!text.isNullOrBlank()) builder.add(ID equal text.trim())
            if (constellation != null) builder.add(SkyPoint(RA, DEC) contains ConstellationBoundary(constellation.name))

            if (sortType == SimbadSearch.SortType.OID) builder.add(SortBy(OID, sortDir))
            else if (sortType == SimbadSearch.SortType.MAGNITUDE) {
                builder.add(SortBy(MAG_V, sortDir))
                builder.add(SortBy(MAG_B, sortDir))
            }
        }

        builder.add(join)

        return search(builder.build())
    }

    companion object {

        const val DEFAULT_LIMIT = 5000
        const val MAIN_URL = "https://simbad.cds.unistra.fr/"
        const val ALTERNATIVE_URL = "https://simbad.u-strasbg.fr/"

        private val LOG = loggerFor<SimbadService>()

        private val CSV_READER = CsvReader.builder()
            .fieldSeparator('\t')
            .quoteCharacter('"')
            .commentCharacter('#')
            .commentStrategy(CommentStrategy.SKIP)

        private val BASIC_TABLE = From("basic").alias("b")
        private val FLUX_TABLE = From("allfluxes").alias("f")
        private val IDS_TABLE = From("ids").alias("i")
        private val IDENT_TABLE = From("ident").alias("id")
        private val OID = BASIC_TABLE.column("oid")
        private val MAIN_ID = BASIC_TABLE.column("main_id")
        private val OTYPE = BASIC_TABLE.column("otype")
        private val SP_TYPE = BASIC_TABLE.column("sp_type")
        private val RA = BASIC_TABLE.column("ra")
        private val DEC = BASIC_TABLE.column("dec")
        private val PM_RA = BASIC_TABLE.column("pmra")
        private val PM_DEC = BASIC_TABLE.column("pmdec")
        private val PLX = BASIC_TABLE.column("plx_value")
        private val RAD_VEL = BASIC_TABLE.column("rvz_radvel")
        private val REDSHIFT = BASIC_TABLE.column("rvz_redshift")
        private val MAJOR_AXIS = BASIC_TABLE.column("galdim_majaxis")
        private val MINOR_AXIS = BASIC_TABLE.column("galdim_minaxis")
        private val ORIENT = BASIC_TABLE.column("galdim_angle")
        private val MAG_V = FLUX_TABLE.column("V")
        private val MAG_B = FLUX_TABLE.column("B")
        private val MAG_U = FLUX_TABLE.column("U")
        private val MAG_R = FLUX_TABLE.column("R")
        private val MAG_I = FLUX_TABLE.column("I")
        private val MAG_J = FLUX_TABLE.column("J")
        private val MAG_H = FLUX_TABLE.column("H")
        private val MAG_K = FLUX_TABLE.column("K")
        private val IDS = IDS_TABLE.column("ids")
        private val ID = IDENT_TABLE.column("id")
    }
}
