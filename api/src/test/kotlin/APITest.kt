import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.kotlinModule
import io.kotest.core.annotation.EnabledIf
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.booleans.shouldBeTrue
import kotlinx.coroutines.delay
import nebulosa.api.autofocus.AutoFocusRequest
import nebulosa.api.beans.converters.time.DurationSerializer
import nebulosa.api.cameras.CameraStartCaptureRequest
import nebulosa.api.connection.ConnectionType
import nebulosa.api.stardetector.StarDetectionRequest
import nebulosa.common.json.PathSerializer
import nebulosa.test.AbstractFitsAndXisfTest.Companion.HTTP_CLIENT
import nebulosa.test.NonGitHubOnlyCondition
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.nio.file.Path
import java.time.Duration

@EnabledIf(NonGitHubOnlyCondition::class)
class APITest : StringSpec() {

    init {
        // GENERAL.

        "Connect" { connect() }
        "Disconnect" { disconnect() }

        // CAMERA.

        "Cameras" { cameras() }
        "Camera Connect" { cameraConnect() }
        "Camera" { camera() }
        "Camera Capture Start" { cameraStartCapture() }
        "Camera Capture Stop" { cameraStopCapture() }
        "Camera Disconnect" { cameraDisconnect() }

        // MOUNT.

        "Mounts" { mounts() }
        "Mount Connect" { mountConnect() }
        "Mount" { mount() }
        "Mount Remote Control Start" { mountRemoteControlStart() }
        "Mount Remote Control List" { mountRemoteControlList() }
        "Mount Remote Control Stop" { mountRemoteControlStop() }
        "Mount Disconnect" { mountDisconnect() }

        // FOCUSER.

        "Focusers" { focusers() }
        "Focuser Connect" { focuserConnect() }
        "Focuser" { focuser() }
        "Focuser Disconnect" { focuserDisconnect() }

        // AUTO FOCUS.

        "Auto Focus Start" {
            connect("192.168.31.153", 11111, ConnectionType.ALPACA)
            delay(1000)
            cameraConnect()
            focuserConnect()
            delay(1000)
            // focuserMoveTo(position = 8100)
            delay(2000)
            autoFocusStart()
        }
        "Auto Focus Stop" { autoFocusStop() }
    }

    private fun connect(host: String = "0.0.0.0", port: Int = 7624, type: ConnectionType = ConnectionType.INDI) {
        put("connection?host=$host&port=$port&type=$type")
    }

    private fun disconnect() {
        delete("connection")
    }

    private fun cameras() {
        get("cameras")
    }

    private fun cameraConnect(camera: String = CAMERA_NAME) {
        put("cameras/$camera/connect")
    }

    private fun cameraDisconnect(camera: String = CAMERA_NAME) {
        put("cameras/$camera/disconnect")
    }

    private fun camera(camera: String = CAMERA_NAME) {
        get("cameras/$camera")
    }

    private fun cameraStartCapture(camera: String = CAMERA_NAME) {
        putJson("cameras/$camera/capture/start", CAMERA_START_CAPTURE_REQUEST)
    }

    private fun cameraStopCapture(camera: String = CAMERA_NAME) {
        put("cameras/$camera/capture/abort")
    }

    private fun mounts() {
        get("mounts")
    }

    private fun mountConnect(mount: String = MOUNT_NAME) {
        put("mounts/$mount/connect")
    }

    private fun mountDisconnect(mount: String = MOUNT_NAME) {
        put("mounts/$mount/disconnect")
    }

    private fun mount(mount: String = MOUNT_NAME) {
        get("mounts/$mount")
    }

    private fun mountRemoteControlStart(mount: String = MOUNT_NAME, host: String = "0.0.0.0", port: Int = 10001) {
        put("mounts/$mount/remote-control/start?type=LX200&host=$host&port=$port")
    }

    private fun mountRemoteControlList(mount: String = MOUNT_NAME) {
        get("mounts/$mount/remote-control")
    }

    private fun mountRemoteControlStop(mount: String = MOUNT_NAME) {
        put("mounts/$mount/remote-control/stop?type=LX200")
    }

    private fun focusers() {
        get("focusers")
    }

    private fun focuserConnect(focuser: String = FOCUSER_NAME) {
        put("focusers/$focuser/connect")
    }

    private fun focuserDisconnect(focuser: String = FOCUSER_NAME) {
        put("focusers/$focuser/disconnect")
    }

    private fun focuser(focuser: String = FOCUSER_NAME) {
        get("focusers/$focuser")
    }

    private fun focuserMoveTo(focuser: String = FOCUSER_NAME, position: Int) {
        put("focusers/$focuser/move-to?steps=$position")
    }

    private fun autoFocusStart(camera: String = CAMERA_NAME, focuser: String = FOCUSER_NAME) {
        putJson("auto-focus/$camera/$focuser/start", AUTO_FOCUS_REQUEST)
    }

    private fun autoFocusStop(camera: String = CAMERA_NAME) {
        put("auto-focus/$camera/stop")
    }

    companion object {

        private const val BASE_URL = "http://localhost:7000"
        private const val CAMERA_NAME = "Sky Simulator"
        private const val MOUNT_NAME = "Telescope Simulator"
        private const val FOCUSER_NAME = "ZWO Focuser (1)"

        @JvmStatic private val EXPOSURE_TIME = Duration.ofSeconds(5)
        @JvmStatic private val CAPTURES_PATH = Path.of(System.getProperty("user.home"), "/Git/nebulosa/data/captures")

        @JvmStatic private val STAR_DETECTION_OPTIONS = StarDetectionRequest(executablePath = Path.of("astap"))

        @JvmStatic private val CAMERA_START_CAPTURE_REQUEST = CameraStartCaptureRequest(
            exposureTime = EXPOSURE_TIME, width = 1280, height = 1024, frameFormat = "INDI_MONO",
            savePath = CAPTURES_PATH, exposureAmount = 1
        )

        @JvmStatic private val AUTO_FOCUS_REQUEST = AutoFocusRequest(
            capture = CAMERA_START_CAPTURE_REQUEST, stepSize = 500,
            starDetector = STAR_DETECTION_OPTIONS
        )

        @JvmStatic private val KOTLIN_MODULE = kotlinModule()
            .addSerializer(PathSerializer)
            .addSerializer(DurationSerializer())

        @JvmStatic private val OBJECT_MAPPER = ObjectMapper()
            .registerModule(JavaTimeModule())
            .registerModule(KOTLIN_MODULE)

        @JvmStatic private val APPLICATION_JSON = "application/json".toMediaType()
        @JvmStatic private val EMPTY_BODY = ByteArray(0).toRequestBody(APPLICATION_JSON)

        @JvmStatic
        private fun get(path: String) {
            val request = Request.Builder().get().url("$BASE_URL/$path").build()
            HTTP_CLIENT.newCall(request).execute().use { it.isSuccessful.shouldBeTrue() }
        }

        @JvmStatic
        private fun put(path: String, body: RequestBody = EMPTY_BODY) {
            val request = Request.Builder().put(body).url("$BASE_URL/$path").build()
            HTTP_CLIENT.newCall(request).execute().use { it.isSuccessful.shouldBeTrue() }
        }

        @JvmStatic
        private fun putJson(path: String, data: Any) {
            val bytes = OBJECT_MAPPER.writeValueAsBytes(data)
            val body = bytes.toRequestBody(APPLICATION_JSON)
            put(path, body)
        }

        @JvmStatic
        private fun delete(path: String) {
            val request = Request.Builder().delete().url("$BASE_URL/$path").build()
            HTTP_CLIENT.newCall(request).execute().use { it.isSuccessful.shouldBeTrue() }
        }
    }
}
