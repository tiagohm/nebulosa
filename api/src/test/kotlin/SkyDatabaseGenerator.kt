import com.fasterxml.jackson.databind.ObjectMapper
import de.siegmar.fastcsv.reader.NamedCsvRow
import nebulosa.adql.*
import nebulosa.math.Angle
import nebulosa.math.Angle.Companion.arcmin
import nebulosa.math.Angle.Companion.deg
import nebulosa.math.Angle.Companion.mas
import nebulosa.math.Velocity
import nebulosa.math.Velocity.Companion.kms
import nebulosa.nova.astrometry.Constellation
import nebulosa.nova.position.ICRF
import nebulosa.simbad.SimbadService
import nebulosa.skycatalog.ClassificationType
import nebulosa.skycatalog.SkyObject
import nebulosa.skycatalog.SkyObjectType
import nebulosa.time.TimeYMDHMS
import nebulosa.time.UTC
import okhttp3.OkHttpClient
import java.io.PrintStream
import java.nio.file.Path
import java.util.concurrent.TimeUnit
import java.util.zip.GZIPOutputStream
import kotlin.io.path.bufferedReader
import kotlin.io.path.outputStream

typealias CatalogNameProvider = Pair<Regex, MatchResult.() -> String>

object SkyDatabaseGenerator {

    @JvmStatic private val STAR_DATABASE_PATH = Path.of("api/data/stars.json.gz")
    @JvmStatic private val DSO_DATABASE_PATH = Path.of("api/data/dsos.json.gz")
    @JvmStatic private val IAU_CSN_PATH = Path.of("api/data/IAU-CSN.txt")
    @JvmStatic private val LOG_PATH = Path.of("api/data/log.txt")

    @JvmStatic private val BASIC_TABLE = From("basic").alias("b")
    @JvmStatic private val IDS_TABLE = From("ids").alias("i")
    @JvmStatic private val IDENT_TABLE = From("ident")
    @JvmStatic private val OTYPES_TABLE = From("otypes").alias("o")
    @JvmStatic private val FLUX_TABLE = From("allfluxes").alias("f")
    @JvmStatic private val OID = BASIC_TABLE.column("oid")
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
    @JvmStatic private val MAG = FLUX_TABLE.column("V")

    @JvmStatic private val HTTP_CLIENT = OkHttpClient.Builder()
        .connectTimeout(5L, TimeUnit.MINUTES)
        .writeTimeout(5L, TimeUnit.MINUTES)
        .readTimeout(5L, TimeUnit.MINUTES)
        .callTimeout(5L, TimeUnit.MINUTES)
        .build()

    @JvmStatic private val SIMBAD_SERVICE = SimbadService(httpClient = HTTP_CLIENT)
    @JvmStatic private val MAPPER = ObjectMapper()

    @JvmStatic private val STAR_CATALOG_TYPES = listOf<CatalogNameProvider>(
        "NAME\\s+(.*)".toRegex() to { groupValues[1].trim() },
        "\\*\\s+(.*)".toRegex() to { groupValues[1].trim() },
        "HD\\s+(\\w*)".toRegex() to { "HD " + groupValues[1].uppercase() },
        "HR\\s+(\\w*)".toRegex() to { "HR " + groupValues[1].uppercase() },
        "HIP\\s+(\\w*)".toRegex() to { "HIP " + groupValues[1].uppercase() },
        "NGC\\s+(\\w{1,5})".toRegex() to { "NGC " + groupValues[1].uppercase() },
        "IC\\s+(\\w{1,5})".toRegex() to { "IC " + groupValues[1].uppercase() },
    )

    @JvmStatic private val DSO_CATALOG_TYPES = listOf<CatalogNameProvider>(
        STAR_CATALOG_TYPES[0],
        "NGC\\s+(\\d{1,4})".toRegex() to { "NGC " + groupValues[1] },
        "IC\\s+(\\d{1,4})".toRegex() to { "IC " + groupValues[1] },
        "GUM\\s+(\\d{1,4})".toRegex() to { "GUM " + groupValues[1] },
        "M\\s+(\\d{1,3})".toRegex() to { "M " + groupValues[1] },
        "Barnard\\s+(\\d{1,3})".toRegex() to { "Barnard " + groupValues[1] },
        "LBN\\s+(\\d{1,4})".toRegex() to { "LBN " + groupValues[1] },
        "LDN\\s+(\\d{1,4})".toRegex() to { "LDN " + groupValues[1] },
        "RCW\\s+(\\d{1,4})".toRegex() to { "RCW " + groupValues[1] },
        "SH\\s+2-(\\d{1,3})".toRegex() to { "SH 2-" + groupValues[1] },
        "Ced\\s+(\\d{1,3})".toRegex() to { "Ced " + groupValues[1] },
        "UGC\\s+(\\d{1,5})".toRegex() to { "UGC " + groupValues[1] },
        "APG\\s+(\\d{1,3})".toRegex() to { "APG " + groupValues[1] },
        "HCG\\s+(\\d{1,3})".toRegex() to { "HCG " + groupValues[1] },
        "VV\\s+(\\d{1,4})".toRegex() to { "VV " + groupValues[1] },
        "VdBH\\s+(\\d{1,2})".toRegex() to { "VdBH " + groupValues[1] },
        "DWB\\s+(\\d{1,3})".toRegex() to { "DWB " + groupValues[1] },
        "LEDA\\s+(\\d{1,7})".toRegex() to { "LEDA " + groupValues[1] },
        "Cl\\s+([\\w-]+)\\s+(\\d{1,5})".toRegex() to { groupValues[1] + " " + groupValues[2] },
    )

    @JvmStatic private val DSO_CATALOG_TYPES_LIKE = mapOf(
        "M %" to Double.NaN,
        "NGC %" to Double.NaN,
        "IC %" to Double.NaN,
        "Cl %" to Double.NaN,
        "Gum %" to Double.NaN,
        "Barnard %" to Double.NaN,
        "LBN %" to Double.NaN,
        "LDN %" to Double.NaN,
        "RCW %" to Double.NaN,
        "SH %" to Double.NaN,
        "Ced %" to Double.NaN,
        "UGC %" to 16.0,
        "APG %" to 16.0,
        "HCG %" to 16.0,
        "VV %" to 16.0,
        "VdBH %" to Double.NaN,
        "DWB %" to Double.NaN,
        "NAME %" to Double.NaN,
    )

    @JvmStatic
    fun main(args: Array<String>) {
        val names = LinkedHashSet<String>(8)
        var lastId = 0L

        val iauNames = ArrayList<String>(451)
        val iauNamesMagnitude = HashMap<String, Double>(451)

        val fetchStars = true
        val fetchDSOs = true

        val logger = PrintStream(LOG_PATH.outputStream().buffered())

        IAU_CSN_PATH
            .bufferedReader()
            .use { reader ->
                reader.lineSequence()
                    .filter { it.isNotEmpty() && !it.startsWith("#") }
                    .forEach {
                        val name = it.substring(0..17).trim()
                        val magnitude = it.substring(81..85).toDoubleOrNull() ?: SkyObject.UNKNOWN_MAGNITUDE
                        iauNames.add(name)
                        iauNamesMagnitude[name] = magnitude
                    }
            }

        fun String.names(
            provider: Iterable<CatalogNameProvider>,
            useIAU: Boolean = false,
        ): Double {
            names.clear()

            val splittedNames = split("|").toMutableList()
            var magnitude = SkyObject.UNKNOWN_MAGNITUDE

            for (type in provider) {
                val namesIterator = splittedNames.iterator()

                while (namesIterator.hasNext()) {
                    val m = type.first.matchEntire(namesIterator.next()) ?: continue
                    val name = type.second(m)
                    names.add(name)
                    namesIterator.remove()

                    if (useIAU && type === STAR_CATALOG_TYPES[0] && name in iauNames) {
                        iauNames.remove(name)
                        magnitude = iauNamesMagnitude[name]!!
                    }
                }
            }

            if (splittedNames.isNotEmpty()) {
                logger.println("unsupported names. [$lastId]: $splittedNames")
            }

            return magnitude
        }

        var join = LeftJoin(BASIC_TABLE, IDS_TABLE, arrayOf(OID equal IDS_TABLE.column("oidref")))
        join = LeftJoin(join, FLUX_TABLE, arrayOf(OID equal FLUX_TABLE.column("oidref")))

        val builder = QueryBuilder()
        builder.add(OID greaterThan lastId)
        builder.add(Limit(1000))
        builder.add(join)
        builder.addAll(arrayOf(OID, OTYPE, RA, DEC, PM_RA, PM_DEC, PLX, RAD_VEL, REDSHIFT, MAG))
        builder.addAll(arrayOf(MAJOR_AXIS, MINOR_AXIS, ORIENT, SP_TYPE, IDS_TABLE.column("ids")))
        builder.addAll(arrayOf(RA.isNotNull, DEC.isNotNull))
        builder.add(SortBy(OID))
        builder.add(Distinct)

        builder.add(Ignored)
        builder.add(Ignored)

        val utc = UTC(TimeYMDHMS(2023, 9, 29, 12))
        val json = HashMap<Long, Map<String, Any?>>(32000)
        val skyObjectTypes = HashSet<SkyObjectType>(SkyObjectType.entries.size)

        fun NamedCsvRow.makeSkyObject(
            isDSO: Boolean,
            provider: Iterable<CatalogNameProvider>,
        ) {
            lastId = getField("oid").toLong()

            if (lastId in json) return

            val ids = getField("ids")
            val magnitudeFromIAU = ids.names(provider)

            if (names.isEmpty()) {
                logger.println("no supported names. [$lastId]: $ids")
                return
            }

            val type = SkyObjectType.parse(getField("otype"))

            if (type == null) {
                logger.println("unknown type. ${getField("otype")}")
                return
            } else if (!isDSO && type.classification != ClassificationType.STAR) {
                logger.println("unsupported star type. $type")
                return
            }

            skyObjectTypes.add(type)

            val ra = getField("ra").deg
            val dec = getField("dec").deg
            val pmRA = getField("pmra").toDoubleOrNull()?.mas ?: Angle.ZERO
            val pmDEC = getField("pmdec").toDoubleOrNull()?.mas ?: Angle.ZERO
            val plx = getField("plx_value").toDoubleOrNull()?.mas ?: Angle.ZERO
            val radVel = getField("rvz_radvel").toDoubleOrNull()?.kms ?: Velocity.ZERO
            val redshift = getField("rvz_redshift").toDoubleOrNull() ?: 0.0
            val majAxis = getField("galdim_majaxis").toDoubleOrNull()?.arcmin ?: Angle.ZERO
            val minAxis = getField("galdim_minaxis").toDoubleOrNull()?.arcmin ?: Angle.ZERO
            val orient = getField("galdim_angle").toDoubleOrNull()?.deg ?: Angle.ZERO
            val spType = getField("sp_type") ?: ""
            val mag = getField("V").toDoubleOrNull() ?: magnitudeFromIAU
            val constellation = utc.computeConstellation(ra, dec)

            json[lastId] = mapOf(
                "id" to lastId, "type" to type, "names" to names.joinToString("|"),
                "ra" to ra.value, "dec" to dec.value, "mag" to mag,
                "pmRA" to pmRA.value, "pmDEC" to pmDEC.value,
                "plx" to plx.value, "rv" to radVel.value, "redshift" to redshift,
                "majAxis" to majAxis.value, "minAxis" to minAxis.value, "orient" to orient.value,
                "spType" to spType, "const" to constellation,
            )
        }

        // STARS. ~23726 objects.

        if (fetchStars) {
            builder[2] = LeftJoin(join, OTYPES_TABLE, arrayOf(OID equal OTYPES_TABLE.column("oidref")))

            builder[builder.size - 2] = OTYPES_TABLE.column("otype") equal "*"
            builder[builder.size - 1] = MAG lessOrEqual 7.4

            while (true) {
                val query = builder.build()
                val rows = SIMBAD_SERVICE.query(query).execute().body()?.ifEmpty { null } ?: break

                for (row in rows) {
                    row.makeSkyObject(false, STAR_CATALOG_TYPES)
                }

                if (rows.size < 1000) break

                builder[0] = OID greaterThan lastId

                logger.flush()
            }

            logger.println("remaining IAU names. size=${iauNames.size}: $iauNames")
            logger.flush()

            builder[0] = OID greaterThan 0L
            builder[builder.size - 1] = IDENT_TABLE.column("id") includes iauNames
            builder[2] = InnerJoin(join, IDENT_TABLE, arrayOf(OID equal IDENT_TABLE.column("oidref")))

            val query = builder.build()
            val rows = SIMBAD_SERVICE.query(query).execute().body()!!

            for (row in rows) {
                row.makeSkyObject(false, STAR_CATALOG_TYPES)
            }

            logger.println("remaining IAU names. size=${iauNames.size}, names=$iauNames")

            logger.println("stars: ${json.size}")
            logger.println("star types: $skyObjectTypes")

            GZIPOutputStream(STAR_DATABASE_PATH.outputStream())
                .use { MAPPER.writeValue(it, json.values) }
        }

        logger.flush()

        // DSOS. ~16343 objects.

        if (fetchDSOs) {
            json.clear()
            skyObjectTypes.clear()

            builder[2] = join

            builder[builder.size - 2] = Ignored
            builder[0] = OID greaterThan 0L

            val idBuilder = QueryBuilder()
            val oidRef = IDENT_TABLE.column("oidref")
            val id = IDENT_TABLE.column("id")
            idBuilder.add(Ignored)
            idBuilder.add(oidRef)
            idBuilder.add(Limit(200))
            idBuilder.add(From(IDENT_TABLE))
            idBuilder.add(SortBy(oidRef))
            idBuilder.add(Distinct)
            idBuilder.add(Ignored)

            for (catalogType in DSO_CATALOG_TYPES_LIKE) {
                lastId = 0L

                while (true) {
                    idBuilder[idBuilder.size - 1] = (oidRef greaterThan lastId) and (id like catalogType.key)
                    var query = idBuilder.build()
                    var rows = SIMBAD_SERVICE.query(query).execute().body()?.ifEmpty { null } ?: break
                    val ids = IntArray(rows.size) { rows[it].getField("oidref").toInt() }

                    builder[builder.size - 2] = if (catalogType.value.isFinite()) (MAG lessOrEqual catalogType.value) else Ignored
                    builder[builder.size - 1] = OID includes ids

                    query = builder.build()
                    rows = SIMBAD_SERVICE.query(query).execute().body()?.ifEmpty { null } ?: break

                    for (row in rows) {
                        row.makeSkyObject(true, DSO_CATALOG_TYPES)
                    }
                }

                logger.flush()
            }

            logger.println("dsos: ${json.size}")
            logger.println("dso types: $skyObjectTypes")

            GZIPOutputStream(DSO_DATABASE_PATH.outputStream())
                .use { MAPPER.writeValue(it, json.values) }
        }

        logger.flush()
        logger.close()
    }

    @JvmStatic
    private fun UTC.computeConstellation(rightAscension: Angle, declination: Angle): Constellation {
        return Constellation.find(ICRF.equatorial(rightAscension, declination, time = this))
    }
}
