import nebulosa.io.transferAndClose
import nebulosa.math.Angle.Companion.arcmin
import nebulosa.math.Angle.Companion.mas
import nebulosa.math.Velocity.Companion.kms
import nebulosa.nova.astrometry.Constellation
import nebulosa.simbad.CatalogType
import nebulosa.simbad.SimbadObject
import nebulosa.simbad.SimbadQuery
import nebulosa.simbad.SimbadService
import nebulosa.skycatalog.SkyObject
import nebulosa.skycatalog.SkyObjectType
import nebulosa.skycatalog.hyg.HygDatabase
import nebulosa.skycatalog.stellarium.Nebula
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.Request
import okio.Source
import okio.buffer
import okio.gzip
import okio.source
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import org.sqlite.JDBC
import java.io.File
import java.nio.file.Path
import java.nio.file.Paths
import java.util.concurrent.TimeUnit
import kotlin.io.path.deleteIfExists
import kotlin.io.path.inputStream
import kotlin.io.path.outputStream
import kotlin.math.min

object SkyDatabaseGenerator {

    object StarEntity : Table("stars") {
        val id = integer("id")
        val hr = integer("hr")
        val hd = integer("hd")
        val hip = integer("hip")
        val magnitude = double("magnitude")
        val rightAscension = double("rightAscension")
        val declination = double("declination")
        val spType = text("spType").nullable()
        val redshift = double("redshift")
        val parallax = double("parallax")
        val radialVelocity = double("radialVelocity")
        val distance = double("distance")
        val pmRA = double("pmRA")
        val pmDEC = double("pmDEC")
        val type = enumeration<SkyObjectType>("type")
        val constellation = enumeration<Constellation>("constellation")
        val names = text("names")

        override val primaryKey = PrimaryKey(id, name = "pk_stars_id")
    }

    object DsoEntity : Table("dsos") {
        val id = integer("id")
        val m = integer("m")
        val ngc = integer("ngc")
        val ic = integer("ic")
        val c = integer("c")
        val b = integer("b")
        val sh2 = integer("sh2")
        val vdb = integer("vdb")
        val rcw = integer("rcw")
        val ldn = integer("ldn")
        val lbn = integer("lbn")
        val cr = integer("cr")
        val mel = integer("mel")
        val pgc = integer("pgc")
        val ugc = integer("ugc")
        val arp = integer("arp")
        val vv = integer("vv")
        val dwb = integer("dwb")
        val tr = integer("tr")
        val st = integer("st")
        val ru = integer("ru")
        val vdbha = integer("vdbha")
        val ced = text("ced").nullable()
        val pk = text("pk").nullable()
        val png = text("png").nullable()
        val snrg = text("snrg").nullable()
        val aco = text("aco").nullable()
        val hcg = text("hcg").nullable()
        val eso = text("eso").nullable()
        val vdbh = text("vdbh").nullable()
        val magnitude = double("magnitude")
        val rightAscension = double("rightAscension")
        val declination = double("declination")
        val type = enumeration<SkyObjectType>("type")
        val mType = text("mType").nullable()
        val majorAxis = double("majorAxis")
        val minorAxis = double("minorAxis")
        val orientation = double("orientation")
        val redshift = double("redshift")
        val parallax = double("parallax")
        val radialVelocity = double("radialVelocity")
        val distance = double("distance")
        val pmRA = double("pmRA")
        val pmDEC = double("pmDEC")
        val constellation = enumeration<Constellation>("constellation")
        val names = text("names")

        override val primaryKey = PrimaryKey(id, name = "pk_dsos_id")
    }

    @JvmStatic private val DATABASE_PATH = Paths.get("desktop/src/main/resources/data/SkyDatabase.db")

    @JvmStatic
    fun main(args: Array<String>) {
        DATABASE_PATH.deleteIfExists()

        val okHttpClient = OkHttpClient.Builder()
            .cache(Cache(File(".cache"), 1024 * 1024 * 128))
            .connectTimeout(1L, TimeUnit.MINUTES)
            .writeTimeout(1L, TimeUnit.MINUTES)
            .readTimeout(1L, TimeUnit.MINUTES)
            .callTimeout(1L, TimeUnit.MINUTES)
            .build()

        val simbadService = SimbadService(okHttpClient = okHttpClient)

        val messierCatalog = simbadService.simbadCatalog(CatalogType.M)
        val ngcCatalog = simbadService.simbadCatalog(CatalogType.NGC)
        val icCatalog = simbadService.simbadCatalog(CatalogType.IC)
        val ledaCatalog = simbadService.simbadCatalog(CatalogType.LEDA)
        val hipCatalog = simbadService.simbadCatalog(CatalogType.HIP)
        val hdCatalog = simbadService.simbadCatalog(CatalogType.HD)

        Database.connect("jdbc:sqlite:$DATABASE_PATH", driver = JDBC::class.java.name)

        transaction {
            SchemaUtils.create(DsoEntity, StarEntity)

            val catalogSource = okHttpClient.download(
                "https://github.com/Stellarium/stellarium/raw/master/nebulae/default/catalog.dat",
                Paths.get(".cache/catalog.dat"),
            ).gzip()

            val namesSource = okHttpClient.download(
                "https://github.com/Stellarium/stellarium/raw/master/nebulae/default/names.dat",
                Paths.get(".cache/names.dat"),
            )

            with(Nebula()) {
                load(catalogSource, namesSource)

                for (item in this) {
                    val simbadObject = messierCatalog["${item.m}"] ?: ngcCatalog["${item.ngc}"]
                    ?: icCatalog["${item.ic}"] ?: ledaCatalog["${item.pgc}"]

                    val plx = simbadObject?.plx?.mas ?: item.parallax

                    DsoEntity.insert {
                        it[id] = item.id
                        it[names] = item.names
                        it[m] = item.m
                        it[ngc] = item.ngc
                        it[ic] = item.ic
                        it[c] = item.c
                        it[b] = item.b
                        it[sh2] = item.sh2
                        it[vdb] = item.vdb
                        it[rcw] = item.rcw
                        it[ldn] = item.ldn
                        it[lbn] = item.lbn
                        it[cr] = item.cr
                        it[mel] = item.mel
                        it[pgc] = item.pgc
                        it[ugc] = item.ugc
                        it[arp] = item.arp
                        it[vv] = item.vv
                        it[dwb] = item.dwb
                        it[tr] = item.tr
                        it[st] = item.st
                        it[ru] = item.ru
                        it[vdbha] = item.vdbha
                        it[ced] = item.ced?.ifEmpty { null }
                        it[pk] = item.pk?.ifEmpty { null }
                        it[png] = item.png?.ifEmpty { null }
                        it[snrg] = item.snrg?.ifEmpty { null }
                        it[aco] = item.aco?.ifEmpty { null }
                        it[hcg] = item.hcg?.ifEmpty { null }
                        it[eso] = item.eso?.ifEmpty { null }
                        it[vdbh] = item.vdbh?.ifEmpty { null }
                        it[magnitude] = min(item.magnitude, simbadObject?.magnitude ?: SkyObject.UNKNOWN_MAGNITUDE)
                        it[rightAscension] = item.rightAscension.value
                        it[declination] = item.declination.value
                        it[type] = simbadObject?.type ?: item.type
                        it[mType] = item.mType?.ifEmpty { null }
                        it[majorAxis] = simbadObject?.majorAxis?.arcmin?.value ?: item.majorAxis.value
                        it[minorAxis] = simbadObject?.minorAxis?.arcmin?.value ?: item.minorAxis.value
                        it[orientation] = item.orientation.value
                        it[redshift] = simbadObject?.redshift ?: item.redshift
                        it[parallax] = simbadObject?.plx?.mas?.value ?: item.parallax.value
                        it[radialVelocity] = simbadObject?.rv?.kms?.value ?: item.radialVelocity.value
                        it[distance] = if (item.distance == 0.0 && plx.value != 0.0) 3.2615637769 / plx.arcsec else item.distance
                        it[pmRA] = simbadObject?.pmRA?.mas?.value ?: item.pmRA.value
                        it[pmDEC] = simbadObject?.pmDEC?.mas?.value ?: item.pmDEC.value
                        it[constellation] = item.constellation
                    }
                }
            }

            val hyg = okHttpClient.download(
                "https://github.com/astronexus/HYG-Database/raw/master/hyg/v3/hyg.csv",
                Paths.get(".cache/hyg.csv")
            )

            with(HygDatabase()) {
                load(hyg.buffer().inputStream())

                for (item in this) {
                    val simbadObject = hipCatalog["${item.hip}"] ?: hdCatalog["${item.hd}"]
                    val plx = simbadObject?.plx?.mas ?: item.parallax

                    StarEntity.insert {
                        it[id] = item.id
                        it[names] = item.names
                        it[hr] = item.hr
                        it[hd] = item.hd
                        it[hip] = item.hip
                        it[magnitude] = min(item.magnitude, simbadObject?.magnitude ?: SkyObject.UNKNOWN_MAGNITUDE)
                        it[rightAscension] = item.rightAscension.value
                        it[declination] = item.declination.value
                        it[spType] = (simbadObject?.spType ?: item.spType)?.ifEmpty { null }
                        it[redshift] = simbadObject?.redshift ?: item.redshift
                        it[parallax] = simbadObject?.plx?.mas?.value ?: item.parallax.value
                        it[radialVelocity] = simbadObject?.rv?.kms?.value ?: item.radialVelocity.value
                        it[distance] = if (item.distance == 0.0 && plx.value != 0.0) 3.2615637769 / plx.arcsec else item.distance
                        it[pmRA] = simbadObject?.pmRA?.mas?.value ?: item.pmRA.value
                        it[pmDEC] = simbadObject?.pmDEC?.mas?.value ?: item.pmDEC.value
                        it[type] = simbadObject?.type ?: item.type
                        it[constellation] = item.constellation
                    }
                }
            }
        }

        Runtime.getRuntime().exec("sqlite3 $DATABASE_PATH 'VACUUM'").waitFor()
        Runtime.getRuntime().exec("gzip -f -6 $DATABASE_PATH").waitFor()
    }

    @JvmStatic
    private fun OkHttpClient.download(url: String, output: Path): Source {
        return with(Request.Builder().url(url).build()) {
            val response = newCall(this).execute()
            response.use { it.body.byteStream().transferAndClose(output.outputStream()) }
            output.inputStream().source()
        }
    }

    @JvmStatic
    private fun SimbadService.simbadCatalog(type: CatalogType): Map<String, SimbadObject> {
        val res = LinkedHashMap<String, SimbadObject>()
        val query = SimbadQuery()

        query.catalog(type)
        query.id(0)
        query.limit(20000)
        query.magnitude(max = 20.0)

        while (true) {
            val data = query(query).execute().body() ?: break
            if (data.isEmpty()) break

            for (item in data) {
                val key = item.names.firstOrNull { it.type == type }?.name ?: continue
                res[key] = item
            }

            query.id(data.last().id.toInt() + 1)
        }

        return res
    }
}
