package nebulosa.desktop.gui.guider

import javafx.scene.canvas.Canvas
import javafx.scene.paint.Color
import nebulosa.guiding.StarPoint
import nebulosa.imaging.Image
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

class StarProfileGraph : Canvas() {

    // private val horProfile = FloatArray(21) // Avg row.
    // private val verProfile = FloatArray(21) // Avg col.
    private val midRowProfile = FloatArray(21) // Mid row.

    override fun isResizable() = true

    override fun maxHeight(width: Double) = Double.POSITIVE_INFINITY

    override fun maxWidth(height: Double) = Double.POSITIVE_INFINITY

    override fun minWidth(height: Double) = 1.0

    override fun minHeight(width: Double) = 1.0

    override fun prefWidth(height: Double) = width

    override fun prefHeight(width: Double) = height

    override fun resize(width: Double, height: Double) {
        this.width = width
        this.height = height
    }

    fun draw(image: Image, primaryStar: StarPoint): Float {
        val xStart = max(0, min(primaryStar.x.roundToInt() - 10, image.width - 22))
        val yStart = max(0, min(primaryStar.y.roundToInt() - 10, image.height - 22))

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

        var midRowIdx = image.indexAt(xStart, yStart + 10)

        for (b in 0..20) {
            midRowProfile[b] = image.readGray(midRowIdx++)
        }

        val g = graphicsContext2D
        val profile = midRowProfile

        g.clearRect(0.0, 0.0, width, height)

        val min = profile.min()
        val max = profile.max()
        val range = max - min
        val mid = range / 2f + min

        val xStep = width / 20

        g.stroke = Color.RED

        for (i in 1..20) {
            val x = i * xStep
            val y = height - height * (profile[i] - min) / range + max
            val prevY = height - height * (profile[i - 1] - min) / range + max
            g.strokeLine(x - xStep, prevY, x, y)
        }

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
}
