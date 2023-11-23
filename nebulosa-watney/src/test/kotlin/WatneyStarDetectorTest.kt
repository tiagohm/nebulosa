import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import nebulosa.imaging.Image
import nebulosa.imaging.algorithms.Draw
import nebulosa.imaging.algorithms.Mean
import nebulosa.star.detection.ImageStar
import nebulosa.test.FitsStringSpec
import nebulosa.watney.star.detection.WatneyStarDetector
import java.awt.Color
import java.awt.Graphics2D
import kotlin.math.roundToInt

class WatneyStarDetectorTest : FitsStringSpec() {

    init {
        val detector = WatneyStarDetector()

        "detect stars" {
            var image = Image.open(NGC3344_COLOR_32)
            var stars = detector.detect(image.transform(Mean))
            stars shouldHaveSize 1
            image.transform(DetectedStarsDraw(stars)).save("color-detected-stars-1").second shouldBe "bb237ce03f7cc9e44e69a5354b7a6fd1"

            image = Image.open(M6707HH)
            stars = detector.detect(image.transform(Mean))
            stars shouldHaveSize 664
            image.transform(DetectedStarsDraw(stars)).save("color-detected-stars-664").second shouldBe "afdba7f121467cced141898bd3b5b8dc"
        }
    }

    private data class DetectedStarsDraw(private val stars: List<ImageStar>) : Draw() {

        override fun draw(source: Image, graphics: Graphics2D) {
            graphics.color = Color.YELLOW

            for (star in stars) {
                graphics.drawOval(star.x.roundToInt() - 4, star.y.roundToInt() - 4, 8, 8)
            }
        }
    }
}
