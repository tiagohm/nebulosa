import nebulosa.desktop.model.DsoEntity
import nebulosa.desktop.model.NameEntity
import nebulosa.desktop.model.StarEntity
import nebulosa.io.transferAndClose
import nebulosa.math.Angle.Companion.mas
import nebulosa.math.Velocity.Companion.kms
import nebulosa.simbad.CatalogType
import nebulosa.simbad.SimbadObject
import nebulosa.simbad.SimbadQuery
import nebulosa.simbad.SimbadService
import nebulosa.skycatalog.hyg.HygDatabase
import nebulosa.skycatalog.stellarium.Nebula
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.Request
import okio.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import org.sqlite.JDBC
import java.io.File
import java.nio.file.Path
import java.nio.file.Paths
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import kotlin.io.path.deleteIfExists
import kotlin.io.path.inputStream
import kotlin.io.path.outputStream
import kotlin.io.use

object StarCatalogGenerator {

    @JvmStatic
    fun main(args: Array<String>) {
        Paths.get("desktop/src/main/resources/data/StarCatalog.db").deleteIfExists()
        Paths.get("desktop/src/main/resources/data/StarCatalog.db.gz").deleteIfExists()

        val okHttpClient = OkHttpClient.Builder()
            .cache(Cache(File(".cache"), 1024 * 1024 * 128))
            .connectTimeout(1L, TimeUnit.MINUTES)
            .writeTimeout(1L, TimeUnit.MINUTES)
            .readTimeout(1L, TimeUnit.MINUTES)
            .callTimeout(1L, TimeUnit.MINUTES)
            .build()

        val simbadService = SimbadService(okHttpClient = okHttpClient)

        Database
            .connect(
                "jdbc:sqlite:desktop/src/main/resources/data/StarCatalog.db",
                driver = JDBC::class.java.name
            )

        transaction {
            SchemaUtils.create(DsoEntity, StarEntity, NameEntity)

            val catalog = okHttpClient.download(
                "https://github.com/Stellarium/stellarium/raw/master/nebulae/default/catalog.dat",
                Paths.get(".cache/catalog.dat"),
            ).gzip()

            val names = okHttpClient.download(
                "https://github.com/Stellarium/stellarium/raw/master/nebulae/default/names.dat",
                Paths.get(".cache/names.dat"),
            )

            val namesId = AtomicInteger(1)

            with(Nebula()) {
                load(catalog, names)

                for (item in this) {
                    DsoEntity.insert {
                        it[id] = item.id
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
                        it[magnitude] = item.magnitude
                        it[rightAscension] = item.rightAscension.value
                        it[declination] = item.declination.value
                        it[type] = item.type
                        it[mType] = item.mType?.ifEmpty { null }
                        it[majorAxis] = item.majorAxis.value
                        it[minorAxis] = item.minorAxis.value
                        it[orientation] = item.orientation.value
                        it[redshift] = item.redshift
                        it[parallax] = item.parallax.value
                        it[radialVelocity] = item.radialVelocity.value
                        it[distance] = item.distance
                        it[pmRA] = item.pmRA.value
                        it[pmDEC] = item.pmDEC.value
                        it[constellation] = item.constellation
                    }

                    for (dsoName in item.names) {
                        NameEntity.insert {
                            it[id] = namesId.getAndIncrement()
                            it[name] = dsoName
                            it[dso] = item.id
                        }
                    }
                }
            }

            val hyg = okHttpClient.download(
                "https://github.com/astronexus/HYG-Database/raw/master/hyg/v3/hyg.csv",
                Paths.get(".cache/hyg.csv")
            )

            with(HygDatabase()) {
                load(hyg.buffer().inputStream())

                val hipCatalog = simbadService.simbadCatalog(CatalogType.HIP)
                val hdCatalog = simbadService.simbadCatalog(CatalogType.HD)

                for (item in this) {
                    val simbadObject = hipCatalog["${item.hip}"] ?: hdCatalog["${item.hd}"]
                    val plx = simbadObject?.plx?.mas ?: item.parallax

                    StarEntity.insert {
                        it[id] = item.id
                        it[hr] = item.hr
                        it[hd] = item.hd
                        it[hip] = item.hip
                        it[magnitude] = item.magnitude
                        it[rightAscension] = item.rightAscension.value
                        it[declination] = item.declination.value
                        it[spType] = (simbadObject?.spType ?: item.spType)?.ifEmpty { null }
                        it[redshift] = simbadObject?.redshift ?: item.redshift
                        it[parallax] = simbadObject?.plx?.mas?.value ?: item.parallax.value
                        it[radialVelocity] = simbadObject?.rv?.kms?.value ?: item.radialVelocity.value
                        it[distance] = if (item.distance == 0.0 && plx.value != 0.0) 3.2615637769 / plx.arcsec else item.distance
                        it[pmRA] = item.pmRA.value
                        it[pmDEC] = item.pmDEC.value
                        it[type] = simbadObject?.type ?: item.type
                        it[constellation] = item.constellation
                    }

                    for (starName in item.names) {
                        NameEntity.insert {
                            it[id] = namesId.getAndIncrement()
                            it[name] = starName
                            it[star] = item.id
                        }
                    }
                }
            }
        }

        Paths.get("desktop/src/main/resources/data/StarCatalog.db.gz")
            .sink().gzip().use {
                Paths.get("desktop/src/main/resources/data/StarCatalog.db").source().use(it.buffer()::writeAll)
                it.flush()
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

    @JvmStatic
    private fun SimbadService.simbadCatalog(type: CatalogType): Map<String, SimbadObject> {
        val res = LinkedHashMap<String, SimbadObject>()
        val query = SimbadQuery()

        query.catalog(type)
        query.id(0)
        query.limit(20000)
        query.magnitude(max = 10.0)

        while (true) {
            val data = query(query).execute().body() ?: break
            if (data.isEmpty()) break

            for (item in data) {
                val key = item.names.first { it.type == type }.name
                res[key] = item
            }

            query.id(data.last().id.toInt() + 1)
        }

        return res
    }
}
