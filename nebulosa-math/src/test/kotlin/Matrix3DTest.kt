import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.doubles.shouldBeExactly
import io.kotest.matchers.shouldBe
import nebulosa.math.Matrix3D
import nebulosa.math.SEMICIRCLE
import nebulosa.math.Vector3D
import nebulosa.math.rad
import org.junit.jupiter.api.Test

@Suppress("FloatingPointLiteralPrecision")
class Matrix3DTest {

    @Test
    fun rotateX() {
        val m = Matrix3D(2.0, 3.0, 2.0, 3.0, 2.0, 3.0, 3.0, 4.0, 5.0)
        val r = m.rotateX(0.3456789.rad)
        r[0, 0] shouldBeExactly 2.0
        r[0, 1] shouldBeExactly 3.0
        r[0, 2] shouldBeExactly 2.0

        r[1, 0] shouldBe (3.839043388235612460 plusOrMinus 1e-12)
        r[1, 1] shouldBe (3.237033249594111899 plusOrMinus 1e-12)
        r[1, 2] shouldBe (4.516714379005982719 plusOrMinus 1e-12)

        r[2, 0] shouldBe (1.806030415924501684 plusOrMinus 1e-12)
        r[2, 1] shouldBe (3.085711545336372503 plusOrMinus 1e-12)
        r[2, 2] shouldBe (3.687721683977873065 plusOrMinus 1e-12)
    }

    @Test
    fun rotateY() {
        val m = Matrix3D(2.0, 3.0, 2.0, 3.0, 2.0, 3.0, 3.0, 4.0, 5.0)
        val r = m.rotateY(0.3456789.rad)
        r[0, 0] shouldBe (0.8651847818978159930 plusOrMinus 1e-12)
        r[0, 1] shouldBe (1.467194920539316554 plusOrMinus 1e-12)
        r[0, 2] shouldBe (0.1875137911274457342 plusOrMinus 1e-12)

        r[1, 0] shouldBeExactly 3.0
        r[1, 1] shouldBeExactly 2.0
        r[1, 2] shouldBeExactly 3.0

        r[2, 0] shouldBe (3.500207892850427330 plusOrMinus 1e-12)
        r[2, 1] shouldBe (4.779889022262298150 plusOrMinus 1e-12)
        r[2, 2] shouldBe (5.381899160903798712 plusOrMinus 1e-12)
    }

    @Test
    fun rotateZ() {
        val m = Matrix3D(2.0, 3.0, 2.0, 3.0, 2.0, 3.0, 3.0, 4.0, 5.0)
        val r = m.rotateZ(0.3456789.rad)
        r[0, 0] shouldBe (2.898197754208926769 plusOrMinus 1e-12)
        r[0, 1] shouldBe (3.500207892850427330 plusOrMinus 1e-12)
        r[0, 2] shouldBe (2.898197754208926769 plusOrMinus 1e-12)

        r[1, 0] shouldBe (2.144865911309686813 plusOrMinus 1e-12)
        r[1, 1] shouldBe (0.865184781897815993 plusOrMinus 1e-12)
        r[1, 2] shouldBe (2.144865911309686813 plusOrMinus 1e-12)

        r[2, 0] shouldBeExactly 3.0
        r[2, 1] shouldBeExactly 4.0
        r[2, 2] shouldBeExactly 5.0
    }

    @Test
    fun chainRotation() {
        val m0 = Matrix3D.IDENTITY.rotateZ(SEMICIRCLE).rotateX(-SEMICIRCLE).rotateY(SEMICIRCLE)
        val m1 = Matrix3D.rotZ(SEMICIRCLE).rotateX(-SEMICIRCLE).rotateY(SEMICIRCLE)
        m0.matrix shouldBe m1.matrix
    }

    @Test
    fun plusMatrix() {
        val m = Matrix3D(2.0, 3.0, 2.0, 3.0, 2.0, 3.0, 3.0, 4.0, 5.0)
        val n = Matrix3D(2.0, 3.0, 2.0, 3.0, 2.0, 3.0, 3.0, 4.0, 5.0)
        val r = m + n

        r[0, 0] shouldBeExactly 4.0
        r[0, 1] shouldBeExactly 6.0
        r[0, 2] shouldBeExactly 4.0

        r[1, 0] shouldBeExactly 6.0
        r[1, 1] shouldBeExactly 4.0
        r[1, 2] shouldBeExactly 6.0

        r[2, 0] shouldBeExactly 6.0
        r[2, 1] shouldBeExactly 8.0
        r[2, 2] shouldBeExactly 10.0
    }

    @Test
    fun minusMatrix() {
        val m = Matrix3D(2.0, 3.0, 2.0, 6.0, 9.0, 6.0, 3.0, 6.0, 5.0)
        val n = Matrix3D(1.0, 1.0, 5.0, 4.0, 5.0, 9.0, 2.0, 4.0, 4.0)
        val r = m - n

        r[0, 0] shouldBeExactly 1.0
        r[0, 1] shouldBeExactly 2.0
        r[0, 2] shouldBeExactly -3.0

        r[1, 0] shouldBeExactly 2.0
        r[1, 1] shouldBeExactly 4.0
        r[1, 2] shouldBeExactly -3.0

        r[2, 0] shouldBeExactly 1.0
        r[2, 1] shouldBeExactly 2.0
        r[2, 2] shouldBeExactly 1.0
    }

    @Test
    fun timesScalar() {
        val m = Matrix3D(2.0, 3.0, 2.0, 3.0, 2.0, 3.0, 3.0, 4.0, 5.0)
        val r = m * 5.0

        r[0, 0] shouldBeExactly 10.0
        r[0, 1] shouldBeExactly 15.0
        r[0, 2] shouldBeExactly 10.0

        r[1, 0] shouldBeExactly 15.0
        r[1, 1] shouldBeExactly 10.0
        r[1, 2] shouldBeExactly 15.0

        r[2, 0] shouldBeExactly 15.0
        r[2, 1] shouldBeExactly 20.0
        r[2, 2] shouldBeExactly 25.0
    }

    @Test
    fun transpose() {
        val m = Matrix3D(2.0, 3.0, 2.0, 3.0, 2.0, 3.0, 3.0, 4.0, 5.0)
        val r = m.transposed

        r[0, 0] shouldBeExactly 2.0
        r[0, 1] shouldBeExactly 3.0
        r[0, 2] shouldBeExactly 3.0

        r[1, 0] shouldBeExactly 3.0
        r[1, 1] shouldBeExactly 2.0
        r[1, 2] shouldBeExactly 4.0

        r[2, 0] shouldBeExactly 2.0
        r[2, 1] shouldBeExactly 3.0
        r[2, 2] shouldBeExactly 5.0
    }

    @Test
    fun timesVector() {
        val m = Matrix3D(2.0, 3.0, 2.0, 3.0, 2.0, 3.0, 3.0, 4.0, 5.0)
        val v = Vector3D(2.0, 3.0, 2.0)
        val r = m * v

        r[0] shouldBeExactly 17.0
        r[1] shouldBeExactly 18.0
        r[2] shouldBeExactly 28.0
    }

    @Test
    fun determinant() {
        val m = Matrix3D(2.0, 3.0, 2.0, 3.0, 2.0, 3.0, 3.0, 4.0, 5.0)
        m.determinant shouldBeExactly -10.0
    }

    @Test
    fun isEmpty() {
        Matrix3D(2.0, 3.0, 2.0, 3.0, 2.0, 3.0, 3.0, 4.0, 5.0).isEmpty().shouldBeFalse()
        Matrix3D(2.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0).isEmpty().shouldBeFalse()
        Matrix3D(0.0, 3.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0).isEmpty().shouldBeFalse()
        Matrix3D(0.0, 0.0, 4.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0).isEmpty().shouldBeFalse()
        Matrix3D(0.0, 0.0, 0.0, 3.0, 0.0, 0.0, 0.0, 0.0, 0.0).isEmpty().shouldBeFalse()
        Matrix3D(0.0, 0.0, 0.0, 0.0, 2.0, 0.0, 0.0, 0.0, 0.0).isEmpty().shouldBeFalse()
        Matrix3D(0.0, 0.0, 0.0, 0.0, 0.0, 3.0, 0.0, 0.0, 0.0).isEmpty().shouldBeFalse()
        Matrix3D(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 3.0, 0.0, 0.0).isEmpty().shouldBeFalse()
        Matrix3D(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 4.0, 0.0).isEmpty().shouldBeFalse()
        Matrix3D(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 5.0).isEmpty().shouldBeFalse()
        Matrix3D(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0).isEmpty().shouldBeTrue()
    }
}
