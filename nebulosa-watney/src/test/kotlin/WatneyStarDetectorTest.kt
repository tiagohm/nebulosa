import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import nebulosa.fits.fits
import nebulosa.image.Image
import nebulosa.image.Image.Companion.asImage
import nebulosa.image.algorithms.transformation.Draw
import nebulosa.image.algorithms.transformation.convolution.Mean
import nebulosa.stardetector.StarPoint
import nebulosa.test.M6707HH
import nebulosa.test.NGC3344_COLOR_32_FITS
import nebulosa.test.save
import nebulosa.watney.stardetector.WatneyStarDetector
import org.junit.jupiter.api.Test
import java.awt.Color
import java.awt.Graphics2D
import kotlin.math.roundToInt

class WatneyStarDetectorTest {

    @Test
    fun detectStars() {
        var image = NGC3344_COLOR_32_FITS.fits().asImage()
        var stars = DETECTOR.detect(image.transform(Mean))
        stars shouldHaveSize 1
        image.transform(ImageStarsDraw(stars)).save("color-detected-stars-1")
            .second shouldBe "bb237ce03f7cc9e44e69a5354b7a6fd1"

        image = M6707HH.fits().asImage()
        stars = DETECTOR.detect(image.transform(Mean))
        stars shouldHaveSize 870
        image.transform(ImageStarsDraw(stars)).save("color-detected-stars-870")
            .second shouldBe "004e4d2b4d9725c5367f6865986f6756"
    }

    private data class ImageStarsDraw(private val stars: List<StarPoint>) : Draw() {

        override fun draw(source: Image, graphics: Graphics2D) {
            graphics.color = Color.YELLOW

            for (star in stars) {
                graphics.drawOval(star.x.roundToInt() - 4, star.y.roundToInt() - 4, 8, 8)
            }
        }
    }

    companion object {

        @JvmStatic private val DETECTOR = WatneyStarDetector(computeHFD = true)
    }
}
