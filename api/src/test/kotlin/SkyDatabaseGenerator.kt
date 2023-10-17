import com.fasterxml.jackson.databind.ObjectMapper
import de.siegmar.fastcsv.reader.NamedCsvReader
import de.siegmar.fastcsv.reader.NamedCsvRow
import nebulosa.api.atlas.DeepSkyObjectEntity
import nebulosa.api.atlas.StarEntity
import nebulosa.common.concurrency.CountUpDownLatch
import nebulosa.io.resource
import nebulosa.log.loggerFor
import nebulosa.math.*
import nebulosa.simbad.SimbadCatalogType
import nebulosa.simbad.SimbadService
import nebulosa.skycatalog.ClassificationType
import nebulosa.skycatalog.SkyObject
import nebulosa.skycatalog.SkyObjectType
import nebulosa.time.TimeYMDHMS
import nebulosa.time.UTC
import okhttp3.OkHttpClient
import java.io.InputStreamReader
import java.nio.file.Path
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.zip.GZIPOutputStream
import kotlin.io.path.bufferedReader
import kotlin.io.path.outputStream
import kotlin.math.min

typealias CatalogNameProvider = Pair<Regex, (String) -> String?>

// TODO: Herschel Catalog
// TODO: Dunlop Catalog: https://www.docdb.net/tutorials/dunlop_catalogue.php

object SkyDatabaseGenerator {

    @JvmStatic private val STAR_DATABASE_PATH = Path.of("api/data/stars.json.gz")
    @JvmStatic private val DSO_DATABASE_PATH = Path.of("api/data/dsos.json.gz")
    @JvmStatic private val IAU_CSN_PATH = Path.of("api/data/IAU-CSN.txt")

    @JvmStatic private val LOG = loggerFor<SkyDatabaseGenerator>()

    @JvmStatic private val HTTP_CLIENT = OkHttpClient.Builder()
        .connectTimeout(5L, TimeUnit.MINUTES)
        .writeTimeout(5L, TimeUnit.MINUTES)
        .readTimeout(5L, TimeUnit.MINUTES)
        .callTimeout(5L, TimeUnit.MINUTES)
        .build()

    @JvmStatic private val SIMBAD_SERVICE = SimbadService(httpClient = HTTP_CLIENT)
    @JvmStatic private val MAPPER = ObjectMapper()

    @JvmStatic private val STAR_CATALOG_TYPES: List<CatalogNameProvider> = SimbadCatalogType.entries
        .filter { it.isStar }
        .map { it.regex to it::match }

    @JvmStatic private val DSO_CATALOG_TYPES: List<CatalogNameProvider> = SimbadCatalogType.entries
        .filter { it.isDSO }
        .map { it.regex to it::match }

    @JvmStatic private val NUMBER_OF_CPUS = Runtime.getRuntime().availableProcessors()
    @JvmStatic private val EXECUTOR_SERVICE = Executors.newFixedThreadPool(NUMBER_OF_CPUS)

    @JvmStatic private val CSV_READER = NamedCsvReader.builder()
        .fieldSeparator(',')
        .quoteCharacter('"')
        .commentCharacter('#')
        .skipComments(true)

    @JvmStatic private val CALDWELL = resource("Caldwell.csv")!!
        .use { stream ->
            CSV_READER.build(InputStreamReader(stream, Charsets.UTF_8))
                .associate { it.getField("NGC number").ifEmpty { it.getField("Common name") } to it.getField("Caldwell number") }
        }

    @JvmStatic private val BENNETT = resource("Bennett.csv")!!
        .use { stream ->
            CSV_READER.build(InputStreamReader(stream, Charsets.UTF_8))
                .associate { it.getField("NGC") to it.getField("Bennett") }
        }

    @JvmStatic private val DUNLOP = resource("Dunlop.csv")!!
        .use { stream ->
            CSV_READER.build(InputStreamReader(stream, Charsets.UTF_8))
                .associate { it.getField("NGC") to it.getField("Dunlop") }
        }

    @JvmStatic
    fun main(args: Array<String>) {
        val names = LinkedHashSet<String>(8)

        val iauNames = ArrayList<String>(451)
        val iauNamesMagnitude = HashMap<String, Double>(451)

        val fetchStars = true
        val fetchDSOs = true

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
                    val name = type.second(namesIterator.next()) ?: continue
                    namesIterator.remove()

                    if (names.add(name)) {
                        if (name in CALDWELL) {
                            names.add("Caldwell ${CALDWELL[name]}")
                        }
                        if (name in BENNETT) {
                            names.add("Bennett ${BENNETT[name]}")
                        }
                        if (name in DUNLOP) {
                            names.add("Dunlop ${DUNLOP[name]}")
                        }

                        if (useIAU && type === STAR_CATALOG_TYPES[0] && name in iauNames) {
                            iauNames.remove(name)
                            magnitude = iauNamesMagnitude[name]!!
                        }
                    }
                }
            }

            return magnitude
        }

        val currentTime = UTC(TimeYMDHMS(2023, 10, 5, 12))
        val data = HashMap<Long, SkyObject>(32000)
        val skyObjectTypes = HashSet<SkyObjectType>(SkyObjectType.entries.size)

        val maxOID = SIMBAD_SERVICE.query("SELECT max(b.oid) FROM basic b")
            .execute().body()!![0].getField("MAX").toLong()

        LOG.info("max oid: {}", maxOID)

        fun NamedCsvRow.makeSkyObject(
            isDSO: Boolean,
            provider: Iterable<CatalogNameProvider>,
            isStarDSO: Boolean = true,
        ): Long {
            val id = getField("oid").toLong()

            if (id in data) return id

            val ids = getField("ids")
            val magnitudeFromIAU = ids.names(provider, !isDSO)

            if (names.isEmpty()) return id

            val type = SkyObjectType.parse(getField("otype"))

            if (type == null) {
                LOG.info("unknown type: {} {}", getField("otype"), getField("main_id"))
                return id
            } else if (!isDSO && type.classification != ClassificationType.STAR) {
                LOG.info("unsupported star type: {} {}", type, getField("main_id"))
                return id
            } else if (isDSO && !isStarDSO && type.classification == ClassificationType.STAR) {
                LOG.info("unsupported DSO type: {} {}", type, getField("main_id"))
                return id
            }

            skyObjectTypes.add(type)

            val rightAscensionJ2000 = getField("ra").deg
            val declinationJ2000 = getField("dec").deg
            val pmRA = getField("pmra").toDoubleOrNull()?.mas ?: 0.0
            val pmDEC = getField("pmdec").toDoubleOrNull()?.mas ?: 0.0
            val parallax = getField("plx_value").toDoubleOrNull() ?: 0.0
            val radialVelocity = getField("rvz_radvel").toDoubleOrNull()?.kms ?: 0.0
            val redshift = getField("rvz_redshift").toDoubleOrNull() ?: 0.0
            val majorAxis = getField("galdim_majaxis").toDoubleOrNull()?.arcmin ?: 0.0
            val minorAxis = getField("galdim_minaxis").toDoubleOrNull()?.arcmin ?: 0.0
            val orientation = getField("galdim_angle").toDoubleOrNull()?.deg ?: 0.0
            val spType = getField("sp_type") ?: ""

            var magnitude = getField("V").toDoubleOrNull()
                ?: getField("B").toDoubleOrNull()
                ?: getField("U").toDoubleOrNull()
                ?: magnitudeFromIAU

            if (magnitude >= SkyObject.UNKNOWN_MAGNITUDE || !magnitude.isFinite()) {
                magnitude = min(magnitude, getField("R").toDoubleOrNull() ?: SkyObject.UNKNOWN_MAGNITUDE)
                magnitude = min(magnitude, getField("I").toDoubleOrNull() ?: SkyObject.UNKNOWN_MAGNITUDE)
                magnitude = min(magnitude, getField("J").toDoubleOrNull() ?: SkyObject.UNKNOWN_MAGNITUDE)
                magnitude = min(magnitude, getField("H").toDoubleOrNull() ?: SkyObject.UNKNOWN_MAGNITUDE)
                magnitude = min(magnitude, getField("K").toDoubleOrNull() ?: SkyObject.UNKNOWN_MAGNITUDE)
            }

            val distance = if (parallax > 0.0) (1000.0 * ONE_PARSEC) / parallax else 0.0
            val constellation = SkyObject.computeConstellation(rightAscensionJ2000, declinationJ2000, currentTime)

            data[id] = if (isDSO) DeepSkyObjectEntity(
                id, names.joinToString("|"), magnitude,
                rightAscensionJ2000, declinationJ2000,
                type, majorAxis, minorAxis, orientation,
                pmRA, pmDEC, parallax.mas, radialVelocity, redshift,
                distance, constellation,
            ) else StarEntity(
                id, names.joinToString("|"), magnitude,
                rightAscensionJ2000, declinationJ2000,
                type, spType, pmRA, pmDEC,
                parallax.mas, radialVelocity, redshift,
                distance, constellation,
            )

            return id
        }

        // STARS. ~23735 objects.

        val stepSize = maxOID / NUMBER_OF_CPUS

        if (fetchStars) {
            val latch = CountUpDownLatch()

            for (i in 0L until maxOID step stepSize) {
                latch.countUp()

                EXECUTOR_SERVICE.submit {
                    var lastId = i

                    while (true) {
                        val idFetcher = StarIdFetcher(lastId, i + stepSize)
                        val ids = idFetcher.fetch(SIMBAD_SERVICE)

                        if (ids.isEmpty()) break

                        val starFetcher = SkyObjectFetcher(ids)
                        val rows = starFetcher.fetch(SIMBAD_SERVICE)

                        LOG.info("rows. size={}", rows.size)

                        if (rows.isEmpty()) {
                            lastId = ids.last()
                            continue
                        }

                        synchronized(data) {
                            for (row in rows) {
                                lastId = row.makeSkyObject(false, STAR_CATALOG_TYPES)
                            }
                        }
                    }

                    latch.countDown()
                }
            }

            latch.await()

            LOG.info("remaining IAU names. size={}: {}", iauNames.size, iauNames)

            // NAMED STARS. ~451 objects.

            val starFetcher = SkyObjectFetcher(names = iauNames)
            val rows = starFetcher.fetch(SIMBAD_SERVICE)

            LOG.info("rows. size={}", rows.size)

            for (row in rows) {
                row.makeSkyObject(false, STAR_CATALOG_TYPES)
            }

            LOG.info("remaining IAU names. size={}: {}", iauNames.size, iauNames)

            LOG.info("stars: {}", data.size)
            LOG.info("star types: {}", skyObjectTypes)

            GZIPOutputStream(STAR_DATABASE_PATH.outputStream())
                .use { MAPPER.writeValue(it, data.values) }
        }

        // DSOS. ~30263 objects.

        if (fetchDSOs) {
            data.clear()
            skyObjectTypes.clear()

            val latch = CountUpDownLatch()

            val catalogTypes = listOf(
                "M %" to Double.NaN, "NGC %" to Double.NaN,
                "IC %" to Double.NaN, "Cl %" to Double.NaN,
                "Gum %" to Double.NaN, "Barnard %" to Double.NaN,
                "LBN %" to Double.NaN, "LDN %" to Double.NaN,
                "RCW %" to Double.NaN, "SH %" to Double.NaN,
                "Ced %" to Double.NaN, "UGC %" to 18.0,
                "APG %" to Double.NaN, "HCG %" to 18.0,
                "VV %" to 16.0, "VdBH %" to Double.NaN,
                "DWB %" to Double.NaN, "NAME %" to Double.NaN,
            )

            for (i in 0 until maxOID step stepSize) {
                for (catalogType in catalogTypes) {
                    latch.countUp()

                    EXECUTOR_SERVICE.submit {
                        var lastId = i

                        while (true) {
                            val idFetcher = DsoIdFetcher(catalogType.first, lastId, i + stepSize)
                            val ids = idFetcher.fetch(SIMBAD_SERVICE)

                            if (ids.isEmpty()) break

                            val dsoFetcher = SkyObjectFetcher(ids, magnitudeMax = catalogType.second)
                            val rows = dsoFetcher.fetch(SIMBAD_SERVICE)

                            LOG.info("rows. size={}", rows.size)

                            if (rows.isEmpty()) {
                                lastId = ids.last()
                                continue
                            }

                            val isStarDSO = catalogType !== catalogTypes.last()

                            synchronized(data) {
                                for (row in rows) {
                                    lastId = row.makeSkyObject(true, DSO_CATALOG_TYPES, isStarDSO)
                                }
                            }
                        }

                        latch.countDown()
                    }
                }
            }

            latch.await()

            LOG.info("dsos: {}", data.size)
            LOG.info("dso types: {}", skyObjectTypes)

            GZIPOutputStream(DSO_DATABASE_PATH.outputStream())
                .use { MAPPER.writeValue(it, data.values) }

            EXECUTOR_SERVICE.shutdown()
        }
    }
}
