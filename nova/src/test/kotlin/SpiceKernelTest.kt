import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.shouldBe
import nebulosa.io.seekableSource
import nebulosa.nasa.daf.Daf
import nebulosa.nasa.spk.Spk
import nebulosa.nova.astrometry.SpiceKernel
import nebulosa.nova.position.Barycentric
import nebulosa.time.TDB
import nebulosa.time.TimeYMDHMS
import java.io.File

@Suppress("FloatingPointLiteralPrecision")
class SpiceKernelTest : StringSpec() {

    init {
        val spk = Spk(Daf(File("../assets/MAR097.bsp").seekableSource()))
        val kernel = SpiceKernel(spk)

        "position of mars" {
            val mars = kernel[499]
            val time = TDB(TimeYMDHMS(2022, 11, 27, 22, 30, 0.0))
            val barycentric = mars.at<Barycentric>(time)
            barycentric.position[0] shouldBe (7.482139521504711E+07 plusOrMinus 5e-1)
            barycentric.position[1] shouldBe (1.957005895707266E+08 plusOrMinus 5e-1)
            barycentric.position[2] shouldBe (8.774122508349305E+07 plusOrMinus 5e-1)
        }
        "position of mars viewed from earth" {
            val earth = kernel[399]
            val mars = kernel[499]
            val time = TDB(TimeYMDHMS(2022, 11, 27, 22, 30, 0.0))
            val barycentric = earth.at<Barycentric>(time)
            val astrometric = barycentric.observe(mars)
            astrometric.position[0] shouldBe (14603184.156446915 plusOrMinus 1e-7)
            astrometric.position[1] shouldBe (72568268.9480419 plusOrMinus 1e-7)
            astrometric.position[2] shouldBe (34330157.101964995 plusOrMinus 1e-7)
        }
    }
}
