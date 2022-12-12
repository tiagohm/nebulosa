package nebulosa.api.fits

import nebulosa.fits.FitsImage
import nebulosa.fits.algorithms.Flip
import nebulosa.fits.algorithms.Invert
import nebulosa.fits.algorithms.ScreenTransformFunction
import nebulosa.fits.algorithms.TransformAlgorithm
import nebulosa.fits.algorithms.TransformAlgorithm.Companion.transform
import nom.tam.fits.Fits
import org.springframework.stereotype.Service
import java.io.File
import java.io.OutputStream
import javax.imageio.ImageIO

@Service
class FitsService {

    fun open(
        path: String, format: String = "PNG",
        midtone: Float = 0.5f, shadow: Float = 0f, highlight: Float = 1f,
        flipH: Boolean = false, flipV: Boolean = false,
        invert: Boolean = false,
        debayer: Boolean = true,
        output: OutputStream,
    ) {
        val file = File(path)
        val fits = Fits(file)
        val algorithms = ArrayList<TransformAlgorithm>(4)
        if (invert) algorithms.add(Invert)
        algorithms.add(ScreenTransformFunction(midtone, shadow, highlight))
        algorithms.add(Flip(horizontal = flipH, vertical = flipV))
        val image = algorithms.transform(FitsImage(fits, debayer))
        ImageIO.write(image, format, output)
    }
}
