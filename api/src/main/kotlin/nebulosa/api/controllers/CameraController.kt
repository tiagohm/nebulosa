package nebulosa.api.controllers

import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import nebulosa.api.data.entities.CameraPreferenceEntity
import nebulosa.api.data.entities.SavedCameraImageEntity
import nebulosa.api.data.events.CameraCaptureFinished
import nebulosa.api.data.requests.CameraStartCaptureRequest
import nebulosa.api.data.responses.CameraResponse
import nebulosa.api.repositories.CameraPreferenceRepository
import nebulosa.api.services.CameraService
import nebulosa.api.services.EventEmitterService
import nebulosa.indi.device.PropertyChangedEvent
import nebulosa.indi.device.camera.CameraAttached
import nebulosa.indi.device.camera.CameraDetached
import nebulosa.indi.device.camera.CameraEvent
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.util.*

@RestController
class CameraController(
    private val cameraService: CameraService,
    private val cameraPreferenceRepository: CameraPreferenceRepository,
    private val eventBus: EventBus,
    private val eventEmitterService: EventEmitterService,
) {

    @Subscribe(threadMode = ThreadMode.ASYNC)
    fun onSavedCameraImageEvent(event: SavedCameraImageEntity) {
        eventEmitterService.sendEvent("CAMERA", "CAMERA_IMAGE_SAVED", event)
    }

    private fun sendEvent(event: CameraEvent, type: String) {
        eventEmitterService.sendEvent("CAMERA", type, CameraResponse(event.device!!))
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    fun onCameraEvent(event: CameraEvent) {
        when (event) {
            is PropertyChangedEvent -> sendEvent(event, "CAMERA_UPDATED")
            is CameraCaptureFinished -> sendEvent(event, "CAMERA_CAPTURE_FINISHED")
            is CameraAttached -> sendEvent(event, "CAMERA_ATTACHED")
            is CameraDetached -> sendEvent(event, "CAMERA_DETACHED")
        }
    }

    @PostConstruct
    private fun initialize() {
        eventBus.register(this)
    }

    @PreDestroy
    private fun destroy() {
        eventBus.unregister(this)
    }

    @GetMapping("attachedCameras")
    fun attachedCameras(): List<CameraResponse> {
        return cameraService.attachedCameras()
    }

    @GetMapping("camera")
    fun camera(@RequestParam name: String): CameraResponse {
        return cameraService[name]
    }

    @PostMapping("cameraConnect")
    fun connect(@RequestParam name: String) {
        cameraService.connect(name)
    }

    @PostMapping("cameraDisconnect")
    fun disconnect(@RequestParam name: String) {
        cameraService.disconnect(name)
    }

    @GetMapping("cameraIsCapturing")
    fun isCapturing(@RequestParam name: String): Boolean {
        return cameraService.isCapturing(name)
    }

    @PostMapping("cameraSetpointTemperature")
    fun setpointTemperature(@RequestParam name: String, @RequestParam temperature: Double) {
        cameraService.setpointTemperature(name, temperature)
    }

    @PostMapping("cameraCooler")
    fun cooler(@RequestParam name: String, @RequestParam value: Boolean) {
        cameraService.cooler(name, value)
    }

    @PostMapping("cameraStartCapture")
    fun startCapture(@RequestParam name: String, @RequestBody @Valid body: CameraStartCaptureRequest) {
        cameraService.startCapture(name, body)
    }

    @PostMapping("cameraAbortCapture")
    fun abortCapture(@RequestParam name: String) {
        cameraService.abortCapture(name)
    }

    @PutMapping("cameraPreferences")
    fun savePreferences(@RequestParam name: String, @RequestBody @Valid body: CameraPreferenceEntity) {
        val preference = cameraPreferenceRepository.withName(body.name)
        cameraPreferenceRepository.save(body.copy(id = preference?.id ?: 0L, name = name))
    }

    @GetMapping("cameraPreferences")
    fun loadPreferences(@RequestParam name: String): CameraPreferenceEntity {
        return cameraPreferenceRepository.withName(name) ?: CameraPreferenceEntity(name = name)
    }

    @GetMapping("cameraEvents")
    fun events(): SseEmitter {
        return eventEmitterService.register("CAMERA")
    }
}
