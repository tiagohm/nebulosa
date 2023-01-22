import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.shouldBe
import nebulosa.time.ParabolaOfStephensonMorrison2004
import nebulosa.time.Spline

class ParabolaOfStephensonMorrison2004Test : StringSpec(), Spline<Double> by ParabolaOfStephensonMorrison2004 {

    init {
        "parabola" {
            compute(0.0) shouldBe (10579.679999999998 plusOrMinus 1E-6)
            compute(100.0) shouldBe (9446.88 plusOrMinus 1E-6)
            compute(200.0) shouldBe (8378.08 plusOrMinus 1E-6)
            compute(300.0) shouldBe (7373.28 plusOrMinus 1E-6)
            compute(400.0) shouldBe (6432.48 plusOrMinus 1E-6)
            compute(500.0) shouldBe (5555.679999999999 plusOrMinus 1E-6)
            compute(600.0) shouldBe (4742.879999999999 plusOrMinus 1E-6)
            compute(700.0) shouldBe (3994.0799999999995 plusOrMinus 1E-6)
            compute(800.0) shouldBe (3309.2799999999997 plusOrMinus 1E-6)
            compute(900.0) shouldBe (2688.4799999999996 plusOrMinus 1E-6)
            compute(1000.0) shouldBe (2131.68 plusOrMinus 1E-6)
            compute(1100.0) shouldBe (1638.88 plusOrMinus 1E-6)
            compute(1200.0) shouldBe (1210.0800000000002 plusOrMinus 1E-6)
            compute(1300.0) shouldBe (845.2800000000001 plusOrMinus 1E-6)
            compute(1400.0) shouldBe (544.48 plusOrMinus 1E-6)
            compute(1500.0) shouldBe (307.68000000000006 plusOrMinus 1E-6)
            compute(1600.0) shouldBe (134.88000000000002 plusOrMinus 1E-6)
            compute(1700.0) shouldBe (26.08 plusOrMinus 1E-6)
            compute(1800.0) shouldBe (-18.72 plusOrMinus 1E-6)
            compute(1900.0) shouldBe (0.480000000000004 plusOrMinus 1E-6)
            compute(2000.0) shouldBe (83.68 plusOrMinus 1E-6)
            compute(2100.0) shouldBe (230.87999999999997 plusOrMinus 1E-6)
            compute(2200.0) shouldBe (442.08 plusOrMinus 1E-6)
            compute(2300.0) shouldBe (717.28 plusOrMinus 1E-6)
            compute(2400.0) shouldBe (1056.48 plusOrMinus 1E-6)
            compute(2500.0) shouldBe (1459.6799999999998 plusOrMinus 1E-6)
            compute(2600.0) shouldBe (1926.8799999999999 plusOrMinus 1E-6)
            compute(2700.0) shouldBe (2458.0800000000004 plusOrMinus 1E-6)
            compute(2800.0) shouldBe (3053.2800000000007 plusOrMinus 1E-6)
            compute(2900.0) shouldBe (3712.4800000000005 plusOrMinus 1E-6)
            compute(3000.0) shouldBe (4435.68 plusOrMinus 1E-6)
        }
        "derivative" {
            with(derivative) {
                compute(0.0) shouldBe (-11.648 plusOrMinus 1E-6)
                compute(100.0) shouldBe (-11.008 plusOrMinus 1E-6)
                compute(200.0) shouldBe (-10.368 plusOrMinus 1E-6)
                compute(300.0) shouldBe (-9.728 plusOrMinus 1E-6)
                compute(400.0) shouldBe (-9.088 plusOrMinus 1E-6)
                compute(500.0) shouldBe (-8.448 plusOrMinus 1E-6)
                compute(600.0) shouldBe (-7.808 plusOrMinus 1E-6)
                compute(700.0) shouldBe (-7.167999999999999 plusOrMinus 1E-6)
                compute(800.0) shouldBe (-6.528 plusOrMinus 1E-6)
                compute(900.0) shouldBe (-5.888 plusOrMinus 1E-6)
                compute(1000.0) shouldBe (-5.247999999999999 plusOrMinus 1E-6)
                compute(1100.0) shouldBe (-4.6080000000000005 plusOrMinus 1E-6)
                compute(1200.0) shouldBe (-3.9680000000000004 plusOrMinus 1E-6)
                compute(1300.0) shouldBe (-3.3280000000000003 plusOrMinus 1E-6)
                compute(1400.0) shouldBe (-2.688 plusOrMinus 1E-6)
                compute(1500.0) shouldBe (-2.048 plusOrMinus 1E-6)
                compute(1600.0) shouldBe (-1.4080000000000001 plusOrMinus 1E-6)
                compute(1700.0) shouldBe (-0.768 plusOrMinus 1E-6)
                compute(1800.0) shouldBe (-0.128 plusOrMinus 1E-6)
                compute(1900.0) shouldBe (0.512 plusOrMinus 1E-6)
                compute(2000.0) shouldBe (1.1520000000000001 plusOrMinus 1E-6)
                compute(2100.0) shouldBe (1.7919999999999998 plusOrMinus 1E-6)
                compute(2200.0) shouldBe (2.432 plusOrMinus 1E-6)
                compute(2300.0) shouldBe (3.072 plusOrMinus 1E-6)
                compute(2400.0) shouldBe (3.7119999999999997 plusOrMinus 1E-6)
                compute(2500.0) shouldBe (4.352 plusOrMinus 1E-6)
                compute(2600.0) shouldBe (4.992 plusOrMinus 1E-6)
                compute(2700.0) shouldBe (5.632000000000001 plusOrMinus 1E-6)
                compute(2800.0) shouldBe (6.272 plusOrMinus 1E-6)
                compute(2900.0) shouldBe (6.912000000000001 plusOrMinus 1E-6)
                compute(3000.0) shouldBe (7.5520000000000005 plusOrMinus 1E-6)
            }
        }
    }
}
