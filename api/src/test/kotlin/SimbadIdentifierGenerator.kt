import de.siegmar.fastcsv.reader.CommentStrategy
import de.siegmar.fastcsv.reader.CsvReader
import nebulosa.adql.*
import nebulosa.api.atlas.SimbadIdentifierWriter
import nebulosa.io.resource
import nebulosa.log.loggerFor
import nebulosa.simbad.SimbadCatalogType
import nebulosa.simbad.SimbadService
import okhttp3.OkHttpClient
import okio.sink
import java.io.InputStreamReader
import java.nio.file.Path
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit

object SimbadIdentifierGenerator {

    @JvmStatic private val SIMBAD_IDS_PATH = Path.of("data", "simbad.ids")

    @JvmStatic private val LOG = loggerFor<SimbadIdentifierGenerator>()

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

    @JvmStatic private val CALDWELL = resource("caldwell.csv")!!
        .use { stream ->
            CSV_READER.build(InputStreamReader(stream, Charsets.UTF_8))
                .associate { it.getField(1).ifEmpty { it.getField(2) } to it.getField(0) }
        }

    @JvmStatic private val BENNETT = resource("bennett.csv")!!
        .use { stream ->
            CSV_READER.build(InputStreamReader(stream, Charsets.UTF_8))
                .associate { it.getField(1) to it.getField(0) }
        }

    @JvmStatic private val DUNLOP = resource("dunlop.csv")!!
        .use { stream ->
            CSV_READER.build(InputStreamReader(stream, Charsets.UTF_8))
                .associate { it.getField(1) to it.getField(0) }
        }

    @JvmStatic private val HERSHEL = resource("hershel.csv")!!
        .use { stream ->
            CSV_READER.build(InputStreamReader(stream, Charsets.UTF_8))
                .associate { it.getField(1) to it.getField(0) }
        }

    @JvmStatic private val IDS_TABLE = From("ids").alias("i")
    @JvmStatic private val BASIC_TABLE = From("basic").alias("b")
    @JvmStatic private val FLUX_TABLE = From("allfluxes").alias("f")
    @JvmStatic private val OIDREF = IDS_TABLE.column("oidref")
    @JvmStatic private val IDS = IDS_TABLE.column("ids")
    @JvmStatic private val RA = BASIC_TABLE.column("ra")
    @JvmStatic private val DEC = BASIC_TABLE.column("dec")
    @JvmStatic private val MAG_V = FLUX_TABLE.column("V")
    @JvmStatic private val MAG_B = FLUX_TABLE.column("B")

    private const val STEP_SIZE = 10000

    @JvmStatic
    fun main(args: Array<String>) {
        val names = LinkedHashSet<String>(8)

        fun extractNames(text: String): Set<String> {
            names.clear()

            val ids = text.split("|").toMutableList()

            for (entry in SimbadCatalogType.entries) {
                val namesIterator = ids.iterator()

                while (namesIterator.hasNext()) {
                    val name = entry.match(namesIterator.next()) ?: continue

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
                        if (name in HERSHEL) {
                            names.add("Hershel ${HERSHEL[name]}")
                        }
                    }
                }
            }

            return names
        }

        val builder = QueryBuilder()
        builder.add(Limit(STEP_SIZE))
        builder.add(OIDREF.alias("oidref"))
        builder.add(IDS)
        var join: Table = InnerJoin(IDS_TABLE, BASIC_TABLE, arrayOf(OIDREF equal BASIC_TABLE.column("oid")))
        join = LeftJoin(join, FLUX_TABLE, arrayOf(OIDREF equal FLUX_TABLE.column("oidref")))
        builder.add(join)
        builder.add(Ignored)
        builder.addAll(arrayOf(RA.isNotNull, DEC.isNotNull))
        builder.add((MAG_V.isNull and MAG_B.isNull) or (MAG_V lessOrEqual 14.0) or (MAG_B lessOrEqual 14.0))
        builder.add(SortBy(OIDREF))

        val maxOID = SIMBAD_SERVICE.query("SELECT max(b.oid) FROM basic b")
            .execute().body()!![0].getField("MAX").toInt()

        LOG.info("maxOID={}", maxOID)

        val writer = SimbadIdentifierWriter(SIMBAD_IDS_PATH.sink())
        val tasks = ArrayList<Future<Int>>(maxOID / STEP_SIZE + 1)

        for (i in 0 until maxOID step STEP_SIZE) {
            builder[4] = (OIDREF greaterOrEqual i) and (OIDREF lessThan i + STEP_SIZE)
            val task = EXECUTOR_SERVICE.submit(DownloadAndWriteWorker(builder.build(), writer, ::extractNames))
            tasks.add(task)
        }

        val count = tasks.sumOf { it.get() }

        writer.close()

        LOG.info("generated {} identifiers", count)

        EXECUTOR_SERVICE.shutdownNow()
    }

    private data class DownloadAndWriteWorker(
        private val query: Query,
        private val writer: SimbadIdentifierWriter,
        private val names: String.() -> Set<String>,
    ) : Callable<Int> {

        override fun call(): Int {
            val rows = SIMBAD_SERVICE.query(query).execute().body() ?: return 0

            if (rows.isEmpty()) {
                return 0
            }

            LOG.info("found {} rows", rows.size)

            var writeCount = 0

            synchronized(writer) {
                for (row in rows) {
                    val ids = row.getField("ids")
                    val id = row.getField("oidref").toLong()

                    for (name in ids.names()) {
                        writer.write(id, name)
                        writeCount++
                    }
                }
            }

            LOG.info("written {} identifiers", writeCount)

            return writeCount
        }
    }
}
