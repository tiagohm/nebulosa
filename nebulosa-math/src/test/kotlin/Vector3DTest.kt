import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.doubles.shouldBeExactly
import io.kotest.matchers.shouldBe
import nebulosa.constants.PI
import nebulosa.constants.PIOVERTWO
import nebulosa.math.Vector3D
import nebulosa.math.deg
import nebulosa.math.toDegrees
import nebulosa.test.plusOrMinus

class Vector3DTest : StringSpec() {

    init {
        "plus vector" {
            Vector3D(2.0, 3.0, 2.0) + Vector3D(2.0, 3.0, 2.0) shouldBe Vector3D(4.0, 6.0, 4.0)
        }
        "minus vector" {
            Vector3D(2.0, 3.0, 2.0) - Vector3D(1.0, 1.0, 5.0) shouldBe Vector3D(1.0, 2.0, -3.0)
        }
        "times scalar" {
            Vector3D(2.0, 3.0, 2.0) * 5.0 shouldBe Vector3D(10.0, 15.0, 10.0)
        }
        "divide by scalar" {
            Vector3D(2.0, 3.0, 2.0) / 2.0 shouldBe Vector3D(1.0, 1.5, 1.0)
        }
        "dot" {
            val m = Vector3D(2.0, 3.0, 2.0)
            val v = Vector3D(2.0, 3.0, 2.0)
            m.dot(v) shouldBeExactly 17.0
            m.dot(-v) shouldBeExactly -17.0
        }
        "cross" {
            Vector3D(2.0, 3.0, 2.0).cross(Vector3D(3.0, 2.0, 3.0)) shouldBe Vector3D(5.0, 0.0, -5.0)
        }
        "is empty" {
            Vector3D(2.0, 3.0, 2.0).isEmpty().shouldBeFalse()
            Vector3D(2.0, 0.0, 0.0).isEmpty().shouldBeFalse()
            Vector3D(0.0, 3.0, 0.0).isEmpty().shouldBeFalse()
            Vector3D(0.0, 0.0, 4.0).isEmpty().shouldBeFalse()
            Vector3D(0.0, 0.0, 0.0).isEmpty().shouldBeTrue()
        }
        "right angle" {
            Vector3D.X.angle(Vector3D.Y) shouldBeExactly PIOVERTWO
        }
        "opposite" {
            Vector3D(1.0, 2.0, 3.0).angle(Vector3D(-1.0, -2.0, -3.0)) shouldBeExactly PI
        }
        "collinear" {
            Vector3D(2.0, -3.0, 1.0).angle(Vector3D(4.0, -6.0, 2.0)) shouldBeExactly 0.0
        }
        "general" {
            Vector3D(3.0, 4.0, 5.0).angle(Vector3D(1.0, 2.0, 2.0)).toDegrees shouldBeExactly 8.130102354156005
        }
        "rotate around x axis" {
            Vector3D.X.rotate(Vector3D.X, 90.0.deg) shouldBe Vector3D.X
        }
        "rotate around y axis" {
            Vector3D.Y.rotate(Vector3D.Y, 90.0.deg) shouldBe Vector3D.Y
        }
        "rotate around z axis" {
            Vector3D.Z.rotate(Vector3D.Z, 90.0.deg) shouldBe Vector3D.Z
        }
        "rotate" {
            val v = Vector3D(1.0, 2.0, 3.0)
            v.rotate(Vector3D.X, PI / 4.0) shouldBe (Vector3D(1.0, -0.707107, 3.535534) plusOrMinus 1e-6)
            v.rotate(Vector3D.Y, PI / 4.0) shouldBe (Vector3D(2.828427, 2.0, 1.414213) plusOrMinus 1e-6)
            v.rotate(Vector3D.Z, PI / 4.0) shouldBe (Vector3D(-0.707107, 2.12132, 3.0) plusOrMinus 1e-6)
            val axis = Vector3D(3.0, 4.0, 5.0)
            v.rotate(axis, 29.6512852.deg) shouldBe (Vector3D(1.21325856, 1.73061994, 3.08754891) plusOrMinus 1e-8)
            v.rotate(axis, 120.3053274.deg) shouldBe (Vector3D(2.08677229, 1.63198489, 2.64234871) plusOrMinus 1e-8)
            v.rotate(axis, 230.6512852.deg) shouldBe (Vector3D(1.69633894, 2.56816842, 2.12766190) plusOrMinus 1e-8)
            v.rotate(axis, 359.6139797.deg) shouldBe (Vector3D(0.99810712, 2.00381299, 2.99808533) plusOrMinus 1e-8)
        }
        "no rotation" {
            Vector3D(1.0, 2.0, 3.0).rotate(Vector3D.Y, 0.0) shouldBe Vector3D(1.0, 2.0, 3.0)
        }
    }
}
