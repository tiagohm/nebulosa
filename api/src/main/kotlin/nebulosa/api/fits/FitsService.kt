package nebulosa.api.fits

import nebulosa.fits.FitsImage
import nebulosa.fits.algorithms.ScreenTransformFunction
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
        output: OutputStream,
    ) {
        val file = File(path)
        val fits = Fits(file)
        val image = ScreenTransformFunction(midtone, shadow, highlight).transform(FitsImage(fits))
        ImageIO.write(image, format, output)
    }
}
