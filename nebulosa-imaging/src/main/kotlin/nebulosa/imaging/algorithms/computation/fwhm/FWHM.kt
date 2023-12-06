package nebulosa.imaging.algorithms.computation.fwhm

import nebulosa.imaging.Image
import nebulosa.imaging.algorithms.ComputationAlgorithm
import kotlin.math.max
import kotlin.math.min

data class FWHM(
    private val x: Int, private val y: Int,
    private val mode: Mode = Mode.MIDDLE_ROW,
) : ComputationAlgorithm<Float> {

    enum class Mode {
        MIDDLE_ROW,
        AVERAGE_ROW,
        AVERAGE_COL,
    }

    override fun compute(source: Image): Float {
        return compute(source, x, y, mode)
    }

    companion object {

        const val HALFW = 10
        const val FULLW = 2 * HALFW + 1

        @JvmStatic
        fun compute(source: Image, x: Int, y: Int, mode: Mode = Mode.MIDDLE_ROW): Float {
            val profile = FloatArray(FULLW)

            val startX = max(0, min(x - HALFW, source.width - FULLW))
            val startY = max(0, min(y - HALFW, source.height - FULLW))

            if (mode == Mode.AVERAGE_ROW || mode == Mode.AVERAGE_COL) {
                repeat(FULLW) { a ->
                    var index = source.indexAt(startX, startY + a)

                    repeat(FULLW) { b ->
                        val p = source.readGrayBT709(index++)
                        if (mode == Mode.AVERAGE_COL) profile[a] += p
                        else profile[b] += p
                    }
                }
            } else {
                var index = source.indexAt(startX, startY + HALFW)

                repeat(FULLW) {
                    profile[it] = source.readGrayBT709(index++)
                }
            }

            val min = profile.min()
            val max = profile.max()
            val mid = (max - min) / 2f + min

            var m = 0
            var n = 0

            for (i in 1 until FULLW) {
                val a = profile[i]
                val b = profile[i - 1]
                if (mid in b..a) m = i
                else if (mid in a..b) n = i
            }

            val c = m - (profile[m] - mid) / (profile[m] - profile[m - 1])
            val d = n - (profile[n - 1] - mid) / (profile[n - 1] - profile[n])

            return d - c
        }
    }
}
