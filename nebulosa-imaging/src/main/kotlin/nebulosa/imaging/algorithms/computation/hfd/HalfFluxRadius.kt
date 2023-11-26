package nebulosa.imaging.algorithms.computation.hfd

import kotlin.math.sqrt

data object HalfFluxRadius {

    fun compute(
        cx: Float, cy: Float, mass: Float,
        data: List<FloatArray>,
    ): Float {
        // Hot pixel?
        if (data.size <= 1) return 0.25f

        val hfr = Array(data.size) {
            val (px, py, m) = data[it]
            val dx = px - cx
            val dy = py - cy
            val r2 = dx * dx + dy * dy
            floatArrayOf(m, r2)
        }

        hfr.sortBy { it[1] }

        var m0 = 0f
        var m1 = 0f
        var r20 = 0f
        var r21 = 0f

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
            0.25f
        }
    }
}
