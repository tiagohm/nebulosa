import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.doubles.shouldBeExactly
import nebulosa.math.Vector3D

class Vector3DTest : StringSpec() {

    init {
        "plus vector" {
            val m = Vector3D(2.0, 3.0, 2.0)
            val n = Vector3D(2.0, 3.0, 2.0)
            val r = m + n

            r[0] shouldBeExactly 4.0
            r[1] shouldBeExactly 6.0
            r[2] shouldBeExactly 4.0
        }
        "minus vector" {
            val m = Vector3D(2.0, 3.0, 2.0)
            val n = Vector3D(1.0, 1.0, 5.0)
            val r = m - n

            r[0] shouldBeExactly 1.0
            r[1] shouldBeExactly 2.0
            r[2] shouldBeExactly -3.0
        }
        "times scalar" {
            val m = Vector3D(2.0, 3.0, 2.0)
            val r = m * 5.0

            r[0] shouldBeExactly 10.0
            r[1] shouldBeExactly 15.0
            r[2] shouldBeExactly 10.0
        }
        "dot" {
            val m = Vector3D(2.0, 3.0, 2.0)
            val v = Vector3D(2.0, 3.0, 2.0)
            m.dot(v) shouldBeExactly 17.0
            m.dot(-v) shouldBeExactly -17.0
        }
        "cross" {
            val m = Vector3D(2.0, 3.0, 2.0)
            val v = Vector3D(3.0, 2.0, 3.0)
            val r = m.cross(v)

            r[0] shouldBeExactly 5.0
            r[1] shouldBeExactly 0.0
            r[2] shouldBeExactly -5.0
        }
        "is empty" {
            Vector3D(2.0, 3.0, 2.0).isEmpty().shouldBeFalse()
            Vector3D(2.0, 0.0, 0.0).isEmpty().shouldBeFalse()
            Vector3D(0.0, 3.0, 0.0).isEmpty().shouldBeFalse()
            Vector3D(0.0, 0.0, 4.0).isEmpty().shouldBeFalse()
            Vector3D(0.0, 0.0, 0.0).isEmpty().shouldBeTrue()
        }
    }
}
