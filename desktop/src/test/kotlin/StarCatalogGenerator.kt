import nebulosa.desktop.model.DeepSkyObjects
import nebulosa.desktop.model.Names
import nebulosa.desktop.model.Stars
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

object StarCatalogGenerator {

    @JvmStatic
    fun main(args: Array<String>) {
        Paths.get("desktop/src/main/resources/data/star.catalog.db").deleteIfExists()

        val client = OkHttpClient.Builder()
            .cache(Cache(File(".cache"), 1024 * 1024 * 32))
            .connectTimeout(1L, TimeUnit.MINUTES)
            .writeTimeout(1L, TimeUnit.MINUTES)
            .readTimeout(1L, TimeUnit.MINUTES)
            .callTimeout(1L, TimeUnit.MINUTES)
            .build()

        Database
            .connect(
                "jdbc:sqlite:desktop/src/main/resources/data/star.catalog.db",
                driver = JDBC::class.java.name
            )

        transaction {
            SchemaUtils.create(DeepSkyObjects, Stars, Names)

            val catalog = client.download(
                "https://github.com/Stellarium/stellarium/raw/master/nebulae/default/catalog.dat",
                Paths.get(".cache/catalog.dat"),
            ).gzip()

            val names = client.download(
                "https://github.com/Stellarium/stellarium/raw/master/nebulae/default/names.dat",
                Paths.get(".cache/names.dat"),
            )

            val namesId = AtomicInteger(1)

            with(Nebula()) {
                load(catalog, names)

                for (item in this) {
                    DeepSkyObjects.insert {
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
                        it[ced] = item.ced
                        it[pk] = item.pk
                        it[png] = item.png
                        it[snrg] = item.snrg
                        it[aco] = item.aco
                        it[hcg] = item.hcg
                        it[eso] = item.eso
                        it[vdbh] = item.vdbh
                        it[mB] = item.mB
                        it[mV] = item.mV
                        it[rightAscension] = item.rightAscension.value
                        it[declination] = item.declination.value
                        it[type] = item.type.name
                        it[mType] = item.mType
                        it[majorAxis] = item.majorAxis.value
                        it[minorAxis] = item.minorAxis.value
                        it[orientation] = item.orientation.value
                        it[redshift] = item.redshift
                        it[parallax] = item.parallax.value
                        it[radialVelocity] = item.radialVelocity.value
                        it[distance] = item.distance
                        it[pmRA] = item.pmRA.value
                        it[pmDEC] = item.pmDEC.value
                        it[constellation] = item.constellation.name
                    }

                    for (dsoName in item.names) {
                        Names.insert {
                            it[id] = namesId.getAndIncrement()
                            it[name] = dsoName
                            it[dso] = item.id
                        }
                    }
                }
            }

            val hyg = client.download(
                "https://github.com/astronexus/HYG-Database/raw/master/hygdata_v3.csv",
                Paths.get(".cache/hygdata_v3.csv")
            )

            with(HygDatabase()) {
                load(hyg.buffer().inputStream())

                for (item in this) {
                    Stars.insert {
                        it[id] = item.id
                        it[hr] = item.hr
                        it[hd] = item.hd
                        it[hip] = item.hip
                        it[sao] = item.sao
                        it[mB] = item.mB
                        it[mV] = item.mV
                        it[rightAscension] = item.rightAscension.value
                        it[declination] = item.declination.value
                        it[spType] = item.spType
                        it[redshift] = item.redshift
                        it[parallax] = item.parallax.value
                        it[radialVelocity] = item.radialVelocity.value
                        it[distance] = item.distance
                        it[pmRA] = item.pmRA.value
                        it[pmDEC] = item.pmDEC.value
                        it[type] = item.type.name
                        it[constellation] = item.constellation.name
                    }

                    for (dsoName in item.names) {
                        Names.insert {
                            it[id] = namesId.getAndIncrement()
                            it[name] = dsoName
                            it[star] = item.id
                        }
                    }
                }
            }
        }
    }

    @JvmStatic
    private fun OkHttpClient.download(url: String, output: Path): Source {
        return with(Request.Builder().url(url).build()) {
            val response = newCall(this).execute()
            response.use { output.outputStream().use(it.body.byteStream()::transferTo) }
            output.inputStream().source()
        }
    }
}
