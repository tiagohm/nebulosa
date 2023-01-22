import io.kotest.core.spec.style.StringSpec
import nebulosa.guiding.*
import nebulosa.imaging.FitsImage
import nom.tam.fits.Fits

class MultiStarGuiderTest : StringSpec(), GuiderListener {

    init {
        val fits1 = FitsImage(Fits("src/test/resources/1.fits"))

        "select star" {
            val guider = MultiStarGuider()
            guider.registerListener(this@MultiStarGuiderTest)
            guider.selectGuideStar(fits1, 542f, 974f)
        }
    }

    override fun onLockPositionChanged(guider: Guider, position: Point) {
        println("onLockPositionChanged: position: $position")
    }

    override fun onStarSelected(guider: Guider, star: Star) {
        println("onStarSelected: star: $star")
    }

    override fun onGuidingDithered(guider: Guider, dx: Float, dy: Float, mountCoordinate: Boolean) {
        println("onGuidingDithered: dx: $dx, dy: $dy, mountCoordinate: $mountCoordinate")
    }
}
