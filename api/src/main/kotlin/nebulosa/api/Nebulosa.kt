package nebulosa.api

import ch.qos.logback.classic.Level
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jsonMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.github.rvesse.airline.annotations.Command
import com.github.rvesse.airline.annotations.Option
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import kotlinx.coroutines.runBlocking
import nebulosa.api.converters.DeviceModule
import nebulosa.api.core.FileLocker
import nebulosa.api.database.migration.MainDatabaseMigrator
import nebulosa.api.database.migration.SkyDatabaseMigrator
import nebulosa.api.inject.controllersModule
import nebulosa.api.inject.coreModule
import nebulosa.api.inject.databaseModule
import nebulosa.api.inject.eventBusModule
import nebulosa.api.inject.httpModule
import nebulosa.api.inject.objectMapperModule
import nebulosa.api.inject.pathModule
import nebulosa.api.inject.phd2Module
import nebulosa.api.inject.repositoriesModule
import nebulosa.api.inject.serverModule
import nebulosa.api.inject.servicesModule
import nebulosa.api.ktor.configureHTTP
import nebulosa.api.ktor.configureMonitoring
import nebulosa.api.ktor.configureRouting
import nebulosa.api.ktor.configureSerialization
import nebulosa.api.ktor.configureSockets
import nebulosa.api.preference.PreferenceService
import nebulosa.json.PathModule
import nebulosa.log.d
import nebulosa.log.loggerFor
import org.koin.core.context.startKoin
import org.slf4j.Logger
import java.net.HttpURLConnection
import java.net.URL
import java.nio.file.Path
import java.util.*
import java.util.concurrent.ExecutorService
import kotlin.io.path.Path
import kotlin.io.path.exists
import kotlin.io.path.fileSize
import kotlin.io.path.isRegularFile
import kotlin.system.exitProcess

@Command(name = "nebulosa")
class Nebulosa : Runnable {

    private val preferencesPath = Path(System.getProperty(APP_DIR_KEY), PreferenceService.FILENAME)
    private val preferences = PreferenceService()

    init {
        preferencesPath
            .takeIf { it.exists() && it.isRegularFile() }
            ?.also(preferences::load)
    }

    @Option(name = ["-h", "--host"])
    private var host = preferences["host"]?.ifBlank { null } ?: DEFAULT_HOST

    @Option(name = ["-p", "--port"])
    private var port = preferences["port"]?.toIntOrNull() ?: DEFAULT_PORT

    @Option(name = ["-d", "--debug"])
    private var debug = preferences["debug"]?.toBoolean() == true

    @Option(name = ["-t", "--trace"])
    private var trace = preferences["trace"]?.toBoolean() == true

    @Option(name = ["-f", "--files"])
    private val files = mutableListOf<String>()

    override fun run() {
        if (debug) {
            with(loggerFor("nebulosa")) {
                level = Level.DEBUG
            }

            if (trace) {
                with(loggerFor(Logger.ROOT_LOGGER_NAME)) {
                    level = Level.TRACE
                }
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
        // https://start.ktor.io/settings?name=ktor-sample&website=example.com&artifact=com.example.ktor-sample&kotlinVersion=2.0.21&ktorVersion=3.0.1&buildSystem=GRADLE_KTS&engine=NETTY&configurationIn=CODE&addSampleCode=true&plugins=routing%2Cktor-websockets%2Ccontent-negotiation%2Cktor-jackson%2Ccall-logging%2Ccors%2Ccompression%2Cstatic-content%2Cresources
        val server = embeddedServer(Netty, port = port, host = host) {
            configureHTTP()
            configureSerialization(OBJECT_MAPPER)
            configureSockets()
            configureRouting()
            configureMonitoring(debug)
        }.start(false)

        val koinApp = startKoin {
            modules(pathModule())
            modules(coreModule())
            modules(httpModule())
            modules(databaseModule())
            modules(eventBusModule())
            modules(repositoriesModule())
            modules(phd2Module())
            modules(serverModule(server))
            modules(objectMapperModule(OBJECT_MAPPER))
            modules(servicesModule(preferences))
            modules(controllersModule())
        }

        with(runBlocking { server.engine.resolvedConnectors().first().port }) {
            println("server is started at port: $this")
            FileLocker.write("$this")
        }

        with(koinApp.koin) {
            val executor = get<ExecutorService>()
            executor.submit(get<MainDatabaseMigrator>())
            executor.submit(get<SkyDatabaseMigrator>())
        }

        Thread.currentThread().join()
    }

    private fun requestToOpenImagesOnDesktop(paths: Iterable<Path>): Boolean {
        val port = FileLocker.read().toIntOrNull() ?: return false
        LOG.d { info("requesting to open images on desktop. port={}, paths={}", port, paths) }
        val query = paths.map { "$it".encodeToByteArray() }.joinToString("&") { "path=${Base64.getUrlEncoder().encodeToString(it)}" }
        val url = URL("http://localhost:$port/image/open-on-desktop?$query")
        val connection = url.openConnection() as HttpURLConnection
        connection.setRequestMethod("POST")
        LOG.d { info("response from opening images on desktop. url={}, code={}", url, connection.responseCode) }
        connection.disconnect()
        return true
    }

    companion object {

        internal val LOG = loggerFor<Nebulosa>()

        const val DEFAULT_HOST = "0.0.0.0"
        const val DEFAULT_PORT = 0

        private val OBJECT_MAPPER = jsonMapper {
            addModule(JavaTimeModule())
            addModule(PathModule())
            addModule(DeviceModule())
            disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
            disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        }.registerKotlinModule()
    }
}
