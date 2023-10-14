package nebulosa.api.guiding

import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("guiding")
class GuidingController(private val guidingService: GuidingService) {

    @PutMapping("connect")
    fun connect(
        @RequestParam(required = false, defaultValue = "localhost") host: String,
        @RequestParam(required = false, defaultValue = "4400") port: Int,
    ) {
        guidingService.connect(host, port)
    }

    @DeleteMapping("disconnect")
    fun disconnect() {
        guidingService.disconnect()
    }

    @GetMapping("status")
    fun status(): GuiderStatus {
        return guidingService.status()
    }

    @GetMapping("history")
    fun history(): List<HistoryStep> {
        return guidingService.history()
    }

    @GetMapping("history/latest")
    fun latestHistory(): HistoryStep? {
        return guidingService.latestHistory()
    }

    @PutMapping("history/clear")
    fun clearHistory() {
        return guidingService.clearHistory()
    }

    @PutMapping("loop")
    fun loop(@RequestParam(required = false, defaultValue = "true") autoSelectGuideStar: Boolean) {
        guidingService.loop(autoSelectGuideStar)
    }

    @PutMapping("start")
    fun start(@RequestParam(required = false, defaultValue = "false") forceCalibration: Boolean) {
        guidingService.start(forceCalibration)
    }

    @PutMapping("dither")
    fun dither(
        @RequestParam pixels: Double,
        @RequestParam(required = false, defaultValue = "false") raOnly: Boolean,
    ) {
        return guidingService.dither(pixels, raOnly)
    }

    @PutMapping("stop")
    fun stop() {
        guidingService.stop()
    }
}
