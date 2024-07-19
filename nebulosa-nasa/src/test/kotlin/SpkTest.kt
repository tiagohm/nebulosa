import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.shouldBe
import nebulosa.io.seekableSource
import nebulosa.nasa.daf.RemoteDaf
import nebulosa.nasa.daf.SourceDaf
import nebulosa.nasa.spk.Spk
import nebulosa.test.AbstractTest
import nebulosa.test.concat
import nebulosa.test.dataDirectory
import nebulosa.time.TimeYMDHMS
import nebulosa.time.UTC
import org.junit.jupiter.api.Test

class SpkTest : AbstractTest() {

    @Test
    fun de421SsbEarthBarycenter() {
        val spk = Spk(RemoteDaf("https://naif.jpl.nasa.gov/pub/naif/generic_kernels/spk/planets/a_old_versions/de421.bsp"))
        val (p, v) = spk[0, 3]!!.compute(UTC(TimeYMDHMS(2022, 12, 8, 20, 7, 15.0)))

        p[0] shouldBe (2.226291206593103E-01 plusOrMinus 1e-6)
        p[1] shouldBe (8.786267892743717E-01 plusOrMinus 1e-6)
        p[2] shouldBe (3.811036725894850E-01 plusOrMinus 1e-6)
        v[0] shouldBe (-1.700037773927917E-02 plusOrMinus 1e-6)
        v[1] shouldBe (3.644822472013536E-03 plusOrMinus 1e-6)
        v[2] shouldBe (1.580159029289274E-03 plusOrMinus 1e-6)
    }

    @Test
    fun de405SsbEarthBarycenter() {
        val spk = Spk(RemoteDaf("https://naif.jpl.nasa.gov/pub/naif/generic_kernels/spk/planets/a_old_versions/de405.bsp"))
        val (p, v) = spk[0, 3]!!.compute(UTC(TimeYMDHMS(2022, 12, 8, 20, 7, 15.0)))

        p[0] shouldBe (2.226291206593103E-01 plusOrMinus 1e-6)
        p[1] shouldBe (8.786267892743717E-01 plusOrMinus 1e-6)
        p[2] shouldBe (3.811036725894850E-01 plusOrMinus 1e-6)
        v[0] shouldBe (-1.700037773927917E-02 plusOrMinus 1e-6)
        v[1] shouldBe (3.644822472013536E-03 plusOrMinus 1e-6)
        v[2] shouldBe (1.580159029289274E-03 plusOrMinus 1e-6)
    }

    @Test
    fun type2165803Didymos() {
        val spk = Spk(SourceDaf(dataDirectory.concat("65803 Didymos.bsp").seekableSource().autoClose()))
        val (p, v) = spk[10, 2065803]!!.compute(UTC(TimeYMDHMS(2022, 12, 8, 20, 7, 15.0)))
        p[0] shouldBe (1.231026319338612E-01 plusOrMinus 1e-2)
        p[1] shouldBe (1.022833989843715E+00 plusOrMinus 1e-2)
        p[2] shouldBe (4.567595812943146E-01 plusOrMinus 1e-2)
        v[0] shouldBe (-1.739740083644565E-02 plusOrMinus 1e-2)
        v[1] shouldBe (5.410812824810350E-03 plusOrMinus 1e-2)
        v[2] shouldBe (3.549254153190032E-03 plusOrMinus 1e-2)
    }
}
