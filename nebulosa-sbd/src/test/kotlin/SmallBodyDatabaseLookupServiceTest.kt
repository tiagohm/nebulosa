import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.ints.shouldBeExactly
import io.kotest.matchers.ints.shouldBeGreaterThanOrEqual
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import nebulosa.sbd.SmallBodyDatabaseLookupService

class SmallBodyDatabaseLookupServiceTest : StringSpec() {

    init {
        val service = SmallBodyDatabaseLookupService()

        "search matches single record" {
            val body = service.search("C/2017 K2").execute().body().shouldNotBeNull()
            body.list.shouldBeNull()
            body.orbit!!.equinox shouldBe "J2000"
            body.body!!.fullname shouldBe "C/2017 K2 (PANSTARRS)"
            body.body!!.spkId shouldBeExactly 1003517
        }
        "search matches multiple records" {
            val body = service.search("PANSTARRS").execute().body().shouldNotBeNull()
            val list = body.list.shouldNotBeNull()
            list.size shouldBeGreaterThanOrEqual 257
            list.any { it.name == "253P/PANSTARRS" }.shouldBeTrue()
        }
        "search failed" {
            val body = service.search("ggdgdfgdfgdg").execute().body().shouldNotBeNull()
            body.list.shouldBeNull()
            body.orbit.shouldBeNull()
            body.body.shouldBeNull()
            body.physical.shouldBeNull()
            body.message shouldBe "specified object was not found"
        }
    }
}
