package nebulosa.api.controllers

import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import nebulosa.api.data.entities.CameraPreferenceEntity
import nebulosa.api.data.entities.SavedCameraImageEntity
import nebulosa.api.data.enums.EventType
import nebulosa.api.data.events.CameraCaptureFinished
import nebulosa.api.data.requests.CameraStartCaptureRequest
import nebulosa.api.data.responses.CameraResponse
import nebulosa.api.repositories.CameraPreferenceRepository
import nebulosa.api.services.CameraService
import nebulosa.indi.device.PropertyChangedEvent
import nebulosa.indi.device.camera.CameraAttached
import nebulosa.indi.device.camera.CameraDetached
import nebulosa.indi.device.camera.CameraEvent
import nebulosa.log.loggerFor
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter.event
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

@RestController
class CameraController(
    private val cameraService: CameraService,
    private val cameraPreferenceRepository: CameraPreferenceRepository,
    private val eventBus: EventBus,
) {

    private val eventId = AtomicInteger(1)
    private val eventEmitters = Collections.synchronizedList(ArrayList<SseEmitter>())

    @Subscribe(threadMode = ThreadMode.ASYNC)
    fun onSavedCameraImageEvent(event: SavedCameraImageEntity) {
        sendEvent(EventType.CAMERA_IMAGE_SAVED, event)
    }

    private fun sendEvent(type: EventType, data: Any) {
        val event = event().id(eventId.getAndIncrement().toString()).name(type.name).data(data)
        eventEmitters.forEach { it.send(event) }
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    fun onCameraEvent(event: CameraEvent) {
        when (event) {
            is PropertyChangedEvent -> sendEvent(EventType.CAMERA_UPDATED, CameraResponse(event.device!!))
            is CameraCaptureFinished -> sendEvent(EventType.CAMERA_CAPTURE_FINISHED, CameraResponse(event.device))
            is CameraAttached -> sendEvent(EventType.CAMERA_ATTACHED, CameraResponse(event.device))
            is CameraDetached -> sendEvent(EventType.CAMERA_DETACHED, CameraResponse(event.device))
        }
    }

    @PostConstruct
    private fun initialize() {
        eventBus.register(this)
    }

    @PreDestroy
    private fun destroy() {
        eventBus.unregister(this)

        eventEmitters.forEach(SseEmitter::complete)
        eventEmitters.clear()
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
    fun events(request: HttpServletRequest): SseEmitter {
        LOG.info("SSE client connected. host={}, port={}", request.remoteHost, request.remotePort)

        val eventEmitter = SseEmitter(-1)

        eventEmitter.onCompletion {
            LOG.info("SSE client disconnected. host={}, port={}", request.remoteHost, request.remotePort)
            eventEmitters.remove(eventEmitter)
        }

        eventEmitters.add(eventEmitter)

        return eventEmitter
    }

    companion object {

        @JvmStatic private val LOG = loggerFor<CameraController>()
    }
}
