import com.fasterxml.jackson.databind.ObjectMapper
import de.siegmar.fastcsv.reader.NamedCsvRow
import nebulosa.api.atlas.SkyObjectConverter
import nebulosa.common.concurrency.CountUpDownLatch
import nebulosa.json.modules.JsonModule
import nebulosa.log.loggerFor
import nebulosa.math.Angle
import nebulosa.math.Angle.Companion.arcmin
import nebulosa.math.Angle.Companion.deg
import nebulosa.math.Angle.Companion.mas
import nebulosa.math.Velocity
import nebulosa.math.Velocity.Companion.kms
import nebulosa.simbad.SimbadService
import nebulosa.skycatalog.*
import nebulosa.time.TimeYMDHMS
import nebulosa.time.UTC
import okhttp3.OkHttpClient
import java.nio.file.Path
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.zip.GZIPOutputStream
import kotlin.io.path.bufferedReader
import kotlin.io.path.outputStream
import kotlin.math.min

typealias CatalogNameProvider = Pair<Regex, MatchResult.() -> String>

// TODO: Caldwell Catalog
// TODO: Herschel Catalog
// TODO: Bennett Catalog: https://www.docdb.net/tutorials/bennett_catalogue.php
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

    @JvmStatic private val MAPPER = ObjectMapper().apply {
        registerModule(JsonModule(listOf(SkyObjectConverter()), emptyList()))
    }

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
        "APG\\s+(\\d{1,3})".toRegex() to { "Arp " + groupValues[1] },
        "HCG\\s+(\\d{1,3})".toRegex() to { "HCG " + groupValues[1] },
        "VV\\s+(\\d{1,4})".toRegex() to { "VV " + groupValues[1] },
        "VdBH\\s+(\\d{1,2})".toRegex() to { "VdBH " + groupValues[1] },
        "DWB\\s+(\\d{1,3})".toRegex() to { "DWB " + groupValues[1] },
        "LEDA\\s+(\\d{1,7})".toRegex() to { "LEDA " + groupValues[1] },
        "Cl\\s+([\\w-]+)\\s+(\\d{1,5})".toRegex() to { groupValues[1] + " " + groupValues[2] },
    )

    @JvmStatic private val DSO_CATALOG_TYPES_LIKE = listOf(
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
        "APG %" to Double.NaN,
        "HCG %" to 16.0,
        "VV %" to 16.0,
        "VdBH %" to Double.NaN,
        "DWB %" to Double.NaN,
        "NAME %" to Double.NaN,
    )

    @JvmStatic private val NUMBER_OF_CPUS = Runtime.getRuntime().availableProcessors()
    @JvmStatic private val EXECUTOR_SERVICE = Executors.newFixedThreadPool(NUMBER_OF_CPUS)

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

            return magnitude
        }

        val currentTime = UTC(TimeYMDHMS(2023, 9, 29, 12))
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
            val pmRA = getField("pmra").toDoubleOrNull()?.mas ?: Angle.ZERO
            val pmDEC = getField("pmdec").toDoubleOrNull()?.mas ?: Angle.ZERO
            val parallax = getField("plx_value").toDoubleOrNull()?.mas ?: Angle.ZERO
            val radialVelocity = getField("rvz_radvel").toDoubleOrNull()?.kms ?: Velocity.ZERO
            val redshift = getField("rvz_redshift").toDoubleOrNull() ?: 0.0
            val majorAxis = getField("galdim_majaxis").toDoubleOrNull()?.arcmin ?: Angle.ZERO
            val minorAxis = getField("galdim_minaxis").toDoubleOrNull()?.arcmin ?: Angle.ZERO
            val orientation = getField("galdim_angle").toDoubleOrNull()?.deg ?: Angle.ZERO
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

            val constellation = SkyObject.computeConstellation(rightAscensionJ2000, declinationJ2000, currentTime)

            data[id] = if (isDSO) DeepSkyObject(
                id, names.joinToString("|"), magnitude,
                rightAscensionJ2000, declinationJ2000,
                type, majorAxis, minorAxis, orientation,
                pmRA, pmDEC, parallax, radialVelocity, redshift,
                constellation,
            ) else Star(
                id, names.joinToString("|"), magnitude,
                rightAscensionJ2000, declinationJ2000,
                type, spType, pmRA, pmDEC,
                parallax, radialVelocity, redshift,
                constellation,
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

        // DSOS. ~28201 objects.

        if (fetchDSOs) {
            data.clear()
            skyObjectTypes.clear()

            val latch = CountUpDownLatch()

            for (i in 0 until maxOID step stepSize) {
                for (catalogType in DSO_CATALOG_TYPES_LIKE) {
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

                            val isStarDSO = catalogType !== DSO_CATALOG_TYPES_LIKE.last()

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
