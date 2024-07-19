import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.shouldBe
import nebulosa.time.S15
import nebulosa.time.Spline
import org.junit.jupiter.api.Test

class S15Test : Spline<DoubleArray> by S15 {

    @Test
    fun curve() {
        compute(0.0) shouldBe (10441.312576 plusOrMinus 1E-8)
        compute(100.0) shouldBe (9405.044447999999 plusOrMinus 1E-8)
        compute(200.0) shouldBe (8424.698832 plusOrMinus 1E-8)
        compute(300.0) shouldBe (7476.110944 plusOrMinus 1E-8)
        compute(400.0) shouldBe (6535.116 plusOrMinus 1E-8)
        compute(500.0) shouldBe (5586.600523148149 plusOrMinus 1E-8)
        compute(600.0) shouldBe (4651.654629629629 plusOrMinus 1E-8)
        compute(700.0) shouldBe (3760.419625 plusOrMinus 1E-8)
        compute(800.0) shouldBe (2943.036814814815 plusOrMinus 1E-8)
        compute(900.0) shouldBe (2229.647504629629 plusOrMinus 1E-8)
        compute(1000.0) shouldBe (1650.393 plusOrMinus 1E-8)
        compute(1100.0) shouldBe (1222.8812962962963 plusOrMinus 1E-8)
        compute(1200.0) shouldBe (914.6107037037036 plusOrMinus 1E-8)
        compute(1300.0) shouldBe (681.149 plusOrMinus 1E-8)
        compute(1400.0) shouldBe (482.288 plusOrMinus 1E-8)
        compute(1500.0) shouldBe (292.343 plusOrMinus 1E-8)
        compute(1600.0) shouldBe (109.127 plusOrMinus 1E-8)
        compute(1700.0) shouldBe (14.099507288629734 plusOrMinus 1E-8)
        compute(1800.0) shouldBe (18.367 plusOrMinus 1E-8)
        compute(1900.0) shouldBe (-1.977 plusOrMinus 1E-8)
        compute(2000.0) shouldBe (63.808962962962966 plusOrMinus 1E-8)
        compute(2100.0) shouldBe (-2952.9510000000005 plusOrMinus 1E-8)
        compute(2200.0) shouldBe (-31950.310259259262 plusOrMinus 1E-8)
        compute(2300.0) shouldBe (-117798.78062962965 plusOrMinus 1E-8)
        compute(2400.0) shouldBe (-291387.25100000005 plusOrMinus 1E-8)
        compute(2500.0) shouldBe (-583604.6102592595 plusOrMinus 1E-8)
        compute(2600.0) shouldBe (-1025339.7472962962 plusOrMinus 1E-8)
        compute(2700.0) shouldBe (-1647481.5510000002 plusOrMinus 1E-8)
        compute(2800.0) shouldBe (-2480918.9102592585 plusOrMinus 1E-8)
        compute(2900.0) shouldBe (-3556540.7139629633 plusOrMinus 1E-8)
        compute(3000.0) shouldBe (-4905235.851000001 plusOrMinus 1E-8)
    }

    @Test
    fun derivative() {
        with(derivative) {
            compute(0.0) shouldBe (-10.72284312 plusOrMinus 1E-8)
            compute(100.0) shouldBe (-10.04279408 plusOrMinus 1E-8)
            compute(200.0) shouldBe (-9.60439288 plusOrMinus 1E-8)
            compute(300.0) shouldBe (-9.40763952 plusOrMinus 1E-8)
            compute(400.0) shouldBe (-9.452531666666667 plusOrMinus 1E-8)
            compute(500.0) shouldBe (-9.467542361111112 plusOrMinus 1E-8)
            compute(600.0) shouldBe (-9.181140000000001 plusOrMinus 1E-8)
            compute(700.0) shouldBe (-8.593324583333334 plusOrMinus 1E-8)
            compute(800.0) shouldBe (-7.704096111111111 plusOrMinus 1E-8)
            compute(900.0) shouldBe (-6.513454583333333 plusOrMinus 1E-8)
            compute(1000.0) shouldBe (-5.0214 plusOrMinus 1E-8)
            compute(1100.0) shouldBe (-3.6039333333333334 plusOrMinus 1E-8)
            compute(1200.0) shouldBe (-2.635517777777778 plusOrMinus 1E-8)
            compute(1300.0) shouldBe (-2.106725 plusOrMinus 1E-8)
            compute(1400.0) shouldBe (-1.9072624999999999 plusOrMinus 1E-8)
            compute(1500.0) shouldBe (-1.9284100000000002 plusOrMinus 1E-8)
            compute(1600.0) shouldBe (-1.5739400000000001 plusOrMinus 1E-8)
            compute(1700.0) shouldBe (-0.23690262390670547 plusOrMinus 1E-8)
            compute(1800.0) shouldBe (-0.34809999999999997 plusOrMinus 1E-8)
            compute(1900.0) shouldBe (1.143 plusOrMinus 1E-8)
            compute(2000.0) shouldBe (0.32577777777777783 plusOrMinus 1E-8)
            compute(2100.0) shouldBe (-108.681 plusOrMinus 1E-8)
            compute(2200.0) shouldBe (-522.7476666666668 plusOrMinus 1E-8)
            compute(2300.0) shouldBe (-1245.7032222222224 plusOrMinus 1E-8)
            compute(2400.0) shouldBe (-2277.547666666667 plusOrMinus 1E-8)
            compute(2500.0) shouldBe (-3618.281000000001 plusOrMinus 1E-8)
            compute(2600.0) shouldBe (-5267.903222222221 plusOrMinus 1E-8)
            compute(2700.0) shouldBe (-7226.414333333333 plusOrMinus 1E-8)
            compute(2800.0) shouldBe (-9493.814333333332 plusOrMinus 1E-8)
            compute(2900.0) shouldBe (-12070.103222222226 plusOrMinus 1E-8)
            compute(3000.0) shouldBe (-14955.281000000003 plusOrMinus 1E-8)
        }
    }
}
