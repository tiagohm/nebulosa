package nebulosa.api.atlas

import nebulosa.math.Angle.Companion.deg
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime

@RestController
@RequestMapping("atlas")
class AtlasController {

    @Autowired
    private lateinit var atlasService: AtlasService

    @GetMapping("sun")
    fun sun(
        @RequestParam time: LocalDateTime,
        @RequestParam(required = false, defaultValue = "0.0") longitude: Double = 0.0,
        @RequestParam(required = false, defaultValue = "0.0") latitude: Double = 0.0,
        @RequestParam(required = false, defaultValue = "0.0") altitude: Double = 0.0,
    ) = atlasService.sun(time, longitude.deg, latitude.deg)

    @GetMapping("moon")
    fun moon(
        @RequestParam time: LocalDateTime,
        @RequestParam(required = false, defaultValue = "0.0") longitude: Double = 0.0,
        @RequestParam(required = false, defaultValue = "0.0") latitude: Double = 0.0,
        @RequestParam(required = false, defaultValue = "0.0") altitude: Double = 0.0,
    ) = atlasService.moon(time, longitude.deg, latitude.deg)
}
