package nebulosa.api

import ch.qos.logback.classic.Level
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jsonMapper
import com.github.rvesse.airline.annotations.Command
import com.github.rvesse.airline.annotations.Option
import io.javalin.Javalin
import io.javalin.json.JavalinJackson
import nebulosa.api.beans.modules.DeviceModule
import nebulosa.api.inject.*
import nebulosa.json.PathModule
import nebulosa.log.i
import nebulosa.log.loggerFor
import org.koin.core.context.startKoin
import org.slf4j.LoggerFactory

@Command(name = "nebulosa")
class Nebulosa : Runnable, AutoCloseable {

    @Option(name = ["-h", "--host"])
    private var host = "0.0.0.0"

    @Option(name = ["-p", "--port"])
    private var port = 0

    @Option(name = ["-d", "--debug"])
    private var debug = false

    private lateinit var app: Javalin

    override fun run() {
        if (debug) {
            with(LoggerFactory.getLogger("nebulosa") as ch.qos.logback.classic.Logger) {
                level = Level.DEBUG
            }
        }

        // Run the server.
        app = Javalin.create { config ->
            config.showJavalinBanner = false
            // JACKSON
            config.jsonMapper(JavalinJackson(OBJECT_MAPPER))
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

        LOG.i("server is started at port: {}", app.port())
    }

    override fun close() {
        app.stop()
    }

    companion object {

        @JvmStatic internal val LOG = loggerFor<Nebulosa>()

        @JvmStatic private val OBJECT_MAPPER = jsonMapper {
            addModule(JavaTimeModule())
            addModule(PathModule())
            addModule(DeviceModule())
            disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
            disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        }
    }
}
