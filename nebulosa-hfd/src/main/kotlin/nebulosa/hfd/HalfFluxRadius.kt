package nebulosa.hfd

import kotlin.math.sqrt

data class HalfFluxRadius(
    private val cx: Double, private val cy: Double, private val mass: Double,
    private val data: List<R2M>,
) {

    private val hfr = Array(data.size) {
        val r2m = data[it]
        val dx = r2m.px - cx
        val dy = r2m.py - cy
        val r2 = dx * dx + dy * dy
        doubleArrayOf(r2m.m, r2)
    }

    init {
        hfr.sortBy { it[1] }
    }

    fun compute(): Double {
        // Hot pixel?
        if (data.size <= 1) return 0.25

        var m0 = 0.0
        var m1 = 0.0
        var r20 = 0.0
        var r21 = 0.0

        val halfm = 0.5f * mass

        for (r2m in hfr) {
            r20 = r21
            m0 = m1

            r21 = r2m[1]
            m1 += r2m[0]

            if (m1 > halfm) break
        }

        // Interpolate.
        return if (m1 > m0) {
            val r0 = sqrt(r20)
            val r1 = sqrt(r21)
            val s = (r1 - r0) / (m1 - m0)
            r0 + s * (halfm - m0)
        } else {
            0.25
        }
    }
}
