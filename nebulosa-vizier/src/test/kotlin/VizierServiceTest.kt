import io.kotest.matchers.ints.shouldBeExactly
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import nebulosa.vizier.VizierTAPService
import org.junit.jupiter.api.Test

class VizierServiceTest {

    @Test
    fun query() {
        val query = """
                SELECT TOP 100 sao.SAO, sao.HD, sao.Pmag, sao.Vmag, sao.SpType, sao.RA2000, sao.DE2000, sao.pmRA2000, sao.pmDE2000
                FROM "I/131A/sao" AS sao
                ORDER BY SAO ASC
            """.trimIndent()
        val data = SERVICE.query(query).execute().body().shouldNotBeNull()
        data.size shouldBeExactly 100

        data[0].getField("SAO") shouldBe "1"
        data[0].getField("HD") shouldBe "225019"
        data[0].getField("RA2000") shouldBe "0.6735416666666666"

        data[99].getField("SAO") shouldBe "100"
        data[99].getField("HD").trim() shouldBe ""
        data[99].getField("RA2000") shouldBe "9.303554166666665"
    }

    companion object {

        @JvmStatic private val SERVICE = VizierTAPService()
    }
}
