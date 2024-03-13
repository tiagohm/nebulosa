import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.ints.shouldBeGreaterThanOrEqual
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import nebulosa.constants.AU_KM
import nebulosa.sbd.SmallBodyDatabaseService
import java.time.LocalDate

class SmallBodyCloseApprochServiceTest : StringSpec() {

    init {
        val service = SmallBodyDatabaseService()

        "search" {
            val data = service.closeApproaches(distance = 10, date = LocalDate.of(2024, 3, 13)).execute().body()

            data.shouldNotBeNull()
            data.count shouldBeGreaterThanOrEqual 1

            data.fields shouldHaveSize 13
            data.data shouldHaveSize 10

            data.data[0][0] shouldBe "2024 EC3"
            data.data[0][3] shouldBe "2024-Mar-13 07:22"
            data.data[0][4].toDouble() shouldBe (2.29 * 384400 / AU_KM plusOrMinus 1e-2)

            data.data.map { it[0] } shouldContainAll listOf(
                "2024 EC3", "2024 EL1", "2024 EH3",
                "2024 EK3", "2024 EQ", "2024 EK", "2024 EX2", "2024 ED3",
                "2024 EN", "2020 FD"
            )

            // https://minorplanetcenter.net/
            // ############# Close Approaches ############
            // 2024 EC3	    Mar 13 07:22	2.29	4-11
            // 2024 EL1	    Mar 13 13:10	8.48	9-30
            // 2024 EH3	    Mar 14 16:16	6.5	    16-50
            // 2024 EK3	    Mar 15 03:10	1.62	3-11
            // 2020 FU	    Mar 15 21:29	14.84	10-31
            // 2024 CJ8	    Mar 16 01:25	17.21	43-140
            // 2024 EQ	    Mar 16 06:31	6.92	12-37
            // 2022 EK1	    Mar 16 09:08	45.25	22-69
            // 2024 EK	    Mar 16 15:29	6.96	8-26
            // 2024 EX2	    Mar 16 16:03	5.82	10-32
            // 2004 FK2	    Mar 17 23:18	24.01	24-75
            // 2024 EW	    Mar 18 07:55	32.45	14-43
            // 2024 ED3	    Mar 18 08:37	3.5	    17-54
            // 2024 CH6	    Mar 18 08:42	47.08	61-190
            // 2024 EN	    Mar 18 18:34	3.87	23-74
            // 2020 FD	    Mar 18 19:11	4.52	5-17
            // 2011 BY24	Mar 18 22:50	26.14	56-180
        }
    }
}
