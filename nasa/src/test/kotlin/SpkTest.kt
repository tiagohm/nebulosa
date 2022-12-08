import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.shouldBe
import nebulosa.constants.J2000
import nebulosa.nasa.daf.SourceDaf
import nebulosa.nasa.spk.Spk
import nebulosa.time.TDB
import nebulosa.time.TimeJD
import java.io.File

class SpkTest : StringSpec() {

    init {

        timeout = 1000L

        // https://naif.jpl.nasa.gov/pub/naif/generic_kernels/spk/planets/a_old_versions/de421.bsp
        "DE421: SSB - Earth Barycenter" {
            val source = File("../assets/DE421.bsp")
            val spk = Spk(SourceDaf(source))

            val data = arrayOf(
                doubleArrayOf(
                    2414864.5,
                    92098385.7843526, -110446323.01007293, -47933358.9410614,
                    2011650.8349494017, 1413901.922546147, 613451.7205519667
                ),
                doubleArrayOf(
                    2459902.5,
                    80662732.97603533, 112939337.27294622, 48991634.65907695,
                    -2183228.9746516044, 1299676.1724629025, 563421.642946979
                ),
                doubleArrayOf(
                    2470864.5,
                    72753268.25011948, 118405838.0125127, 51316088.617024414,
                    -2285949.9548114464, 1148801.8387569282, 497940.84684531845
                ),
            )

            for (a in data) {
                val b = spk[0, 3]!!.compute(TimeJD(a[0]))

                b.first[0].shouldBe(a[1] plusOrMinus 1e-8)
                b.first[1].shouldBe(a[2] plusOrMinus 1e-8)
                b.first[2].shouldBe(a[3] plusOrMinus 1e-8)

                b.second[0].shouldBe(a[4] plusOrMinus 1e-8)
                b.second[1].shouldBe(a[5] plusOrMinus 1e-8)
                b.second[2].shouldBe(a[6] plusOrMinus 1e-8)
            }
        }
        // https://naif.jpl.nasa.gov/pub/naif/generic_kernels/spk/planets/a_old_versions/de405.bsp
        "DE405: SSB - Earth Barycenter" {
            val source = File("../assets/DE405.bsp")
            val spk = Spk(SourceDaf(source))

            val data = arrayOf(
                doubleArrayOf(
                    2414864.5,
                    92098386.42073137, -110446323.73556949, -47933358.40404554,
                    2011650.8348978553, 1413901.9309391663, 613451.7027442808
                ),
                doubleArrayOf(
                    2459902.5,
                    80662731.75111333, 112939338.52705082, 48991633.68797776,
                    -2183228.9821606628, 1299676.1625413185, 563421.6367061776
                ),
                doubleArrayOf(
                    2470864.5,
                    72753265.9195635, 118405839.58208825, 51316088.00707238,
                    -2285949.964223509, 1148801.8237617193, 497940.8407130948
                ),
            )

            for (a in data) {
                val b = spk[0, 3]!!.compute(TimeJD(a[0]))

                b.first[0].shouldBe(a[1] plusOrMinus 1e-8)
                b.first[1].shouldBe(a[2] plusOrMinus 1e-8)
                b.first[2].shouldBe(a[3] plusOrMinus 1e-8)

                b.second[0].shouldBe(a[4] plusOrMinus 1e-8)
                b.second[1].shouldBe(a[5] plusOrMinus 1e-8)
                b.second[2].shouldBe(a[6] plusOrMinus 1e-8)
            }
        }
        "65803 Didymos (Type 21)" {
            val source = File("../assets/65803 Didymos.bsp")
            val spk = Spk(SourceDaf(source))
            val (p, v) = spk[0, 2065803]!!.compute(TDB(TimeJD(J2000, 0.7065000000000000E+04)))
            p[0] shouldBe (-0.3182740418953450E+09 plusOrMinus 1e-8)
            p[1] shouldBe (-0.4181692099565738E+08 plusOrMinus 1e-8)
            p[2] shouldBe (0.8948716100331508E+06 plusOrMinus 1e-8)
            v[0] shouldBe (-0.2156253652003626E+01 plusOrMinus 1e-8)
            v[1] shouldBe (-0.1536768916314177E+02 plusOrMinus 1e-8)
            v[2] shouldBe (-0.6844721896204574E+01 plusOrMinus 1e-8)
        }
    }
}
