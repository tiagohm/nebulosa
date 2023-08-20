package nebulosa.guiding.internal

import nebulosa.guiding.StarPoint
import nebulosa.imaging.Image
import nebulosa.imaging.algorithms.ComputationAlgorithm
import nebulosa.imaging.algorithms.Draw
import java.awt.Color
import java.awt.Graphics2D
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

data class FWHM(
    private val primaryStar: StarPoint,
) : ComputationAlgorithm<Float>, Draw() {

    // private val horProfile = FloatArray(21) // Avg row.
    // private val verProfile = FloatArray(21) // Avg col.
    private val midRowProfile = FloatArray(21) // Mid row.

    override fun compute(source: Image): Float {
        val xStart = max(0, min(primaryStar.x.roundToInt() - 10, source.width - 22))
        val yStart = max(0, min(primaryStar.y.roundToInt() - 10, source.height - 22))

        // horProfile.fill(0f)
        // verProfile.fill(0f)

        // for (a in 0..20) {
        //     val y = yStart + a

        //     for (b in 0..20) {
        //         val x = xStart + b
        //         val p = image.readGray(x, y)
        //         horProfile[b] += p
        //         verProfile[a] += p
        //     }
        // }

        var midRowIdx = source.indexAt(xStart, yStart + 10)

        val profile = midRowProfile

        for (b in 0..20) {
            profile[b] = source.readGray(midRowIdx++)
        }

        val min = profile.min()
        val max = profile.max()
        val range = max - min
        val mid = range / 2f + min

        var m = 0
        var n = 0

        for (i in 1..20) {
            val a = profile[i]
            val b = profile[i - 1]
            if (mid in b..a) m = i
            else if (mid in a..b) n = i
        }

        val c = m - (profile[m] - mid) / (profile[m] - profile[m - 1])
        val d = n - (profile[n - 1] - mid) / (profile[n - 1] - profile[n])

        return d - c
    }

    override fun draw(source: Image, graphics: Graphics2D) {
        val profile = midRowProfile

        val min = profile.min()
        val max = profile.max()
        val range = max - min

        val xStep = source.width / 20f

        graphics.color = Color.RED

        val height = source.height

        for (i in 1..20) {
            val x = i * xStep
            val y = height - height * (profile[i] - min) / range + max
            val prevY = height - height * (profile[i - 1] - min) / range + max
            graphics.drawLine((x - xStep).toInt(), prevY.toInt(), x.toInt(), y.toInt())
        }
    }
}
