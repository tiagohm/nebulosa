package nebulosa.api.fits

import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("fits")
class FitsController {

    @Autowired
    private lateinit var fitsService: FitsService

    @GetMapping("{path}")
    fun open(
        @PathVariable path: String,
        @RequestParam(required = false, defaultValue = "PNG") format: String = "PNG",
        @RequestParam(required = false, defaultValue = "32767") midtone: Long = 32767L,
        @RequestParam(required = false, defaultValue = "0") shadow: Long = 0L,
        @RequestParam(required = false, defaultValue = "65535") highlight: Long = 65535L,
        @RequestParam(required = false, defaultValue = "false") flipH: Boolean = false,
        @RequestParam(required = false, defaultValue = "false") flipV: Boolean = false,
        @RequestParam(required = false, defaultValue = "false") invert: Boolean = false,
        @RequestParam(required = false, defaultValue = "true") debayer: Boolean = true,
        response: HttpServletResponse,
    ) {
        val decodedPath = Base64.getUrlDecoder().decode(path).decodeToString()
        response.setHeader("Content-Type", "image/${format.lowercase()}")
        fitsService.open(
            decodedPath, format,
            midtone / 65535f, shadow / 65535f, highlight / 65535f,
            flipH, flipV, invert, debayer,
            response.outputStream,
        )
        response.flushBuffer()
    }
}
