import io.kotest.core.spec.style.StringSpec
import nebulosa.imaging.Image
import nebulosa.imaging.algorithms.SigmaClip
import nebulosa.imaging.algorithms.star.detection.HFDStarDetector
import nom.tam.fits.Fits

class StarDetectorTest : StringSpec() {

    init {
        "detect stars" {
            val fits = Fits("src/test/resources/M51.16.Mono.fits")
            val image = Image.openFITS(fits)
            HFDStarDetector().detectStars(image)
        }
        "sigma clip" {
            val fits = Fits("src/test/resources/M51.16.Mono.fits")
            val image = Image.openFITS(fits)
            val sigmaClip = SigmaClip().compute(image)
            println(sigmaClip)
        }
    }
}
