import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldHaveAtLeastSize
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldStartWith
import nebulosa.adql.*
import nebulosa.math.Angle.Companion.arcmin
import nebulosa.math.Angle.Companion.deg
import nebulosa.math.Angle.Companion.hours
import nebulosa.simbad.SimbadService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit

class SimbadServiceTest : StringSpec() {

    init {
        val httpClient = OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BASIC))
            .callTimeout(1, TimeUnit.MINUTES)
            .connectTimeout(1, TimeUnit.MINUTES)
            .readTimeout(1, TimeUnit.MINUTES)
            .build()

        val service = SimbadService(httpClient = httpClient)
        val basicTable = From("basic").alias("b")
        val identTable = From("ident").alias("i")
        val otypesTable = From("otypes").alias("o")
        val fluxTable = From("allfluxes").alias("f")

        val oidColumn = basicTable.column("oid")
        val mainIdColumn = basicTable.column("main_id")
        val otypeColumn = basicTable.column("otype")
        val raColumn = basicTable.column("ra")
        val decColumn = basicTable.column("dec")
        val pmRAColumn = basicTable.column("pmra")
        val pmDECColumn = basicTable.column("pmdec")
        val plxColumn = basicTable.column("plx_value")
        val radVelColumn = basicTable.column("rvz_radvel")
        val redshiftColumn = basicTable.column("rvz_redshift")
        val nameColumn = identTable.column("id")
        val magColumn = fluxTable.column("V")

        "identifier query" {
            val query = QueryBuilder()
            query.addAll(listOf(oidColumn, otypeColumn, raColumn, decColumn, pmRAColumn, pmDECColumn))
            query.addAll(listOf(plxColumn, radVelColumn, redshiftColumn, mainIdColumn))
            val join = InnerJoin(basicTable, identTable, arrayOf(oidColumn equal Column("i.oidref")))
            query.add(join)
            query.add(nameColumn equal "ngc5128")

            val call = service.query(query.build())
            val rows = call.execute().body().shouldNotBeNull()

            with(rows[0]) {
                getField("oid") shouldBe "3392496"
                getField("ra") shouldStartWith "201.3650"
                getField("dec") shouldStartWith "-43.0191"
            }
        }
        "coordinate query" {
            val query = QueryBuilder()
            query.addAll(listOf(oidColumn, otypeColumn, raColumn, decColumn, pmRAColumn, pmDECColumn))
            query.addAll(listOf(plxColumn, radVelColumn, redshiftColumn, mainIdColumn))
            query.add(basicTable)
            query.add(SkyPoint(raColumn, decColumn) contains Circle("20 54 05.689".hours, "+37 01 17.38".deg, 2.0.arcmin))

            val call = service.query(query.build())
            val rows = call.execute().body().shouldNotBeNull()

            rows shouldHaveAtLeastSize 4

            with(rows.first { it.getField("main_id") == "TYC 2700-2084-1" }) {
                getField("oid") shouldBe "6742437"
                getField("ra") shouldStartWith "313.5013"
                getField("dec") shouldStartWith "37.0192"
            }
        }
        "object type query" {
            val query = QueryBuilder()
            query.add(Limit(100))
            query.addAll(listOf(oidColumn, otypeColumn, raColumn, decColumn, pmRAColumn, pmDECColumn))
            query.addAll(listOf(plxColumn, radVelColumn, redshiftColumn, mainIdColumn))
            val join = InnerJoin(basicTable, otypesTable, arrayOf(oidColumn equal otypesTable.column("oidref")))
            query.add(join)
            query.add(otypesTable.column("otype") equal "*")

            val call = service.query(query.build())
            val rows = call.execute().body().shouldNotBeNull()

            rows shouldHaveAtLeastSize 100

            with(rows.first { it.getField("main_id") == "TYC 2713-2426-1" }) {
                getField("oid") shouldBe "86"
                getField("ra") shouldStartWith "316.7063"
                getField("dec") shouldStartWith "36.7207"
                getField("otype") shouldBe "PM*"
            }
        }
        "constellation query" {
            val query = QueryBuilder()
            query.add(Limit(100))
            query.addAll(listOf(oidColumn, otypeColumn, raColumn, decColumn, pmRAColumn, pmDECColumn))
            query.addAll(listOf(plxColumn, radVelColumn, redshiftColumn, mainIdColumn, magColumn))
            val join = InnerJoin(basicTable, fluxTable, arrayOf(oidColumn equal fluxTable.column("oidref")))
            query.add(join)
            query.add(raColumn.isNotNull)
            query.add(SkyPoint(raColumn, decColumn) contains ConstellationBoundary("CRU"))
            query.add(SortBy(magColumn))

            val call = service.query(query.build())
            val rows = call.execute().body().shouldNotBeNull()

            rows shouldHaveAtLeastSize 100

            with(rows.first { it.getField("main_id") == "Cl Collinder  258" }) {
                getField("oid") shouldBe "3297061"
                getField("otype") shouldBe "OpC"
                getField("ra") shouldStartWith "186.798"
                getField("dec") shouldStartWith "-60.767"
                getField("V") shouldBe "7.1"
            }
        }
    }
}
