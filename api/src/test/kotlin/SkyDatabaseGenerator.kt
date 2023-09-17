import com.fasterxml.jackson.databind.ObjectMapper
import nebulosa.api.atlas.DeepSkyObjectEntity
import nebulosa.api.atlas.StarEntity
import nebulosa.io.transferAndClose
import nebulosa.log.loggerFor
import nebulosa.math.Angle.Companion.arcmin
import nebulosa.math.Angle.Companion.mas
import nebulosa.math.Velocity.Companion.kms
import nebulosa.simbad.CatalogType
import nebulosa.simbad.SimbadObject
import nebulosa.simbad.SimbadQuery
import nebulosa.simbad.SimbadService
import nebulosa.skycatalog.SkyObject
import nebulosa.skycatalog.hyg.HygDatabase
import nebulosa.skycatalog.stellarium.Nebula
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.Request
import okio.Source
import okio.buffer
import okio.gzip
import okio.source
import java.io.File
import java.nio.file.Path
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.zip.GZIPOutputStream
import kotlin.io.path.inputStream
import kotlin.io.path.outputStream
import kotlin.math.min

object SkyDatabaseGenerator {

    @JvmStatic private val STAR_DATABASE_PATH = Path.of("api/data/stars.json.gz")
    @JvmStatic private val DSO_DATABASE_PATH = Path.of("api/data/dsos.json.gz")

    @JvmStatic
    fun main(args: Array<String>) {
        val okHttpClient = OkHttpClient.Builder()
            .cache(Cache(File(".cache"), 1024 * 1024 * 128))
            .connectTimeout(1L, TimeUnit.MINUTES)
            .writeTimeout(1L, TimeUnit.MINUTES)
            .readTimeout(1L, TimeUnit.MINUTES)
            .callTimeout(1L, TimeUnit.MINUTES)
            .build()

        val simbadService = SimbadService(okHttpClient = okHttpClient)

        val messierCatalog = simbadService.simbadCatalog(CatalogType.M, SkyObject.UNKNOWN_MAGNITUDE)
        val ngcCatalog = simbadService.simbadCatalog(CatalogType.NGC, SkyObject.UNKNOWN_MAGNITUDE)
        val icCatalog = simbadService.simbadCatalog(CatalogType.IC, SkyObject.UNKNOWN_MAGNITUDE)
        val ledaCatalog = simbadService.simbadCatalog(CatalogType.LEDA)
        val hipCatalog = simbadService.simbadCatalog(CatalogType.HIP)
        val hdCatalog = simbadService.simbadCatalog(CatalogType.HD)

        EXECUTORS.shutdown()
        EXECUTORS.awaitTermination(1, TimeUnit.HOURS)

        LOG.info("Messier. size={}", messierCatalog.size)
        LOG.info("NGC. size={}", ngcCatalog.size)
        LOG.info("IC. size={}", icCatalog.size)
        LOG.info("LEDA. size={}", ledaCatalog.size)
        LOG.info("HIP. size={}", hipCatalog.size)
        LOG.info("HD. size={}", hdCatalog.size)

        LOG.info("Downloading Nebula catalog")

        val catalogSource = okHttpClient.download(
            "https://github.com/Stellarium/stellarium/raw/master/nebulae/default/catalog.dat",
            Path.of(".cache/catalog.dat"),
        ).gzip()

        val namesSource = okHttpClient.download(
            "https://github.com/Stellarium/stellarium/raw/master/nebulae/default/names.dat",
            Path.of(".cache/names.dat"),
        )

        LOG.info("Loading Nebula catalog")

        val mapper = ObjectMapper()

        with(Nebula()) {
            load(catalogSource, namesSource)

            val dsos = ArrayList<DeepSkyObjectEntity>(size)

            for (item in this) {
                val simbadObject = messierCatalog["${item.m}"] ?: ngcCatalog["${item.ngc}"]
                ?: icCatalog["${item.ic}"] ?: ledaCatalog["${item.pgc}"]

                val plx = simbadObject?.plx?.mas ?: item.parallax

                val dso = DeepSkyObjectEntity(
                    id = item.id.toLong(),
                    names = item.names,
                    m = item.m,
                    ngc = item.ngc,
                    ic = item.ic,
                    c = item.c,
                    b = item.b,
                    sh2 = item.sh2,
                    vdb = item.vdb,
                    rcw = item.rcw,
                    ldn = item.ldn,
                    lbn = item.lbn,
                    cr = item.cr,
                    mel = item.mel,
                    pgc = item.pgc,
                    ugc = item.ugc,
                    arp = item.arp,
                    vv = item.vv,
                    dwb = item.dwb,
                    tr = item.tr,
                    st = item.st,
                    ru = item.ru,
                    vdbha = item.vdbha,
                    ced = item.ced?.ifEmpty { null },
                    pk = item.pk?.ifEmpty { null },
                    png = item.png?.ifEmpty { null },
                    snrg = item.snrg?.ifEmpty { null },
                    aco = item.aco?.ifEmpty { null },
                    hcg = item.hcg?.ifEmpty { null },
                    eso = item.eso?.ifEmpty { null },
                    vdbh = item.vdbh?.ifEmpty { null },
                    magnitude = min(item.magnitude, simbadObject?.magnitude ?: SkyObject.UNKNOWN_MAGNITUDE),
                    rightAscension = item.rightAscension.value,
                    declination = item.declination.value,
                    type = simbadObject?.type ?: item.type,
                    mType = item.mType?.ifEmpty { null },
                    majorAxis = simbadObject?.majorAxis?.arcmin?.value ?: item.majorAxis.value,
                    minorAxis = simbadObject?.minorAxis?.arcmin?.value ?: item.minorAxis.value,
                    orientation = item.orientation.value,
                    redshift = simbadObject?.redshift ?: item.redshift,
                    parallax = simbadObject?.plx?.mas?.value ?: item.parallax.value,
                    radialVelocity = simbadObject?.rv?.kms?.value ?: item.radialVelocity.value,
                    distance = if (item.distance == 0.0 && plx.value != 0.0) 3.2615637769 / plx.arcsec else item.distance,
                    pmRA = simbadObject?.pmRA?.mas?.value ?: item.pmRA.value,
                    pmDEC = simbadObject?.pmDEC?.mas?.value ?: item.pmDEC.value,
                    constellation = item.constellation,
                )

                dsos.add(dso)
            }

            GZIPOutputStream(DSO_DATABASE_PATH.outputStream())
                .use { mapper.writeValue(it, dsos) }
        }

        LOG.info("Downloading Hyg catalog")

        // Check the latest version here: https://github.com/astronexus/HYG-Database/tree/master/hyg/v3

        val hyg = okHttpClient.download(
            "https://github.com/astronexus/HYG-Database/raw/master/hyg/v3/hyg_v35.csv",
            Path.of(".cache/hyg_v35.csv")
        )

        LOG.info("Loading Hyg catalog")

        with(HygDatabase()) {
            load(hyg.buffer().inputStream())

            val stars = ArrayList<StarEntity>(size)

            for (item in this) {
                val simbadObject = hipCatalog["${item.hip}"] ?: hdCatalog["${item.hd}"]
                val plx = simbadObject?.plx?.mas ?: item.parallax

                val star = StarEntity(
                    id = item.id.toLong(),
                    names = item.names,
                    hr = item.hr,
                    hd = item.hd,
                    hip = item.hip,
                    magnitude = min(item.magnitude, simbadObject?.magnitude ?: SkyObject.UNKNOWN_MAGNITUDE),
                    rightAscensionJ2000 = item.rightAscension.value,
                    declinationJ2000 = item.declination.value,
                    spType = (simbadObject?.spType ?: item.spType)?.ifEmpty { null },
                    redshift = simbadObject?.redshift ?: item.redshift,
                    parallax = simbadObject?.plx?.mas?.value ?: item.parallax.value,
                    radialVelocity = simbadObject?.rv?.kms?.value ?: item.radialVelocity.value,
                    distance = if (item.distance == 0.0 && plx.value != 0.0) 3.2615637769 / plx.arcsec else item.distance,
                    pmRA = simbadObject?.pmRA?.mas?.value ?: item.pmRA.value,
                    pmDEC = simbadObject?.pmDEC?.mas?.value ?: item.pmDEC.value,
                    type = (simbadObject?.type ?: item.type),
                    constellation = item.constellation,
                )

                stars.add(star)
            }

            GZIPOutputStream(STAR_DATABASE_PATH.outputStream())
                .use { mapper.writeValue(it, stars) }
        }
    }

    @JvmStatic
    private fun OkHttpClient.download(url: String, output: Path): Source {
        return with(Request.Builder().url(url).build()) {
            val response = newCall(this).execute()
            response.use { it.body.byteStream().transferAndClose(output.outputStream()) }
            output.inputStream().source()
        }
    }

    @JvmStatic private val LOG = loggerFor<SkyDatabaseGenerator>()
    @JvmStatic private val EXECUTORS = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())

    @JvmStatic
    private fun SimbadService.simbadCatalog(type: CatalogType, magnitude: Double = 20.0): Map<String, SimbadObject> {
        val output = LinkedHashMap<String, SimbadObject>()
        EXECUTORS.submit(SimbadQueryWorker(this, output, type, magnitude))
        return output
    }

    private class SimbadQueryWorker(
        private val service: SimbadService,
        private val output: MutableMap<String, SimbadObject>,
        private val type: CatalogType,
        private val magnitude: Double = SkyObject.UNKNOWN_MAGNITUDE,
    ) : Runnable {

        override fun run() {
            val query = SimbadQuery()
            query.catalog(type)
            query.id(0)
            query.limit(20000)

            if (magnitude != SkyObject.UNKNOWN_MAGNITUDE) query.magnitude(max = magnitude)

            while (true) {
                val data = service.query(query).execute().body() ?: break

                if (data.isEmpty()) break

                synchronized(output) {
                    for (item in data) {
                        val key = item.names.firstOrNull { it.type == type }?.name ?: continue
                        output[key] = item
                    }
                }

                query.id(data.last().id.toInt() + 1)
            }
        }
    }
}
