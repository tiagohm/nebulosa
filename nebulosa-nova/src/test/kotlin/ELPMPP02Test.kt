import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.shouldBe
import nebulosa.nova.astrometry.ELPMPP02
import nebulosa.time.TDB
import nebulosa.time.TimeJD

class ELPMPP02Test : StringSpec() {

    init {
        val moon = ELPMPP02
        val time = TDB(TimeJD(2459938.0, 0.5))

        "moon" {
            val (p, v) = moon.compute(time)
            p[0] shouldBe (1.013355885727306E-03 plusOrMinus 1e-9)
            p[1] shouldBe (-1.903485709903833E-03 plusOrMinus 1e-9)
            p[2] shouldBe (-1.047798412089101E-03 plusOrMinus 1e-9)
            v[0] shouldBe (5.762732121285166E-04 plusOrMinus 1e-9)
            v[1] shouldBe (2.476878261262097E-04 plusOrMinus 1e-9)
            v[2] shouldBe (8.902329774047208E-05 plusOrMinus 1e-9)
        }
    }
}
