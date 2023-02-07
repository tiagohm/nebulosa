package nebulosa.projection

import nebulosa.math.Vector3D
import kotlin.math.hypot
import kotlin.math.sqrt

class Stereographic(center: Vector3D) : Projection {

    private val normalized = center.normalized
    private val cX = normalized.x
    private val cY = normalized.y
    private val cZ = normalized.z
    private val t0 = 1.0 / hypot(cX, cY)
    private val t2 = sqrt(-(cZ * cZ) + 1.0)
    private val t3 = t0 * t2
    private val t6 = t0 * cZ

    override fun project(position: Vector3D): Point {
        val u = position.normalized
        val (x, y, z) = u

        val t1 = x * cX
        val t4 = y * cY
        val t5 = 1 / (t1 * t3 + t3 * t4 + z * cZ + 1.0)

        return Point(t0 * t5 * (x * cY - cX * y), -t5 * (t1 * t6 - t2 * z + t4 * t6))
    }
}
