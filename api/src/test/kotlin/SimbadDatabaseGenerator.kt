import de.siegmar.fastcsv.reader.CommentStrategy
import de.siegmar.fastcsv.reader.CsvReader
import de.siegmar.fastcsv.reader.NamedCsvRecord
import nebulosa.adql.*
import nebulosa.api.atlas.SimbadDatabaseWriter
import nebulosa.api.atlas.SimbadEntity
import nebulosa.io.resource
import nebulosa.log.loggerFor
import nebulosa.nova.astrometry.Constellation
import nebulosa.simbad.SimbadCatalogType
import nebulosa.simbad.SimbadService
import nebulosa.skycatalog.SkyObject
import nebulosa.skycatalog.SkyObjectType
import nebulosa.skycatalog.stellarium.Nebula
import okhttp3.OkHttpClient
import okio.sink
import okio.source
import org.slf4j.LoggerFactory
import java.io.InputStreamReader
import java.nio.file.Path
import java.util.concurrent.*
import kotlin.io.path.createDirectories
import kotlin.io.path.deleteRecursively
import kotlin.math.min

object SimbadDatabaseGenerator {

    @JvmStatic private val SIMBAD_DATABASE_PATH = Path.of("data", "simbad")
    @JvmStatic private val LOG = loggerFor<SimbadDatabaseGenerator>()

    @JvmStatic private val HTTP_CLIENT = OkHttpClient.Builder()
        .connectTimeout(5L, TimeUnit.MINUTES)
        .writeTimeout(5L, TimeUnit.MINUTES)
        .readTimeout(5L, TimeUnit.MINUTES)
        .callTimeout(5L, TimeUnit.MINUTES)
        .build()

    @JvmStatic private val SIMBAD_SERVICE = SimbadService(httpClient = HTTP_CLIENT)
    @JvmStatic private val EXECUTOR_SERVICE = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())

    @JvmStatic private val CSV_READER = CsvReader.builder()
        .fieldSeparator(',')
        .quoteCharacter('"')
        .commentCharacter('#')
        .commentStrategy(CommentStrategy.SKIP)

    @JvmStatic private val MELOTTE = resource("MELOTTE.csv")!!
        .use { stream ->
            CSV_READER.ofCsvRecord(InputStreamReader(stream, Charsets.UTF_8))
                .associate { it.getField(1) to it.getField(0) }
        }

    @JvmStatic private val CALDWELL = resource("CALDWELL.csv")!!
        .use { stream ->
            CSV_READER.ofCsvRecord(InputStreamReader(stream, Charsets.UTF_8))
                .associate { it.getField(1).ifEmpty { it.getField(2) } to it.getField(0) }
        }

    @JvmStatic private val BENNETT = resource("BENNETT.csv")!!
        .use { stream ->
            CSV_READER.ofCsvRecord(InputStreamReader(stream, Charsets.UTF_8))
                .associate { it.getField(1) to it.getField(0) }
        }

    @JvmStatic private val DUNLOP = resource("DUNLOP.csv")!!
        .use { stream ->
            CSV_READER.ofCsvRecord(InputStreamReader(stream, Charsets.UTF_8))
                .associate { it.getField(1) to it.getField(0) }
        }

    @JvmStatic private val HERSHEL = resource("HERSHEL.csv")!!
        .use { stream ->
            CSV_READER.ofCsvRecord(InputStreamReader(stream, Charsets.UTF_8))
                .associate { it.getField(1) to it.getField(0) }
        }

    @JvmStatic private val BASIC_TABLE = From("basic").alias("b")
    @JvmStatic private val IDS_TABLE = From("ids").alias("i")
    @JvmStatic private val IDENT_TABLE = From("ident").alias("id")
    @JvmStatic private val FLUX_TABLE = From("allfluxes").alias("f")
    @JvmStatic private val IDS = IDS_TABLE.column("ids")
    @JvmStatic private val ID = IDENT_TABLE.column("id")
    @JvmStatic private val OID = BASIC_TABLE.column("oid")
    @JvmStatic private val MAIN_ID = BASIC_TABLE.column("main_id")
    @JvmStatic private val OTYPE = BASIC_TABLE.column("otype")
    @JvmStatic private val RA = BASIC_TABLE.column("ra")
    @JvmStatic private val DEC = BASIC_TABLE.column("dec")
    @JvmStatic private val PM_RA = BASIC_TABLE.column("pmra")
    @JvmStatic private val PM_DEC = BASIC_TABLE.column("pmdec")
    @JvmStatic private val PLX = BASIC_TABLE.column("plx_value")
    @JvmStatic private val RAD_VEL = BASIC_TABLE.column("rvz_radvel")
    @JvmStatic private val REDSHIFT = BASIC_TABLE.column("rvz_redshift")
    @JvmStatic private val MAG_V = FLUX_TABLE.column("V")
    @JvmStatic private val MAG_B = FLUX_TABLE.column("B")
    @JvmStatic private val MAG_U = FLUX_TABLE.column("U")
    @JvmStatic private val MAG_R = FLUX_TABLE.column("R")
    @JvmStatic private val MAG_I = FLUX_TABLE.column("I")
    @JvmStatic private val MAG_J = FLUX_TABLE.column("J")
    @JvmStatic private val MAG_H = FLUX_TABLE.column("H")
    @JvmStatic private val MAG_K = FLUX_TABLE.column("K")

    @JvmStatic private val STELLARIUM_NAMES = Path.of("data", "names.dat").source().use(Nebula::namesFor).toMutableList()
    @JvmStatic private val ENTITY_IDS = ConcurrentHashMap.newKeySet<Long>(64000)

    @JvmStatic
    fun SimbadEntity.generateNames(): Boolean {
        val ids = name.split("|").toMutableList()
        val names = LinkedHashSet<String>(ids.size)
        val moreNames = LinkedHashSet<String>(6)

        for (entry in SimbadCatalogType.entries) {
            val idIterator = ids.iterator()

            while (idIterator.hasNext()) {
                val id = entry.match(idIterator.next()) ?: continue
                idIterator.remove()
                names.add(id)
            }
        }

        synchronized(STELLARIUM_NAMES) {
            val stellariumNamesIterator = STELLARIUM_NAMES.iterator()

            while (stellariumNamesIterator.hasNext()) {
                val name = stellariumNamesIterator.next()

                if (names.any { it.equals(name.id, true) }) {
                    stellariumNamesIterator.remove()
                    moreNames.add(name.name)
                }
            }
        }

        for (name in names) {
            if (name in MELOTTE) {
                moreNames.add("Mel ${MELOTTE[name]}")
            }
            if (name in CALDWELL) {
                moreNames.add("C ${CALDWELL[name]}")
            }
            if (name in BENNETT) {
                moreNames.add("Bennett ${BENNETT[name]}")
            }
            if (name in DUNLOP) {
                moreNames.add("Dunlop ${DUNLOP[name]}")
            }
            if (name in HERSHEL) {
                moreNames.add("Hershel ${HERSHEL[name]}")
            }
        }

        name = buildString {
            var i = 0

            names.forEach {
                if (i > 0) append(SkyObject.NAME_SEPARATOR)
                append(it)
                i++
            }

            moreNames.forEach { n ->
                if (names.none { it.equals(n, true) }) {
                    if (i > 0) append(SkyObject.NAME_SEPARATOR)
                    append(n)
                    i++
                }
            }
        }

        return name.isNotEmpty()
    }

    @JvmStatic
    fun main(args: Array<String>) {
        SIMBAD_DATABASE_PATH.deleteRecursively()
        SIMBAD_DATABASE_PATH.createDirectories()

        val tasks = ArrayList<Future<List<SimbadEntity>>>()

        tasks.add(EXECUTOR_SERVICE.submit(NgcDownloadTask))
        tasks.add(EXECUTOR_SERVICE.submit(IcDownloadTask))
        tasks.add(EXECUTOR_SERVICE.submit(HdDownloadTask))
        tasks.add(EXECUTOR_SERVICE.submit(HrDownloadTask))
        tasks.add(EXECUTOR_SERVICE.submit(HipDownloadTask))
        tasks.add(EXECUTOR_SERVICE.submit(SaoDownloadTask))
        tasks.add(EXECUTOR_SERVICE.submit(GumDownloadTask))
        tasks.add(EXECUTOR_SERVICE.submit(BarnardDownloadTask))
        tasks.add(EXECUTOR_SERVICE.submit(LbnDownloadTask))
        tasks.add(EXECUTOR_SERVICE.submit(LdnDownloadTask))
        tasks.add(EXECUTOR_SERVICE.submit(RcwDownloadTask))
        tasks.add(EXECUTOR_SERVICE.submit(Sh2DownloadTask))
        tasks.add(EXECUTOR_SERVICE.submit(CedDownloadTask))
        tasks.add(EXECUTOR_SERVICE.submit(UgcDownloadTask))
        tasks.add(EXECUTOR_SERVICE.submit(ApgDownloadTask))
        tasks.add(EXECUTOR_SERVICE.submit(HcgDownloadTask))
        tasks.add(EXECUTOR_SERVICE.submit(VVDownloadTask))
        tasks.add(EXECUTOR_SERVICE.submit(VdBHDownloadTask))
        tasks.add(EXECUTOR_SERVICE.submit(DwbDownloadTask))
        tasks.add(EXECUTOR_SERVICE.submit(AcoDownloadTask))
        tasks.add(EXECUTOR_SERVICE.submit(ClDownloadTask))
        tasks.add(EXECUTOR_SERVICE.submit(NamedDownloadTask))

        var count = 0
        var index = 0
        var writer: SimbadDatabaseWriter? = null
        val entities = HashMap<Long, SimbadEntity>()

        for (task in tasks) {
            with(task.get()) {
                forEach { entities[it.id] = it }
            }
        }

        for ((_, entity) in entities) {
            if (writer == null || count > 10000) {
                writer?.close()
                writer = SimbadDatabaseWriter(Path.of("$SIMBAD_DATABASE_PATH", "simbad.%02d.dat".format(index++)).sink())
                count = 0
            }

            writer.write(entity)
            LOG.info("Entity: {}", entity)
            count++
        }

        writer?.close()

        LOG.info("Recorded {} entities", entities.size)
        LOG.info("Remaining names. count={}, stellarium={}", STELLARIUM_NAMES.size, STELLARIUM_NAMES)

        EXECUTOR_SERVICE.shutdownNow()
    }

    @JvmStatic
    private fun List<NamedCsvRecord>.parse(entities: MutableList<SimbadEntity>): List<SimbadEntity> {
        var writeCount = 0

        for (row in this) {
            val name = row.getField("ids")
            val id = row.getField("oid").toLong()
            val type = SkyObjectType.parse(row.getField("otype")) ?: continue
            // Save using the original units to optmize file size.
            val rightAscensionJ2000 = row.getField("ra").toDouble()  // deg
            val declinationJ2000 = row.getField("dec").toDouble()  // deg
            val pmRA = row.getField("pmra").toDoubleOrNull() ?: 0.0 // mas
            val pmDEC = row.getField("pmdec").toDoubleOrNull() ?: 0.0 // mas
            val parallax = row.getField("plx_value").toDoubleOrNull() ?: 0.0 // mas
            val radialVelocity = row.getField("rvz_radvel").toDoubleOrNull() ?: 0.0 // kms
            val redshift = row.getField("rvz_redshift").toDoubleOrNull() ?: 0.0

            var magnitude = row.getField("V").toDoubleOrNull()
                ?: row.getField("B").toDoubleOrNull()
                ?: row.getField("U").toDoubleOrNull()
                ?: SkyObject.UNKNOWN_MAGNITUDE

            if (magnitude >= SkyObject.UNKNOWN_MAGNITUDE || !magnitude.isFinite()) {
                magnitude = min(magnitude, row.getField("R").toDoubleOrNull() ?: SkyObject.UNKNOWN_MAGNITUDE)
                magnitude = min(magnitude, row.getField("I").toDoubleOrNull() ?: SkyObject.UNKNOWN_MAGNITUDE)
                magnitude = min(magnitude, row.getField("J").toDoubleOrNull() ?: SkyObject.UNKNOWN_MAGNITUDE)
                magnitude = min(magnitude, row.getField("H").toDoubleOrNull() ?: SkyObject.UNKNOWN_MAGNITUDE)
                magnitude = min(magnitude, row.getField("K").toDoubleOrNull() ?: SkyObject.UNKNOWN_MAGNITUDE)
            }

            val entity = SimbadEntity(
                id, name, type,
                rightAscensionJ2000, declinationJ2000,
                magnitude, pmRA, pmDEC,
                parallax, radialVelocity, redshift,
                Constellation.AND // Don't save it.
            )

            if (entity.generateNames()) {
                entities.add(entity)
                writeCount++
                ENTITY_IDS.add(entity.id)
            }
        }

        return entities
    }

    private sealed class DownloadTask(
        private val name: String,
        private val magnitudeMax: Double = Double.NaN,
    ) : Callable<List<SimbadEntity>> {

        protected val log by lazy { LoggerFactory.getLogger(javaClass)!! }

        private val mainBuilder = QueryBuilder().apply {
            add(Ignored)
            add(Limit(50000))
            add(Distinct)

            var join: Table = LeftJoin(BASIC_TABLE, IDS_TABLE, arrayOf(OID equal IDS_TABLE.column("oidref")))
            join = LeftJoin(join, FLUX_TABLE, arrayOf(OID equal FLUX_TABLE.column("oidref")))
            add(join)

            addAll(arrayOf(RA.isNotNull, DEC.isNotNull))
            if (magnitudeMax.isFinite()) add((MAG_V lessOrEqual magnitudeMax) or (MAG_B lessOrEqual magnitudeMax))
            addAll(arrayOf(OID, MAIN_ID, OTYPE, RA, DEC, PM_RA, PM_DEC, PLX, RAD_VEL, REDSHIFT))
            addAll(arrayOf(MAG_V, MAG_B, MAG_U, MAG_R, MAG_I, MAG_J, MAG_H, MAG_K, IDS))
            add(SortBy(OID))
        }

        private val idBuilder = QueryBuilder().apply {
            add(Ignored)
            add(Limit(50000))
            add(Distinct)
            add(IDENT_TABLE)
            add(ID like "$name%")
            add(IDENT_TABLE.column("oidref"))
            add(SortBy(IDENT_TABLE.column("oidref")))
        }

        override fun call(): List<SimbadEntity> {
            val entities = ArrayList<SimbadEntity>()
            var lastID = 0L

            log.info("Task started")

            while (true) {
                idBuilder[0] = IDENT_TABLE.column("oidref") greaterThan lastID
                var query = idBuilder.build()

                var ids: LongArray
                var attempt = 0

                while (true) {
                    try {
                        val rows = SIMBAD_SERVICE.query(query).execute().body().takeIf { !it.isNullOrEmpty() } ?: return entities
                        ids = LongArray(rows.size) { rows[it].getField("oidref").toLong() }
                        ids = ids.filter { it !in ENTITY_IDS }.toLongArray()
                        break
                    } catch (e: Throwable) {
                        log.error("Failed to retrieve IDs. attempt=${attempt++}, query=$query", e)
                        Thread.sleep(attempt * 1000L)
                        continue
                    }
                }

                if (ids.isEmpty()) {
                    log.info("no IDs")
                    break
                }

                lastID = ids.last()

                log.info("Found {} IDs", ids.size)

                for (i in ids.indices step 1000) {
                    mainBuilder[0] = OID includes ids.sliceArray(i until min(i + 1000, ids.size))
                    query = mainBuilder.build()

                    attempt = 0

                    while (true) {
                        try {
                            val rows = SIMBAD_SERVICE.query(query).execute().body().takeIf { !it.isNullOrEmpty() } ?: return entities
                            log.info("Found {} rows", rows.size)
                            rows.parse(entities)
                            break
                        } catch (e: Throwable) {
                            log.error("Failed to download. attempt=${attempt++}, query=$query", e)
                            Thread.sleep(attempt * 1000L)
                            continue
                        }
                    }
                }
            }

            log.info("Task finished. count={}", entities.size)

            return entities
        }
    }

    private data object NgcDownloadTask : DownloadTask("NGC ")
    private data object IcDownloadTask : DownloadTask("IC ")
    private data object HdDownloadTask : DownloadTask("HD ", 6.0)
    private data object HrDownloadTask : DownloadTask("HR ", 6.0)
    private data object HipDownloadTask : DownloadTask("HIP ", 6.0)
    private data object SaoDownloadTask : DownloadTask("SAO ", 6.0)
    private data object GumDownloadTask : DownloadTask("GUM ")
    private data object BarnardDownloadTask : DownloadTask("Barnard ")
    private data object LbnDownloadTask : DownloadTask("LBN ")
    private data object LdnDownloadTask : DownloadTask("LDN ")
    private data object RcwDownloadTask : DownloadTask("RCW ")
    private data object Sh2DownloadTask : DownloadTask("SH  2-")
    private data object CedDownloadTask : DownloadTask("Ced ")
    private data object UgcDownloadTask : DownloadTask("UGC ")
    private data object ApgDownloadTask : DownloadTask("APG ")
    private data object HcgDownloadTask : DownloadTask("HCG ")
    private data object VVDownloadTask : DownloadTask("VV ")
    private data object VdBHDownloadTask : DownloadTask("VdBH ")
    private data object DwbDownloadTask : DownloadTask("DWB ")
    private data object AcoDownloadTask : DownloadTask("ACO ")
    private data object ClDownloadTask : DownloadTask("Cl ")
    private data object NamedDownloadTask : DownloadTask("NAME ")
}
