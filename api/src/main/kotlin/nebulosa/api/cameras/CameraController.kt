package nebulosa.api.cameras

import jakarta.validation.Valid
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.PositiveOrZero
import nebulosa.api.connection.ConnectionService
import nebulosa.api.data.enums.AutoSubFolderMode
import nebulosa.indi.device.camera.Camera
import nebulosa.indi.device.camera.FrameType
import org.hibernate.validator.constraints.Range
import org.springframework.web.bind.annotation.*
import java.nio.file.Path
import kotlin.time.Duration.Companion.microseconds
import kotlin.time.Duration.Companion.seconds

@RestController
@RequestMapping("cameras")
class CameraController(
    private val connectionService: ConnectionService,
    private val cameraService: CameraService,
) {

    @GetMapping
    fun cameras(): List<Camera> {
        return connectionService.cameras()
    }

    @GetMapping("{cameraName}")
    fun camera(@PathVariable cameraName: String): Camera {
        return requireNotNull(connectionService.camera(cameraName))
    }

    @PutMapping("{cameraName}/connect")
    fun connect(@PathVariable cameraName: String) {
        cameraService.connect(camera(cameraName))
    }

    @PutMapping("{cameraName}/disconnect")
    fun disconnect(@PathVariable cameraName: String) {
        cameraService.disconnect(camera(cameraName))
    }

    @GetMapping("{cameraName}/capturing")
    fun isCapturing(@PathVariable cameraName: String): Boolean {
        return cameraService.isCapturing(camera(cameraName))
    }

    @PutMapping("{cameraName}/cooler")
    fun cooler(
        @PathVariable cameraName: String,
        @RequestParam enabled: Boolean,
    ) {
        cameraService.cooler(camera(cameraName), enabled)
    }

    @PutMapping("{cameraName}/temperature/setpoint")
    fun setpointTemperature(
        @PathVariable cameraName: String,
        @RequestParam @Valid @Range(min = -50, max = 50) temperature: Double,
    ) {
        cameraService.setpointTemperature(camera(cameraName), temperature)
    }

    @PutMapping("{cameraName}/capture/start")
    fun startCapture(
        @PathVariable cameraName: String,
        @RequestParam @Valid @Positive exposureInMicroseconds: Long,
        @RequestParam(required = false, defaultValue = "1") @Valid @Range(min = 0L, max = 1000L) exposureAmount: Int, // 0 = looping
        @RequestParam(required = false, defaultValue = "0") @Valid @Range(min = 0L, max = 60L) exposureDelayInSeconds: Long,
        @RequestParam(required = false) @Valid @PositiveOrZero x: Int?,
        @RequestParam(required = false) @Valid @PositiveOrZero y: Int?,
        @RequestParam(required = false) @Valid @PositiveOrZero width: Int?,
        @RequestParam(required = false) @Valid @PositiveOrZero height: Int?,
        @RequestParam(required = false, defaultValue = "") frameFormat: String?,
        @RequestParam(required = false, defaultValue = "LIGHT") frameType: FrameType,
        @RequestParam(required = false) @Valid @Positive binX: Int?,
        @RequestParam(required = false) @Valid @Positive binY: Int?,
        @RequestParam(required = false) @Valid @PositiveOrZero gain: Int?,
        @RequestParam(required = false) @Valid @PositiveOrZero offset: Int?,
        @RequestParam(required = false, defaultValue = "false") autoSave: Boolean,
        @RequestParam(required = false) savePath: Path?,
        @RequestParam(required = false, defaultValue = "OFF") autoSubFolderMode: AutoSubFolderMode,
    ) {
        val camera = camera(cameraName)
        cameraService.startCapture(
            camera,
            exposureInMicroseconds.microseconds, exposureAmount, exposureDelayInSeconds.seconds,
            x ?: camera.x, y ?: camera.y, width ?: camera.width, height ?: camera.height,
            frameFormat, frameType, binX ?: camera.binX, binY ?: camera.binY,
            gain ?: camera.gain, offset ?: camera.offset, autoSave, savePath, autoSubFolderMode,
        )
    }

    @PutMapping("{cameraName}/capture/abort")
    fun abortCapture(@PathVariable cameraName: String) {
        cameraService.abortCapture(camera(cameraName))
    }
}
