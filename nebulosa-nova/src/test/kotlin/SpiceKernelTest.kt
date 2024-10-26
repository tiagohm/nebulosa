import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.shouldBe
import nebulosa.nasa.daf.RemoteDaf
import nebulosa.nasa.spk.NAIF
import nebulosa.nasa.spk.Spk
import nebulosa.nova.astrometry.SpiceKernel
import nebulosa.nova.position.Barycentric
import nebulosa.nova.position.ICRF
import nebulosa.time.TDB
import nebulosa.time.TimeYMDHMS
import org.junit.jupiter.api.Test

class SpiceKernelTest {

    @Test
    fun ssbMarsBarycenter() {
        val mars = MAR097[NAIF.MARS_BARYCENTER]
        val barycentric = mars.at<Barycentric>(TIME)
        barycentric.position[0] shouldBe (0.5001501370337544 plusOrMinus 1e-13)
        barycentric.position[1] shouldBe (1.3081776387439241 plusOrMinus 1e-13)
        barycentric.position[2] shouldBe (0.5865138637117445 plusOrMinus 1e-13)
        barycentric.velocity[0] shouldBe (-0.012655073594243272 plusOrMinus 1e-13)
        barycentric.velocity[1] shouldBe (0.005210405441841531 plusOrMinus 1e-13)
        barycentric.velocity[2] shouldBe (0.0027316410426030074 plusOrMinus 1e-13)
    }

    @Test
    fun marsBarycenterMars() {
        val mars = MAR097[NAIF.MARS] - MAR097[NAIF.MARS_BARYCENTER]
        val icrf = mars.at<ICRF>(TIME)
        icrf.position[0] shouldBe (-6.973204561568923e-13 plusOrMinus 1e-13)
        icrf.position[1] shouldBe (-1.0364158902365925e-12 plusOrMinus 1e-13)
        icrf.position[2] shouldBe (-1.3393210655347855e-13 plusOrMinus 1e-13)
        icrf.velocity[0] shouldBe (1.7113987241274633e-11 plusOrMinus 1e-13)
        icrf.velocity[1] shouldBe (-5.325181995164045e-12 plusOrMinus 1e-13)
        icrf.velocity[2] shouldBe (-1.2236477437195075e-11 plusOrMinus 1e-13)
    }

    @Test
    fun positionOfMars() {
        val mars = MAR097[NAIF.MARS]
        val barycentric = mars.at<Barycentric>(TIME)
        barycentric.position[0] shouldBe (0.5001501370330571 plusOrMinus 1e-13)
        barycentric.position[1] shouldBe (1.3081776387428876 plusOrMinus 1e-13)
        barycentric.position[2] shouldBe (0.5865138637116106 plusOrMinus 1e-13)
        barycentric.velocity[0] shouldBe (-0.012655073577129285 plusOrMinus 1e-13)
        barycentric.velocity[1] shouldBe (0.005210405436516349 plusOrMinus 1e-13)
        barycentric.velocity[2] shouldBe (0.00273164103036653 plusOrMinus 1e-13)
    }

    @Test
    fun positionOfMarsViewedFromEarth() {
        val earth = MAR097[NAIF.EARTH]
        val mars = MAR097[NAIF.MARS]
        val barycentric = earth.at<Barycentric>(TIME)
        val astrometric = barycentric.observe(mars)
        astrometric.position[0] shouldBe (0.09761625675629965 plusOrMinus 1e-13)
        astrometric.position[1] shouldBe (0.48508891609539406 plusOrMinus 1e-13)
        astrometric.position[2] shouldBe (0.22948292606924786 plusOrMinus 1e-13)
        astrometric.velocity[0] shouldBe (0.003266059522699063 plusOrMinus 1e-13)
        astrometric.velocity[1] shouldBe (-0.0013034648919245393 plusOrMinus 1e-13)
        astrometric.velocity[2] shouldBe (-9.24056040658666e-05 plusOrMinus 1e-13)
    }

    companion object {

        private val TIME = TDB(TimeYMDHMS(2022, 11, 27, 22, 30, 0.0))
        private val MAR097 = SpiceKernel(Spk(RemoteDaf("https://naif.jpl.nasa.gov/pub/naif/generic_kernels/spk/satellites/mar097.bsp")))
    }
}
