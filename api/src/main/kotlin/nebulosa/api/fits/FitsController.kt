package nebulosa.api.fits

import jakarta.servlet.http.HttpServletResponse
import nebulosa.fits.FitsImage
import nebulosa.fits.algorithms.ScreenTransformFunction
import nom.tam.fits.Fits
import org.springframework.web.bind.annotation.*
import java.io.File
import java.io.OutputStream
import java.util.*
import javax.imageio.ImageIO

@RestController
@RequestMapping("fits")
class FitsController {

    internal fun open(
        path: String, format: String = "PNG",
        midtone: Float = 0.5f, shadow: Float = 0f, highlight: Float = 1f,
        output: OutputStream,
    ) {
        val file = File(path)
        val fits = Fits(file)
        val image = ScreenTransformFunction(midtone, shadow, highlight).transform(FitsImage(fits))
        ImageIO.write(image, format, output)
    }

    @GetMapping("{path}")
    fun open(
        @PathVariable path: String,
        @RequestParam(required = false) format: String = "PNG",
        @RequestParam(required = false) midtone: Float = 0.5f,
        @RequestParam(required = false) shadow: Float = 0f,
        @RequestParam(required = false) highlight: Float = 1f,
        response: HttpServletResponse,
    ) {
        val decodedPath = Base64.getUrlDecoder().decode(path).decodeToString()
        response.setHeader("Content-Type", "image/${format.lowercase()}")
        open(decodedPath, format, midtone, shadow, highlight, response.outputStream)
        response.flushBuffer()
    }
}
