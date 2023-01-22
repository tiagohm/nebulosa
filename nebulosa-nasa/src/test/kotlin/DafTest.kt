import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.ints.shouldBeExactly
import io.kotest.matchers.shouldBe
import nebulosa.nasa.daf.SourceDaf
import java.io.File

class DafTest : StringSpec() {

    init {
        // https://naif.jpl.nasa.gov/pub/naif/generic_kernels/spk/planets/a_old_versions/de405.bsp
        "NAIF/DAF" {
            val source = File("../assets/DE405.bsp")
            val daf = SourceDaf(source)
            daf.initialize()
            val summaries = daf.summaries

            summaries.size shouldBeExactly 15

            val data = arrayOf(
                arrayOf(-1.26248112E10, 6.3472464E9, 1, 0, 1, 2, 1025, 1208740),
                arrayOf(-1.26248112E10, 6.3472464E9, 2, 0, 1, 2, 1208741, 1647912),
                arrayOf(-1.26248112E10, 6.3472464E9, 3, 0, 1, 2, 1647913, 2210600),
                arrayOf(-1.26248112E10, 6.3472464E9, 4, 0, 1, 2, 2210601, 2450774),
                arrayOf(-1.26248112E10, 6.3472464E9, 5, 0, 1, 2, 2450775, 2629190),
                arrayOf(-1.26248112E10, 6.3472464E9, 6, 0, 1, 2, 2629191, 2787020),
                arrayOf(-1.26248112E10, 6.3472464E9, 7, 0, 1, 2, 2787021, 2924264),
                arrayOf(-1.26248112E10, 6.3472464E9, 8, 0, 1, 2, 2924265, 3061508),
                arrayOf(-1.26248112E10, 6.3472464E9, 9, 0, 1, 2, 3061509, 3198752),
                arrayOf(-1.26248112E10, 6.3472464E9, 10, 0, 1, 2, 3198753, 3679096),
                arrayOf(-1.26248112E10, 6.3472464E9, 301, 3, 1, 2, 3679097, 5929836),
                arrayOf(-1.26248112E10, 6.3472464E9, 399, 3, 1, 2, 5929837, 8180576),
                arrayOf(-1.26248112E10, 6.3472464E9, 199, 1, 1, 2, 8180577, 8180588),
                arrayOf(-1.26248112E10, 6.3472464E9, 299, 2, 1, 2, 8180589, 8180600),
                arrayOf(-1.26248112E10, 6.3472464E9, 499, 4, 1, 2, 8180601, 8180612),
            )

            summaries.forEachIndexed { index, summary ->
                summary.name shouldBe "DE-0405LE-0405"

                summary.doubleAt(0) shouldBe data[index][0]
                summary.doubleAt(1) shouldBe data[index][1]
                summary.intAt(0) shouldBe data[index][2]
                summary.intAt(1) shouldBe data[index][3]
                summary.intAt(2) shouldBe data[index][4]
                summary.intAt(3) shouldBe data[index][5]
                summary.intAt(4) shouldBe data[index][6]
                summary.intAt(5) shouldBe data[index][7]
            }
        }
        // https://naif.jpl.nasa.gov/pub/naif/generic_kernels/spk/planets/a_old_versions/de421.bsp
        "DAF/SPK" {
            val source = File("../assets/DE421.bsp")
            val daf = SourceDaf(source)
            daf.initialize()
            val summaries = daf.summaries

            summaries.size shouldBeExactly 15

            val data = arrayOf(
                arrayOf(-3.1691952E9, 1.6968528E9, 1, 0, 1, 2, 513, 310276),
                arrayOf(-3.1691952E9, 1.6968528E9, 2, 0, 1, 2, 310277, 422920),
                arrayOf(-3.1691952E9, 1.6968528E9, 3, 0, 1, 2, 422921, 567244),
                arrayOf(-3.1691952E9, 1.6968528E9, 4, 0, 1, 2, 567245, 628848),
                arrayOf(-3.1691952E9, 1.6968528E9, 5, 0, 1, 2, 628849, 674612),
                arrayOf(-3.1691952E9, 1.6968528E9, 6, 0, 1, 2, 674613, 715096),
                arrayOf(-3.1691952E9, 1.6968528E9, 7, 0, 1, 2, 715097, 750300),
                arrayOf(-3.1691952E9, 1.6968528E9, 8, 0, 1, 2, 750301, 785504),
                arrayOf(-3.1691952E9, 1.6968528E9, 9, 0, 1, 2, 785505, 820708),
                arrayOf(-3.1691952E9, 1.6968528E9, 10, 0, 1, 2, 820709, 943912),
                arrayOf(-3.1691952E9, 1.6968528E9, 301, 3, 1, 2, 943913, 1521196),
                arrayOf(-3.1691952E9, 1.6968528E9, 399, 3, 1, 2, 1521197, 2098480),
                arrayOf(-3.1691952E9, 1.6968528E9, 199, 1, 1, 2, 2098481, 2098492),
                arrayOf(-3.1691952E9, 1.6968528E9, 299, 2, 1, 2, 2098493, 2098504),
                arrayOf(-3.1691952E9, 1.6968528E9, 499, 4, 1, 2, 2098505, 2098516),
            )

            summaries.forEachIndexed { index, summary ->
                summary.name shouldBe "DE-0421LE-0421"

                summary.doubleAt(0) shouldBe data[index][0]
                summary.doubleAt(1) shouldBe data[index][1]
                summary.intAt(0) shouldBe data[index][2]
                summary.intAt(1) shouldBe data[index][3]
                summary.intAt(2) shouldBe data[index][4]
                summary.intAt(3) shouldBe data[index][5]
                summary.intAt(4) shouldBe data[index][6]
                summary.intAt(5) shouldBe data[index][7]
            }
        }
        "DAF/PCK" {
            val source = File("../assets/MOON_PA_DE421_1900-2050.bpc")
            val daf = SourceDaf(source)
            daf.initialize()
            val summaries = daf.summaries

            summaries.size shouldBeExactly 1
            summaries[0].name shouldBe "de421.nio"
            val data = arrayOf(-3.1557168E9, 1.609416E9, 31006, 1, 2, 641, 221284)

            summaries[0].doubleAt(0) shouldBe data[0]
            summaries[0].doubleAt(1) shouldBe data[1]
            summaries[0].intAt(0) shouldBe data[2]
            summaries[0].intAt(1) shouldBe data[3]
            summaries[0].intAt(2) shouldBe data[4]
            summaries[0].intAt(3) shouldBe data[5]
            summaries[0].intAt(4) shouldBe data[6]
        }
    }
}
