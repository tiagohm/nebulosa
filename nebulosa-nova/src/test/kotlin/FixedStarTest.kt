import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.shouldBe
import nebulosa.io.resource
import nebulosa.math.Angle.Companion.deg
import nebulosa.math.Angle.Companion.mas
import nebulosa.math.Velocity.Companion.kms
import nebulosa.nasa.daf.RemoteDaf
import nebulosa.nasa.spk.Spk
import nebulosa.nova.astrometry.FixedStar
import nebulosa.nova.astrometry.SpiceKernel
import nebulosa.nova.position.Barycentric
import nebulosa.time.IERS
import nebulosa.time.IERSA
import nebulosa.time.TimeYMDHMS
import nebulosa.time.UTC

class FixedStarTest : StringSpec() {

    init {
        IERSA.load(resource("finals2000A.all")!!)
        IERS.attach(IERSA)

        "polaris" {
            // https://api.noctuasky.com/api/v1/skysources/name/NAME%20Polaris
            val star = FixedStar(
                37.95456067.deg, 89.26410897.deg,
                (44.48).mas, (-11.85).mas, (7.54).mas, (-16.42).kms,
            )

            val spk = Spk(RemoteDaf("https://naif.jpl.nasa.gov/pub/naif/generic_kernels/spk/planets/a_old_versions/de421.bsp"))
            val kernel = SpiceKernel(spk)

            val astrometric = kernel[399]
                .at<Barycentric>(UTC(TimeYMDHMS(2023, 1, 1, 15, 0, 0.0))).observe(star)

            val (ra, dec, dist) = astrometric.equatorialAtDate()

            ra.normalized.hours shouldBe (3.0115471963487153 plusOrMinus 1e-8)
            dec.degrees shouldBe (89.36032606627879 plusOrMinus 1e-8)
            dist.value shouldBe (27355995.0433298 plusOrMinus 1e-6)
        }
    }
}
