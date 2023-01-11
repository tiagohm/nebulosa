package nebulosa.guiding

import kotlin.math.sqrt

object HalfFluxRadius {

    fun compute(
        cx: Int,
        cy: Int,
        mass: Float,
        data: MutableList<R2M>
    ): Float {
        if (data.size == 1) return 0.25f // Hot pixel?

        // Compute HFR.
        for (r2m in data) {
            val dx = r2m.px - cx
            val dy = r2m.py - cy
            r2m.r2 = dx * dx + dy * dy
        }

        var m0 = 0f
        var m1 = 0f
        var r20 = 0
        var r21 = 0

        val halfm = 0.5f * mass

        data.sort()

        for (r2m in data) {
            r20 = r21
            m0 = m1
            r21 = r2m.r2
            m1 += r2m.m

            if (m1 > halfm) break
        }

        // Interpolate.
        return if (m1 > m0) {
            val r0 = sqrt(r20.toFloat())
            val r1 = sqrt(r21.toFloat())
            val s = (r1 - r0) / (m1 - m0)
            r0 + s * (halfm - m0)
        } else 0.25f
    }
}
