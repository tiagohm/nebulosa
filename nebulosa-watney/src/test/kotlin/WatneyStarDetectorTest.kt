import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import nebulosa.imaging.Image
import nebulosa.imaging.algorithms.Draw
import nebulosa.imaging.algorithms.Mean
import nebulosa.star.detection.DetectedStar
import nebulosa.test.FitsStringSpec
import nebulosa.watney.star.detection.WatneyStarDetector
import java.awt.Color
import java.awt.Graphics2D
import kotlin.math.roundToInt

class WatneyStarDetectorTest : FitsStringSpec() {

    init {
        val detector = WatneyStarDetector()

        "detect stars" {
            val image = Image.open(NGC3344_COLOR_32)
            val stars = detector.detect(image.transform(Mean))
            stars shouldHaveSize 1
            image.transform(DetectedStarsDraw(stars)).save("color-detected-stars").second shouldBe "bb237ce03f7cc9e44e69a5354b7a6fd1"
        }
    }

    private data class DetectedStarsDraw(private val stars: List<DetectedStar>) : Draw() {

        override fun draw(source: Image, graphics: Graphics2D) {
            graphics.color = Color.YELLOW

            for ((x, y) in stars) {
                graphics.drawOval(x.roundToInt() - 4, y.roundToInt() - 4, 8, 8)
            }
        }
    }
}
