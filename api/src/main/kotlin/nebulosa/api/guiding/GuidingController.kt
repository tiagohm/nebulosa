package nebulosa.api.guiding

import jakarta.validation.Valid
import org.hibernate.validator.constraints.Range
import org.springframework.web.bind.annotation.*
import kotlin.time.Duration.Companion.seconds

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
    fun status(): GuiderInfo {
        return guidingService.status()
    }

    @GetMapping("history")
    fun history(@RequestParam(required = false, defaultValue = "100") maxLength: Int): List<HistoryStep> {
        return guidingService.history(maxLength)
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

    @PutMapping("settle")
    fun settle(
        @RequestParam(required = false) @Valid @Range(min = 1, max = 25) amount: Double?,
        @RequestParam(required = false) @Valid @Range(min = 1, max = 60) time: Long?,
        @RequestParam(required = false) @Valid @Range(min = 1, max = 60) timeout: Long?,
    ) {
        guidingService.settle(amount, time?.seconds, timeout?.seconds)
    }

    @PutMapping("dither")
    fun dither(
        @RequestParam amount: Double,
        @RequestParam(required = false, defaultValue = "false") raOnly: Boolean,
    ) {
        return guidingService.dither(amount, raOnly)
    }

    @PutMapping("stop")
    fun stop() {
        guidingService.stop()
    }
}
