package nebulosa.api

import ch.qos.logback.classic.Level
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jsonMapper
import com.github.rvesse.airline.annotations.Command
import com.github.rvesse.airline.annotations.Option
import io.javalin.Javalin
import io.javalin.http.Context
import io.javalin.http.HttpStatus.BAD_REQUEST
import io.javalin.json.JavalinJackson
import nebulosa.api.converters.DeviceModule
import nebulosa.api.core.FileLocker
import nebulosa.api.database.migration.MainDatabaseMigrator
import nebulosa.api.database.migration.SkyDatabaseMigrator
import nebulosa.api.http.responses.ApiMessageResponse
import nebulosa.api.inject.*
import nebulosa.json.PathModule
import nebulosa.log.di
import nebulosa.log.loggerFor
import org.koin.core.context.startKoin
import org.slf4j.LoggerFactory
import java.net.ConnectException
import java.net.HttpURLConnection
import java.net.URL
import java.nio.file.Path
import java.util.*
import java.util.concurrent.ExecutionException
import java.util.concurrent.ExecutorService
import kotlin.io.path.Path
import kotlin.io.path.exists
import kotlin.io.path.fileSize
import kotlin.io.path.inputStream
import kotlin.io.path.isRegularFile
import kotlin.system.exitProcess

@Command(name = "nebulosa")
class Nebulosa : Runnable, AutoCloseable {

    private val properties = Properties(3)

    init {
        Path(System.getProperty(APP_DIR_KEY), PROPERTIES_FILENAME)
            .takeIf { it.exists() && it.isRegularFile() }
            ?.also { it.inputStream().use(properties::load) }
    }

    @Option(name = ["-h", "--host"])
    private var host = properties.getProperty("host")?.ifBlank { null } ?: DEFAULT_HOST

    @Option(name = ["-p", "--port"])
    private var port = properties.getProperty("port")?.ifBlank { null }?.toIntOrNull() ?: DEFAULT_PORT

    @Option(name = ["-d", "--debug"])
    private var debug = properties.getProperty("debug")?.toBoolean() == true

    @Option(name = ["-f", "--files"])
    private val files = mutableListOf<String>()

    private lateinit var app: Javalin

    override fun run() {
        if (debug) {
            with(LoggerFactory.getLogger("nebulosa") as ch.qos.logback.classic.Logger) {
                level = Level.DEBUG
            }
        }

        // is running simultaneously!
        if (!FileLocker.tryLock()) {
            try {
                if (files.map(Path::of)
                        .filter { it.exists() && it.isRegularFile() && it.fileSize() > 0L }
                        .takeIf { it.isNotEmpty() }
                        ?.let(::requestToOpenImagesOnDesktop) == true
                ) exitProcess(1)
            } catch (e: Throwable) {
                LOG.error("failed to request to open images on desktop", e)
            }

            exitProcess(129)
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

        app.exception(Exception::class.java, ::handleException)

        koinApp.modules(appModule(app))
        koinApp.modules(objectMapperModule(OBJECT_MAPPER))
        koinApp.modules(servicesModule())
        koinApp.modules(controllersModule())
        startKoin(koinApp)

        with(app.port()) {
            println("server is started at port: $this")
            FileLocker.write("$this")
        }

        with(koinApp.koin) {
            val executor = get<ExecutorService>()
            executor.submit(get<MainDatabaseMigrator>())
            executor.submit(get<SkyDatabaseMigrator>())
        }
    }

    private fun handleException(ex: Exception, ctx: Context) {
        val message = when (ex) {
            is ConnectException -> "connection refused"
            is NumberFormatException -> "invalid number: ${ex.message}"
            is ExecutionException -> ex.cause!!.message!!
            else -> ex.message!!
        }

        ctx.status(BAD_REQUEST).json(ApiMessageResponse.error(message.lowercase()))
    }

    override fun close() {
        app.stop()
    }

    private fun requestToOpenImagesOnDesktop(paths: Iterable<Path>): Boolean {
        val port = FileLocker.read().toIntOrNull() ?: return false
        LOG.di("requesting to open images on desktop. port={}, paths={}", port, paths)
        val query = paths.map { "$it".encodeToByteArray() }.joinToString("&") { "path=${Base64.getUrlEncoder().encodeToString(it)}" }
        val url = URL("http://localhost:$port/image/open-on-desktop?$query")
        val connection = url.openConnection() as HttpURLConnection
        connection.setRequestMethod("POST")
        LOG.di("response from opening images on desktop. url={}, code={}", url, connection.responseCode)
        connection.disconnect()
        return true
    }

    companion object {

        internal val LOG = loggerFor<Nebulosa>()

        const val PROPERTIES_FILENAME = "nebulosa.properties"
        const val DEFAULT_HOST = "0.0.0.0"
        const val DEFAULT_PORT = 0

        private val OBJECT_MAPPER = jsonMapper {
            addModule(JavaTimeModule())
            addModule(PathModule())
            addModule(DeviceModule())
            disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
            disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        }
    }
}
