package nebulosa.api

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jsonMapper
import com.github.rvesse.airline.annotations.Command
import com.github.rvesse.airline.annotations.Option
import io.javalin.Javalin
import io.javalin.json.JavalinJackson
import nebulosa.api.atlas.Location
import nebulosa.api.beans.modules.DeviceModule
import nebulosa.api.inject.*
import nebulosa.json.PathModule
import nebulosa.log.loggerFor
import org.koin.core.context.startKoin
import java.nio.file.Path
import java.time.LocalDate
import java.time.LocalTime

@Command(name = "nebulosa")
class Nebulosa : Runnable, AutoCloseable {

    @Option(name = ["-h", "--host"])
    private var host = "0.0.0.0"

    @Option(name = ["-p", "--port"])
    private var port = 0

    private lateinit var app: Javalin

    override fun run() {
        // Run the server.
        app = Javalin.create { config ->
            config.showJavalinBanner = false
            // JACKSON
            config.jsonMapper(JavalinJackson(OBJECT_MAPPER))
            // VALIDATOR
            config.validation.register(Path::class.java, Path::of)
            config.validation.register(Location::class.java, LocationConverter)
            config.validation.register(LocalTime::class.java, LocalTimeConverter)
            config.validation.register(LocalDate::class.java, LocalDateConverter)
            // CORS
            config.bundledPlugins.enableCors { cors ->
                cors.addRule {
                    it.anyHost()
                    it.exposeHeader("X-Image-Info")
                }
            }
        }.start(host, port)

        koinApp.modules(appModule(app))
        koinApp.modules(objectMapperModule(OBJECT_MAPPER))
        koinApp.modules(servicesModule())
        koinApp.modules(controllersModule())
        startKoin(koinApp)

        LOG.info("server is started at port: {}", app.port())
    }

    override fun close() {
        app.stop()
    }

    private data object LocationConverter : (String) -> Location? {

        override fun invoke(value: String): Location? {
            return if (value.isBlank()) null else OBJECT_MAPPER.readValue(value, Location::class.java)
        }
    }

    private data object LocalTimeConverter : (String) -> LocalTime? {

        override fun invoke(value: String): LocalTime? {
            return if (value.isBlank()) null else LocalTime.parse(value)
        }
    }

    private data object LocalDateConverter : (String) -> LocalDate? {

        override fun invoke(value: String): LocalDate? {
            return if (value.isBlank()) null else LocalDate.parse(value)
        }
    }

    companion object {

        @JvmStatic private val LOG = loggerFor<Nebulosa>()

        @JvmStatic private val OBJECT_MAPPER = jsonMapper {
            addModule(JavaTimeModule())
            addModule(PathModule())
            addModule(DeviceModule())
            disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
        }
    }
}
