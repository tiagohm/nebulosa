package nebulosa.skycatalog

import nebulosa.math.Vector3D
import kotlin.math.sqrt

class GeodesicGrid(val maxLevel: Int) {

    fun interface Traverser {

        fun traverse(level: Int, index: Int, c0: Vector3D, c1: Vector3D, c2: Vector3D)
    }

    val triangles: Array<Array<Triangle?>>

    init {
        require(maxLevel >= 0) { "maxLevel < 0: $maxLevel" }

        if (maxLevel > 0) {
            var numberOfTriangles = 20

            triangles = Array(maxLevel + 1) { arrayOfNulls<Triangle>(numberOfTriangles).also { numberOfTriangles *= 4 } }

            for (i in 0 until 20) {
                val corners = ICOSAHEDRON_TRIANGLES[i]

                initTriangle(
                    0, i,
                    ICOSAHEDRON_CORNERS[corners[0]],
                    ICOSAHEDRON_CORNERS[corners[1]],
                    ICOSAHEDRON_CORNERS[corners[2]],
                )
            }
        } else {
            triangles = emptyArray()
        }
    }

    fun visitTriangles(maxVisitLevel: Int, traverser: Traverser) {
        if (maxVisitLevel >= 0) {
            for (i in 0 until 20) {
                val corners = ICOSAHEDRON_TRIANGLES[i]
                visitTriangles(
                    0, i,
                    ICOSAHEDRON_CORNERS[corners[0]],
                    ICOSAHEDRON_CORNERS[corners[1]],
                    ICOSAHEDRON_CORNERS[corners[2]],
                    maxVisitLevel, traverser,
                )
            }
        }
    }

    fun zoneNumberForPoint(vector: Vector3D, searchLevel: Int): Int {
        for (i in 0 until 20) {
            var zoneNumber = i

            val corners = ICOSAHEDRON_TRIANGLES[i]
            val c0 = ICOSAHEDRON_CORNERS[corners[0]]
            val c1 = ICOSAHEDRON_CORNERS[corners[1]]
            val c2 = ICOSAHEDRON_CORNERS[corners[2]]

            if (c0.cross(c1).dot(vector) >= 0.0
                && c1.cross(c2).dot(vector) >= 0.0
                && c2.cross(c0).dot(vector) >= 0.0
            ) {
                // vector lies inside this icosahedron triangle.
                for (level in 0 until searchLevel) {
                    val triangle = triangles[level][i]!!

                    zoneNumber = zoneNumber shl 2

                    if (triangle.e1.cross(triangle.e2).dot(vector) <= 0.0) {
                        // zoneNumber += 0
                    } else if (triangle.e2.cross(triangle.e0).dot(vector) <= 0.0) {
                        zoneNumber += 1
                    } else if (triangle.e0.cross(triangle.e1).dot(vector) <= 0.0) {
                        zoneNumber += 2
                    } else {
                        zoneNumber += 3
                    }
                }

                return zoneNumber
            }
        }

        return -1
    }

    private fun visitTriangles(
        level: Int, index: Int,
        c0: Vector3D, c1: Vector3D, c2: Vector3D,
        maxVisitLevel: Int,
        traverser: Traverser,
    ) {
        traverser.traverse(level, index, c0, c1, c2)

        if (level < maxVisitLevel) {
            val triangle = triangles[level][index]!!
            val nextLevel = level + 1
            val nextIndex = index * 4
            visitTriangles(nextLevel, nextIndex + 0, c0, triangle.e2, triangle.e1, maxVisitLevel, traverser)
            visitTriangles(nextLevel, nextIndex + 1, triangle.e2, c1, triangle.e0, maxVisitLevel, traverser)
            visitTriangles(nextLevel, nextIndex + 2, triangle.e1, triangle.e0, c2, maxVisitLevel, traverser)
            visitTriangles(nextLevel, nextIndex + 3, triangle.e0, triangle.e1, triangle.e2, maxVisitLevel, traverser)
        }
    }

    private fun initTriangle(
        level: Int, index: Int,
        c0: Vector3D, c1: Vector3D, c2: Vector3D,
    ) {
        val e0 = (c1 + c2).normalized
        val e1 = (c2 + c0).normalized
        val e2 = (c0 + c1).normalized

        triangles[level][index] = Triangle(e0, e1, e2)

        val nextLevel = level + 1

        if (nextLevel < maxLevel) {
            val nextIndex = index * 4
            initTriangle(nextLevel, nextIndex + 0, c0, e2, e1)
            initTriangle(nextLevel, nextIndex + 1, e2, c1, e0)
            initTriangle(nextLevel, nextIndex + 2, e1, e0, c2)
            initTriangle(nextLevel, nextIndex + 3, e0, e1, e2)
        }
    }

    companion object {

        // TODO: Convert to const val.
        private val ICOSAHEDRON_G = 0.5 * (1.0 + sqrt(5.0))
        private val ICOSAHEDRON_B = 1.0 / sqrt(1.0 + ICOSAHEDRON_G * ICOSAHEDRON_G)
        private val ICOSAHEDRON_A = ICOSAHEDRON_B * ICOSAHEDRON_G

        private val ICOSAHEDRON_CORNERS = arrayOf(
            Vector3D(ICOSAHEDRON_A, -ICOSAHEDRON_B, 0.0),
            Vector3D(ICOSAHEDRON_A, ICOSAHEDRON_B, 0.0),
            Vector3D(-ICOSAHEDRON_A, ICOSAHEDRON_B, 0.0),
            Vector3D(-ICOSAHEDRON_A, -ICOSAHEDRON_B, 0.0),
            Vector3D(0.0, ICOSAHEDRON_A, -ICOSAHEDRON_B),
            Vector3D(0.0, ICOSAHEDRON_A, ICOSAHEDRON_B),
            Vector3D(0.0, -ICOSAHEDRON_A, ICOSAHEDRON_B),
            Vector3D(0.0, -ICOSAHEDRON_A, -ICOSAHEDRON_B),
            Vector3D(-ICOSAHEDRON_B, 0.0, ICOSAHEDRON_A),
            Vector3D(ICOSAHEDRON_B, 0.0, ICOSAHEDRON_A),
            Vector3D(ICOSAHEDRON_B, 0.0, -ICOSAHEDRON_A),
            Vector3D(-ICOSAHEDRON_B, 0.0, -ICOSAHEDRON_A),
        )

        private val ICOSAHEDRON_TRIANGLES = arrayOf(
            intArrayOf(1, 0, 10), //  1
            intArrayOf(0, 1, 9),  //  0
            intArrayOf(0, 9, 6),  // 12
            intArrayOf(9, 8, 6),  //  9
            intArrayOf(0, 7, 10), // 16
            intArrayOf(6, 7, 0),  //  6
            intArrayOf(7, 6, 3),  //  7
            intArrayOf(6, 8, 3),  // 14
            intArrayOf(1, 10, 7), // 11
            intArrayOf(7, 3, 11), // 18
            intArrayOf(3, 2, 11), //  3
            intArrayOf(2, 3, 8),  //  2
            intArrayOf(0, 11, 4), // 10
            intArrayOf(2, 4, 11), // 19
            intArrayOf(5, 4, 2),  //  5
            intArrayOf(2, 8, 5),  // 15
            intArrayOf(4, 1, 10), // 17
            intArrayOf(4, 5, 1),  //  4
            intArrayOf(5, 9, 1),  // 13
            intArrayOf(8, 9, 5),  //  8
        )
    }
}
