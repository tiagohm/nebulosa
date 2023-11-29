import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import nebulosa.imaging.Image
import nebulosa.imaging.algorithms.transformation.Draw
import nebulosa.imaging.algorithms.transformation.convolution.Mean
import nebulosa.star.detection.ImageStar
import nebulosa.test.FitsStringSpec
import nebulosa.watney.star.detection.WatneyStarDetector
import java.awt.Color
import java.awt.Graphics2D

class WatneyStarDetectorTest : FitsStringSpec() {

    init {
        val detector = WatneyStarDetector(computeHFD = true)

        "detect stars" {
            var image = Image.open(NGC3344_COLOR_32)
            var stars = detector.detect(image.transform(Mean))
            stars shouldHaveSize 1
            image.transform(ImageStarsDraw(stars)).save("color-detected-stars-1")
                .second shouldBe "bb237ce03f7cc9e44e69a5354b7a6fd1"

            image = Image.open(M6707HH)
            stars = detector.detect(image.transform(Mean))
            stars shouldHaveSize 870
            image.transform(ImageStarsDraw(stars)).save("color-detected-stars-870")
                .second shouldBe "004e4d2b4d9725c5367f6865986f6756"
        }
    }

    private data class ImageStarsDraw(private val stars: List<ImageStar>) : Draw() {

        override fun draw(source: Image, graphics: Graphics2D) {
            graphics.color = Color.YELLOW

            for (star in stars) {
                graphics.drawOval(star.x - 4, star.y - 4, 8, 8)
            }
        }
    }
}
