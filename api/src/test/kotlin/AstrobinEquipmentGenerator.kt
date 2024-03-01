import com.fasterxml.jackson.databind.ObjectMapper
import nebulosa.astrobin.api.AstrobinService
import nebulosa.astrobin.api.Camera
import nebulosa.astrobin.api.Sensor
import nebulosa.astrobin.api.Telescope
import java.nio.file.Path
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import kotlin.io.path.outputStream
import kotlin.math.max

object AstrobinEquipmentGenerator {

    @JvmStatic private val SENSORS = ConcurrentHashMap<Long, Sensor>()
    @JvmStatic private val CAMERAS = ConcurrentHashMap<Long, Camera>()
    @JvmStatic private val TELESCOPES = ConcurrentHashMap<Long, Telescope>()
    @JvmStatic private val OBJECT_MAPPER = ObjectMapper()
    @JvmStatic private val CAMERA_PATH = Path.of("data", "astrobin", "cameras.json")
    @JvmStatic private val TELESCOPE_PATH = Path.of("data", "astrobin", "telescopes.json")
    @JvmStatic private val EXECUTOR_SERVICE = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())

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

        val task1 = EXECUTOR_SERVICE.submit {
            for (i in 1..99) {
                val sensors = astrobin.sensors(i).execute().body()
                    ?.takeIf { it.results.isNotEmpty() }?.results ?: break

                println("SENSOR: $i")

                sensors.forEach { SENSORS[it.id] = it }
            }
        }

        val task2 = EXECUTOR_SERVICE.submit {
            for (i in 1..99) {
                val cameras = astrobin.cameras(i).execute().body()
                    ?.takeIf { it.results.isNotEmpty() }?.results ?: break

                println("CAMERA: $i")

                cameras.forEach { CAMERAS[it.id] = it }
            }
        }

        val task3 = EXECUTOR_SERVICE.submit {
            for (i in 1..99) {
                val telescopes = astrobin.telescopes(i).execute().body()
                    ?.takeIf { it.results.isNotEmpty() }?.results ?: break

                println("TELESCOPE: $i")

                telescopes.forEach { TELESCOPES[it.id] = it }
            }
        }

        task1.get()
        task2.get()
        task3.get()

        println("CAMERA SIZE: ${CAMERAS.size}")
        println("SENSOR SIZE: ${SENSORS.size}")
        println("TELESCOPE SIZE: ${TELESCOPES.size}")

        val output = HashSet<Any>(max(CAMERAS.size, TELESCOPES.size))

        for ((key, value) in CAMERAS) {
            val sensor = SENSORS[value.sensor] ?: continue

            val name = "%s %s".format(value.brandName, value.name).replace("(color)", "").replace("(mono)", "").trim()
            val sensorName = "%s %s".format(sensor.brandName, sensor.name)
            output.add(CameraEquipment(key, name, sensorName, sensor.pixelWidth, sensor.pixelHeight, sensor.pixelSize))
        }

        CAMERA_PATH.outputStream().use { OBJECT_MAPPER.writeValue(it, output) }
        output.clear()

        for ((key, value) in TELESCOPES) {
            val name = "%s %s".format(value.brandName, value.name)
            output.add(TelescopeEquipment(key, name, value.aperture, value.maxFocalLength))
        }

        TELESCOPE_PATH.outputStream().use { OBJECT_MAPPER.writeValue(it, output) }

        EXECUTOR_SERVICE.shutdownNow()
    }
}
