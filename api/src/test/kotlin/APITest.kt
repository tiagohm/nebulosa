import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.kotlinModule
import io.kotest.core.annotation.EnabledIf
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.booleans.shouldBeTrue
import nebulosa.api.cameras.CameraStartCaptureRequest
import nebulosa.common.json.PathSerializer
import nebulosa.test.NonGitHubOnlyCondition
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import java.nio.file.Path
import java.time.Duration

@EnabledIf(NonGitHubOnlyCondition::class)
class APITest : StringSpec() {

    init {
        "Connect" { put("connection?host=localhost&port=7624") }
        "Cameras" { get("cameras") }
        "Camera Connect" { put("cameras/$CAMERA_NAME/connect") }
        "Camera" { get("cameras/$CAMERA_NAME") }
        "Camera Capture Start" { putJson("cameras/$CAMERA_NAME/capture/start", CAMERA_START_CAPTURE_REQUEST) }
        "Camera Capture Stop" { put("cameras/$CAMERA_NAME/capture/abort") }
        "Camera Disconnect" { put("cameras/$CAMERA_NAME/disconnect") }
        "Disconnect" { delete("connection") }
    }

    companion object {

        private const val BASE_URL = "http://localhost:7000"
        private const val CAMERA_NAME = "CCD Simulator"

        @JvmStatic private val EXPOSURE_TIME = Duration.ofSeconds(5)
        @JvmStatic private val CAPTURES_PATH = Path.of("/home/tiagohm/Git/nebulosa/data/captures")
        @JvmStatic private val CAMERA_START_CAPTURE_REQUEST =
            CameraStartCaptureRequest(exposureTime = EXPOSURE_TIME, width = 1280, height = 1024, frameFormat = "INDI_MONO", savePath = CAPTURES_PATH)
                .copy(exposureAmount = 2)

        @JvmStatic private val CLIENT = OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
            .build()

        @JvmStatic private val KOTLIN_MODULE = kotlinModule().addSerializer(PathSerializer)

        @JvmStatic private val OBJECT_MAPPER = ObjectMapper()
            .registerModule(JavaTimeModule())
            .registerModule(KOTLIN_MODULE)

        @JvmStatic private val APPLICATION_JSON = "application/json".toMediaType()
        @JvmStatic private val EMPTY_BODY = ByteArray(0).toRequestBody(APPLICATION_JSON)

        @JvmStatic
        private fun get(path: String) {
            val request = Request.Builder().get().url("$BASE_URL/$path").build()
            CLIENT.newCall(request).execute().use { it.isSuccessful.shouldBeTrue() }
        }

        @JvmStatic
        private fun put(path: String, body: RequestBody = EMPTY_BODY) {
            val request = Request.Builder().put(body).url("$BASE_URL/$path").build()
            CLIENT.newCall(request).execute().use { it.isSuccessful.shouldBeTrue() }
        }

        @JvmStatic
        private fun putJson(path: String, data: Any) {
            val bytes = OBJECT_MAPPER.writeValueAsBytes(data)
            val body = bytes.toRequestBody(APPLICATION_JSON)
            val request = Request.Builder().put(body).url("$BASE_URL/$path").build()
            CLIENT.newCall(request).execute().use { it.isSuccessful.shouldBeTrue() }
        }

        @JvmStatic
        private fun delete(path: String) {
            val request = Request.Builder().delete().url("$BASE_URL/$path").build()
            CLIENT.newCall(request).execute().use { it.isSuccessful.shouldBeTrue() }
        }
    }
}
