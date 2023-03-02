import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.doubles.shouldBeExactly
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import nebulosa.math.Angle.Companion.deg
import nebulosa.simbad.CatalogType
import nebulosa.simbad.SimbadObjectType
import nebulosa.simbad.SimbadQuery
import nebulosa.simbad.SimbadService

class SimbadServiceTest : StringSpec() {

    init {
        "query circle" {
            val service = SimbadService()
            val query = SimbadQuery()
            query.circle(101.28715533.deg, (-16.71611586).deg, 0.1.deg)
            val objects = service.query(query).execute().body().shouldNotBeNull()
            objects shouldHaveSize 43
            val sirius = objects.first { o -> o.names.any { it.name == "Sirius" } }
            sirius.name shouldBe "* alf CMa"
            sirius.names.first { it.type == CatalogType.HIP }.name shouldBe "32349"
            sirius.names.first { it.type == CatalogType.HD }.name shouldBe "48915"
            sirius.names.first { it.type == CatalogType.SAO }.name shouldBe "151881"
            sirius.names.filter { it.type == CatalogType.NAME }.map { it.name }.shouldContainAll("Dog Star", "Sirius", "Sirius A")
            sirius.type shouldBe SimbadObjectType.SPECTROSCOPIC_BINARY
            sirius.ra shouldBeExactly 101.28715533333335
            sirius.dec shouldBeExactly -16.71611586111111
            sirius.pmRA shouldBeExactly -546.01
            sirius.pmDEC shouldBeExactly -1223.07
            sirius.plx shouldBeExactly 379.21
            sirius.v shouldBeExactly -1.46
            sirius.rv shouldBeExactly -5.5
            sirius.u shouldBeExactly -1.51
            sirius.b shouldBeExactly -1.46
            sirius.r shouldBeExactly -1.46
            sirius.i shouldBeExactly -1.43
            sirius.j shouldBeExactly -1.36
            sirius.h shouldBeExactly -1.33
            sirius.k shouldBeExactly -1.35
            sirius.redshift shouldBeExactly -1.834585695059676E-5
        }
        "query by catalog" {
            val service = SimbadService()
            val query = SimbadQuery()
            query.catalog(CatalogType.M)
            service.query(query).execute().body().shouldNotBeNull() shouldHaveSize 110
        }
    }
}
