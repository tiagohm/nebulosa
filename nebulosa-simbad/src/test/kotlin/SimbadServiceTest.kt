import io.kotest.matchers.collections.shouldHaveAtLeastSize
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldStartWith
import nebulosa.adql.*
import nebulosa.math.arcmin
import nebulosa.math.deg
import nebulosa.math.hours
import nebulosa.simbad.SimbadService
import nebulosa.test.HTTP_CLIENT
import nebulosa.test.NonGitHubOnly
import org.junit.jupiter.api.Test

@NonGitHubOnly
class SimbadServiceTest {

    @Test
    fun identifierQuery() {
        val query = QueryBuilder()
        query.addAll(listOf(OID_COLUMN, OTYPE_COLUMN, RA_COLUMN, DEC_COLUMN, PM_RA_COLUMN, PM_DEC_COLUMN))
        query.addAll(listOf(PLX_COLUMN, RAD_VAL_COLUMN, REDSHIFT_COLUMN, MAIN_ID_COLUMN))
        val join = InnerJoin(BASIC_TABLE, IDENT_TABLE, arrayOf(OID_COLUMN equal Column("i.oidref")))
        query.add(join)
        query.add(NAME_COLUMN equal "ngc5128")

        val call = SERVICE.query(query.build())
        val rows = call.execute().body().shouldNotBeNull()

        with(rows[0]) {
            getField("oid") shouldBe "3392496"
            getField("ra") shouldStartWith "201.3650"
            getField("dec") shouldStartWith "-43.0191"
        }
    }

    @Test
    fun coordinateQuery() {
        val query = QueryBuilder()
        query.addAll(listOf(OID_COLUMN, OTYPE_COLUMN, RA_COLUMN, DEC_COLUMN, PM_RA_COLUMN, PM_DEC_COLUMN))
        query.addAll(listOf(PLX_COLUMN, RAD_VAL_COLUMN, REDSHIFT_COLUMN, MAIN_ID_COLUMN))
        query.add(BASIC_TABLE)
        query.add(SkyPoint(RA_COLUMN, DEC_COLUMN) contains Circle("20 54 05.689".hours, "+37 01 17.38".deg, 2.0.arcmin))

        val call = SERVICE.query(query.build())
        val rows = call.execute().body().shouldNotBeNull()

        rows shouldHaveAtLeastSize 4

        with(rows.first { it.getField("main_id") == "TYC 2700-2084-1" }) {
            getField("oid") shouldBe "6742437"
            getField("ra") shouldStartWith "313.5013"
            getField("dec") shouldStartWith "37.0192"
        }
    }

    @Test
    fun objectTypeQuery() {
        val query = QueryBuilder()
        query.add(Limit(100))
        query.addAll(listOf(OID_COLUMN, OTYPE_COLUMN, RA_COLUMN, DEC_COLUMN, PM_RA_COLUMN, PM_DEC_COLUMN))
        query.addAll(listOf(PLX_COLUMN, RAD_VAL_COLUMN, REDSHIFT_COLUMN, MAIN_ID_COLUMN))
        val join = InnerJoin(BASIC_TABLE, OTYPES_TABLE, arrayOf(OID_COLUMN equal OTYPES_TABLE.column("oidref")))
        query.add(join)
        query.add(OTYPES_TABLE.column("otype") equal "*")

        val call = SERVICE.query(query.build())
        val rows = call.execute().body().shouldNotBeNull()

        rows shouldHaveAtLeastSize 100

        with(rows.first { it.getField("main_id") == "TYC 2713-2426-1" }) {
            getField("oid") shouldBe "86"
            getField("ra") shouldStartWith "316.7063"
            getField("dec") shouldStartWith "36.7207"
            getField("otype") shouldBe "PM*"
        }
    }

    @Test
    fun constellationQuery() {
        val query = QueryBuilder()
        query.add(Limit(100))
        query.addAll(listOf(OID_COLUMN, OTYPE_COLUMN, RA_COLUMN, DEC_COLUMN, PM_RA_COLUMN, PM_DEC_COLUMN))
        query.addAll(listOf(PLX_COLUMN, RAD_VAL_COLUMN, REDSHIFT_COLUMN, MAIN_ID_COLUMN, MAG_COLUMN))
        val join = InnerJoin(BASIC_TABLE, FLUX_TABLE, arrayOf(OID_COLUMN equal FLUX_TABLE.column("oidref")))
        query.add(join)
        query.add(RA_COLUMN.isNotNull)
        query.add(SkyPoint(RA_COLUMN, DEC_COLUMN) contains ConstellationBoundary("CRU"))
        query.add(SortBy(MAG_COLUMN))

        val call = SERVICE.query(query.build())
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

    companion object {

        @JvmStatic private val SERVICE = SimbadService(httpClient = HTTP_CLIENT)
        @JvmStatic private val BASIC_TABLE = From("basic").alias("b")
        @JvmStatic private val IDENT_TABLE = From("ident").alias("i")
        @JvmStatic private val OTYPES_TABLE = From("otypes").alias("o")
        @JvmStatic private val FLUX_TABLE = From("allfluxes").alias("f")

        @JvmStatic private val OID_COLUMN = BASIC_TABLE.column("oid")
        @JvmStatic private val MAIN_ID_COLUMN = BASIC_TABLE.column("main_id")
        @JvmStatic private val OTYPE_COLUMN = BASIC_TABLE.column("otype")
        @JvmStatic private val RA_COLUMN = BASIC_TABLE.column("ra")
        @JvmStatic private val DEC_COLUMN = BASIC_TABLE.column("dec")
        @JvmStatic private val PM_RA_COLUMN = BASIC_TABLE.column("pmra")
        @JvmStatic private val PM_DEC_COLUMN = BASIC_TABLE.column("pmdec")
        @JvmStatic private val PLX_COLUMN = BASIC_TABLE.column("plx_value")
        @JvmStatic private val RAD_VAL_COLUMN = BASIC_TABLE.column("rvz_radvel")
        @JvmStatic private val REDSHIFT_COLUMN = BASIC_TABLE.column("rvz_redshift")
        @JvmStatic private val NAME_COLUMN = IDENT_TABLE.column("id")
        @JvmStatic private val MAG_COLUMN = FLUX_TABLE.column("V")
    }
}
