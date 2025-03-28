import io.kotest.matchers.ints.shouldBeExactly
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import nebulosa.nasa.daf.RemoteDaf
import org.junit.jupiter.api.Test

class DafTest {

    @Test
    fun naifDaf() {
        val daf = RemoteDaf("https://naif.jpl.nasa.gov/pub/naif/generic_kernels/spk/planets/a_old_versions/de405.bsp")
        daf.read()
        val summaries = daf.summaries

        summaries.size shouldBeExactly 15

        val data = arrayOf(
            arrayOf<Number>(-1.5778799588160586E9, 1.5778800641839132E9, 1, 0, 1, 2),
            arrayOf<Number>(-1.5778799588160586E9, 1.5778800641839132E9, 2, 0, 1, 2),
            arrayOf<Number>(-1.5778799588160586E9, 1.5778800641839132E9, 3, 0, 1, 2),
            arrayOf<Number>(-1.5778799588160586E9, 1.5778800641839132E9, 4, 0, 1, 2),
            arrayOf<Number>(-1.5778799588160586E9, 1.5778800641839132E9, 5, 0, 1, 2),
            arrayOf<Number>(-1.5778799588160586E9, 1.5778800641839132E9, 6, 0, 1, 2),
            arrayOf<Number>(-1.5778799588160586E9, 1.5778800641839132E9, 7, 0, 1, 2),
            arrayOf<Number>(-1.5778799588160586E9, 1.5778800641839132E9, 8, 0, 1, 2),
            arrayOf<Number>(-1.5778799588160586E9, 1.5778800641839132E9, 9, 0, 1, 2),
            arrayOf<Number>(-1.5778799588160586E9, 1.5778800641839132E9, 10, 0, 1, 2),
            arrayOf<Number>(-1.5778799588160586E9, 1.5778800641839132E9, 301, 3, 1, 2),
            arrayOf<Number>(-1.5778799588160586E9, 1.5778800641839132E9, 399, 3, 1, 2),
            arrayOf<Number>(-1.5778799588160586E9, 1.5778800641839132E9, 199, 1, 1, 2),
            arrayOf<Number>(-1.5778799588160586E9, 1.5778800641839132E9, 299, 2, 1, 2),
            arrayOf<Number>(-1.5778799588160586E9, 1.5778800641839132E9, 499, 4, 1, 2),
        )

        summaries.forEachIndexed { index, summary ->
            summary.name shouldBe "DE-405"

            summary.doubleAt(0) shouldBe data[index][0]
            summary.doubleAt(1) shouldBe data[index][1]
            summary.intAt(0) shouldBe data[index][2]
            summary.intAt(1) shouldBe data[index][3]
            summary.intAt(2) shouldBe data[index][4]
            summary.intAt(3) shouldBe data[index][5]
            summary.intAt(4) shouldBeGreaterThan 1024
            summary.intAt(5) shouldBeGreaterThan 1024
        }
    }

    @Test
    fun dafSpk() {
        val daf = RemoteDaf("https://naif.jpl.nasa.gov/pub/naif/generic_kernels/spk/planets/a_old_versions/de421.bsp")
        daf.read()
        val summaries = daf.summaries

        summaries.size shouldBeExactly 15

        val data = arrayOf(
            arrayOf<Number>(-3.1691952E9, 1.6968528E9, 1, 0, 1, 2),
            arrayOf<Number>(-3.1691952E9, 1.6968528E9, 2, 0, 1, 2),
            arrayOf<Number>(-3.1691952E9, 1.6968528E9, 3, 0, 1, 2),
            arrayOf<Number>(-3.1691952E9, 1.6968528E9, 4, 0, 1, 2),
            arrayOf<Number>(-3.1691952E9, 1.6968528E9, 5, 0, 1, 2),
            arrayOf<Number>(-3.1691952E9, 1.6968528E9, 6, 0, 1, 2),
            arrayOf<Number>(-3.1691952E9, 1.6968528E9, 7, 0, 1, 2),
            arrayOf<Number>(-3.1691952E9, 1.6968528E9, 8, 0, 1, 2),
            arrayOf<Number>(-3.1691952E9, 1.6968528E9, 9, 0, 1, 2),
            arrayOf<Number>(-3.1691952E9, 1.6968528E9, 10, 0, 1, 2),
            arrayOf<Number>(-3.1691952E9, 1.6968528E9, 301, 3, 1, 2),
            arrayOf<Number>(-3.1691952E9, 1.6968528E9, 399, 3, 1, 2),
            arrayOf<Number>(-3.1691952E9, 1.6968528E9, 199, 1, 1, 2),
            arrayOf<Number>(-3.1691952E9, 1.6968528E9, 299, 2, 1, 2),
            arrayOf<Number>(-3.1691952E9, 1.6968528E9, 499, 4, 1, 2),
        )

        summaries.forEachIndexed { index, summary ->
            summary.name shouldBe "DE-0421LE-0421"

            summary.doubleAt(0) shouldBe data[index][0]
            summary.doubleAt(1) shouldBe data[index][1]
            summary.intAt(0) shouldBe data[index][2]
            summary.intAt(1) shouldBe data[index][3]
            summary.intAt(2) shouldBe data[index][4]
            summary.intAt(3) shouldBe data[index][5]
            summary.intAt(4) shouldBeGreaterThan 512
            summary.intAt(5) shouldBeGreaterThan 512
        }
    }

    @Test
    fun dafPck() {
        val daf = RemoteDaf("https://naif.jpl.nasa.gov/pub/naif/generic_kernels/pck/moon_pa_de421_1900-2050.bpc")
        daf.read()
        val summaries = daf.summaries

        summaries.size shouldBeExactly 1
        summaries[0].name shouldBe "de421.nio"
        val data = arrayOf<Number>(-3.1557168E9, 1.609416E9, 31006, 1, 2, 641, 221284)

        summaries[0].doubleAt(0) shouldBe data[0]
        summaries[0].doubleAt(1) shouldBe data[1]
        summaries[0].intAt(0) shouldBe data[2]
        summaries[0].intAt(1) shouldBe data[3]
        summaries[0].intAt(2) shouldBe data[4]
        summaries[0].intAt(3) shouldBe data[5]
        summaries[0].intAt(4) shouldBe data[6]
    }
}
