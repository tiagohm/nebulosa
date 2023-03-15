package nebulosa.nova.almanac

import nebulosa.math.Vector3D
import kotlin.math.pow


/**
 * Computes distance to intersections of a line and a sphere.
 *
 * Given a line through the origin (0,0,0) and an |xyz| [endpoint],
 * and a sphere with the |xyz| [center] and scalar [radius],
 * return the distance from the origin to their two intersections.
 *
 * If the line is tangent to the sphere, the two intersections will be
 * at the same distance. If the line does not intersect the sphere,
 * two [Double.NaN] values will be returned.
 *
 * @see <a href="http://paulbourke.net/geometry/circlesphere/index.html#linesphere">Reference</a>
 */
fun intersectLineAndSphere(
    endpoint: Vector3D,
    center: Vector3D,
    radius: Double,
): DoubleArray {
    val minusB = endpoint.normalized.dot(center) * 2.0
    val c = center.dot(center) - radius * radius
    val discriminant = minusB * minusB - 4 * c
    val dsqrt = discriminant.pow(if (discriminant < 0) Double.NaN else 0.5)
    return doubleArrayOf((minusB - dsqrt) / 2.0, (minusB + dsqrt) / 2.0)
}
