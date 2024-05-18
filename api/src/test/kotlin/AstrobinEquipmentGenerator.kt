import com.fasterxml.jackson.databind.ObjectMapper
import nebulosa.astrobin.api.AstrobinService
import nebulosa.astrobin.api.Camera
import nebulosa.astrobin.api.Sensor
import nebulosa.astrobin.api.Telescope
import nebulosa.log.loggerFor
import java.nio.file.Path
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import kotlin.io.path.outputStream
import kotlin.math.max

object AstrobinEquipmentGenerator {

    @JvmStatic private val SENSORS = ConcurrentHashMap<Long, Sensor>(1024)
    @JvmStatic private val CAMERAS = ConcurrentHashMap<Long, Camera>(4092)
    @JvmStatic private val TELESCOPES = ConcurrentHashMap<Long, Telescope>(4092)
    @JvmStatic private val OBJECT_MAPPER = ObjectMapper()
    @JvmStatic private val CAMERA_PATH = Path.of("data", "astrobin", "cameras.json")
    @JvmStatic private val TELESCOPE_PATH = Path.of("data", "astrobin", "telescopes.json")
    @JvmStatic private val EXECUTOR_SERVICE = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())
    @JvmStatic private val LOG = loggerFor<AstrobinEquipmentGenerator>()

    data class CameraEquipment(
        val id: Long, val name: String, val sensor: String,
        val width: Int, val height: Int, val pixelSize: Double,
    )

    data class TelescopeEquipment(
        val id: Long, val name: String,
        val aperture: Double, val focalLength: Double,
    )

    @JvmStatic
    fun main(args: Array<String>) {
        val astrobin = AstrobinService()

        val a = EXECUTOR_SERVICE.submit {
            for (page in 1..99) {
                astrobin.sensors(page).execute().body()
                    ?.takeIf { it.results.isNotEmpty() }
                    ?.results
                    ?.forEach { SENSORS[it.id] = it }
                    ?.also { LOG.info("sensor: $page") }
                    ?: break
            }
        }

        val b = EXECUTOR_SERVICE.submit {
            for (page in 1..99) {
                astrobin.cameras(page).execute().body()
                    ?.takeIf { it.results.isNotEmpty() }
                    ?.results
                    ?.forEach { CAMERAS[it.id] = it }
                    ?.also { LOG.info("camera: $page") }
                    ?: break

            }
        }

        val c = EXECUTOR_SERVICE.submit {
            for (page in 1..99) {
                astrobin.telescopes(page).execute().body()
                    ?.takeIf { it.results.isNotEmpty() }
                    ?.results
                    ?.forEach { TELESCOPES[it.id] = it }
                    ?.also { LOG.info("telescope: $page") }
                    ?: break
            }
        }

        a.get()
        b.get()
        c.get()

        LOG.info("cameras: ${CAMERAS.size}")
        LOG.info("sensors: ${SENSORS.size}")
        LOG.info("telescopes: ${TELESCOPES.size}")

        val output = HashSet<Any>(max(CAMERAS.size, TELESCOPES.size))

        for ((key, value) in CAMERAS) {
            if (!value.isValid) continue
            val sensor = SENSORS[value.sensor]?.takeIf { it.isValid } ?: continue

            val name = "%s %s".format(value.brandName, value.name).replace("(color)", "").replace("(mono)", "").trim()
            val sensorName = "%s %s".format(sensor.brandName, sensor.name)
            output.add(CameraEquipment(key, name, sensorName, sensor.pixelWidth, sensor.pixelHeight, sensor.pixelSize))
        }

        CAMERA_PATH.outputStream().use { OBJECT_MAPPER.writeValue(it, output) }
        output.clear()

        for ((key, value) in TELESCOPES) {
            if (!value.isValid) continue
            val name = "%s %s".format(value.brandName, value.name)
            output.add(TelescopeEquipment(key, name, value.aperture, max(value.minFocalLength, value.maxFocalLength)))
        }

        TELESCOPE_PATH.outputStream().use { OBJECT_MAPPER.writeValue(it, output) }

        EXECUTOR_SERVICE.shutdownNow()
    }
}
