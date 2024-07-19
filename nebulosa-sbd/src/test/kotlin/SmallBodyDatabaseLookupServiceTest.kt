import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.ints.shouldBeExactly
import io.kotest.matchers.ints.shouldBeGreaterThanOrEqual
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import nebulosa.sbd.SmallBodyDatabaseService
import nebulosa.test.HTTP_CLIENT
import org.junit.jupiter.api.Test

class SmallBodyDatabaseLookupServiceTest {

    @Test
    fun searchMatchesSingleRecord() {
        val body = SERVICE.search("C/2017 K2").execute().body().shouldNotBeNull()
        body.list.shouldBeNull()
        body.orbit!!.equinox shouldBe "J2000"
        body.body!!.fullname shouldBe "C/2017 K2 (PANSTARRS)"
        body.body!!.spkId shouldBeExactly 1003517
    }

    @Test
    fun searchNeo() {
        val body = SERVICE.search("2023 GA2").execute().body().shouldNotBeNull()
        body.list.shouldBeNull()
        body.orbit!!.equinox shouldBe "J2000"
        body.body!!.fullname shouldBe "(2023 GA2)"
        body.body!!.spkId shouldBeExactly 54354395
    }

    @Test
    fun searchMatchesMultipleRecords() {
        val body = SERVICE.search("PANSTARRS").execute().body().shouldNotBeNull()
        val list = body.list.shouldNotBeNull()
        list.size shouldBeGreaterThanOrEqual 257
        list.any { it.name == "253P/PANSTARRS" }.shouldBeTrue()
    }

    @Test
    fun searchFailed() {
        val body = SERVICE.search("ggdgdfgdfgdg").execute().body().shouldNotBeNull()
        body.list.shouldBeNull()
        body.orbit.shouldBeNull()
        body.body.shouldBeNull()
        body.physical.shouldBeNull()
        body.message shouldBe "specified object was not found"
    }

    companion object {

        @JvmStatic private val SERVICE = SmallBodyDatabaseService(httpClient = HTTP_CLIENT)
    }
}
