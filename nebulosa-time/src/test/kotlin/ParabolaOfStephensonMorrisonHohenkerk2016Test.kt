import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.shouldBe
import nebulosa.time.ParabolaOfStephensonMorrisonHohenkerk2016
import nebulosa.time.Spline

class ParabolaOfStephensonMorrisonHohenkerk2016Test : StringSpec(), Spline<Double> by ParabolaOfStephensonMorrisonHohenkerk2016 {

    init {
        "parabola" {
            compute(0.0) shouldBe (10504.53125 plusOrMinus 1E-6)
            compute(100.0) shouldBe (9350.78125 plusOrMinus 1E-6)
            compute(200.0) shouldBe (8262.03125 plusOrMinus 1E-6)
            compute(300.0) shouldBe (7238.28125 plusOrMinus 1E-6)
            compute(400.0) shouldBe (6279.53125 plusOrMinus 1E-6)
            compute(500.0) shouldBe (5385.78125 plusOrMinus 1E-6)
            compute(600.0) shouldBe (4557.03125 plusOrMinus 1E-6)
            compute(700.0) shouldBe (3793.28125 plusOrMinus 1E-6)
            compute(800.0) shouldBe (3094.53125 plusOrMinus 1E-6)
            compute(900.0) shouldBe (2460.78125 plusOrMinus 1E-6)
            compute(1000.0) shouldBe (1892.03125 plusOrMinus 1E-6)
            compute(1100.0) shouldBe (1388.28125 plusOrMinus 1E-6)
            compute(1200.0) shouldBe (949.53125 plusOrMinus 1E-6)
            compute(1300.0) shouldBe (575.78125 plusOrMinus 1E-6)
            compute(1400.0) shouldBe (267.03125 plusOrMinus 1E-6)
            compute(1500.0) shouldBe (23.28125 plusOrMinus 1E-6)
            compute(1600.0) shouldBe (-155.46875 plusOrMinus 1E-6)
            compute(1700.0) shouldBe (-269.21875 plusOrMinus 1E-6)
            compute(1800.0) shouldBe (-317.96875 plusOrMinus 1E-6)
            compute(1900.0) shouldBe (-301.71875 plusOrMinus 1E-6)
            compute(2000.0) shouldBe (-220.46875 plusOrMinus 1E-6)
            compute(2100.0) shouldBe (-74.21875 plusOrMinus 1E-6)
            compute(2200.0) shouldBe (137.03125 plusOrMinus 1E-6)
            compute(2300.0) shouldBe (413.28125 plusOrMinus 1E-6)
            compute(2400.0) shouldBe (754.53125 plusOrMinus 1E-6)
            compute(2500.0) shouldBe (1160.78125 plusOrMinus 1E-6)
            compute(2600.0) shouldBe (1632.03125 plusOrMinus 1E-6)
            compute(2700.0) shouldBe (2168.28125 plusOrMinus 1E-6)
            compute(2800.0) shouldBe (2769.53125 plusOrMinus 1E-6)
            compute(2900.0) shouldBe (3435.78125 plusOrMinus 1E-6)
            compute(3000.0) shouldBe (4167.03125 plusOrMinus 1E-6)
        }
        "derivative" {
            with(derivative) {
                compute(0.0) shouldBe (-11.8625 plusOrMinus 1E-6)
                compute(100.0) shouldBe (-11.2125 plusOrMinus 1E-6)
                compute(200.0) shouldBe (-10.5625 plusOrMinus 1E-6)
                compute(300.0) shouldBe (-9.9125 plusOrMinus 1E-6)
                compute(400.0) shouldBe (-9.262500000000001 plusOrMinus 1E-6)
                compute(500.0) shouldBe (-8.6125 plusOrMinus 1E-6)
                compute(600.0) shouldBe (-7.9625 plusOrMinus 1E-6)
                compute(700.0) shouldBe (-7.3125 plusOrMinus 1E-6)
                compute(800.0) shouldBe (-6.6625000000000005 plusOrMinus 1E-6)
                compute(900.0) shouldBe (-6.0125 plusOrMinus 1E-6)
                compute(1000.0) shouldBe (-5.3625 plusOrMinus 1E-6)
                compute(1100.0) shouldBe (-4.7125 plusOrMinus 1E-6)
                compute(1200.0) shouldBe (-4.0625 plusOrMinus 1E-6)
                compute(1300.0) shouldBe (-3.4125 plusOrMinus 1E-6)
                compute(1400.0) shouldBe (-2.7625 plusOrMinus 1E-6)
                compute(1500.0) shouldBe (-2.1125000000000003 plusOrMinus 1E-6)
                compute(1600.0) shouldBe (-1.4625000000000001 plusOrMinus 1E-6)
                compute(1700.0) shouldBe (-0.8125 plusOrMinus 1E-6)
                compute(1800.0) shouldBe (-0.1625 plusOrMinus 1E-6)
                compute(1900.0) shouldBe (0.48750000000000004 plusOrMinus 1E-6)
                compute(2000.0) shouldBe (1.1375 plusOrMinus 1E-6)
                compute(2100.0) shouldBe (1.7875 plusOrMinus 1E-6)
                compute(2200.0) shouldBe (2.4375 plusOrMinus 1E-6)
                compute(2300.0) shouldBe (3.0875 plusOrMinus 1E-6)
                compute(2400.0) shouldBe (3.7375000000000003 plusOrMinus 1E-6)
                compute(2500.0) shouldBe (4.3875 plusOrMinus 1E-6)
                compute(2600.0) shouldBe (5.0375000000000005 plusOrMinus 1E-6)
                compute(2700.0) shouldBe (5.6875 plusOrMinus 1E-6)
                compute(2800.0) shouldBe (6.3375 plusOrMinus 1E-6)
                compute(2900.0) shouldBe (6.9875 plusOrMinus 1E-6)
                compute(3000.0) shouldBe (7.6375 plusOrMinus 1E-6)
            }
        }
    }
}
