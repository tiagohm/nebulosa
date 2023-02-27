package nebulosa.desktop

import ch.qos.logback.classic.Level
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import javafx.scene.text.Font
import nebulosa.desktop.logic.*
import nebulosa.desktop.logic.loader.IERSLoader
import nebulosa.hips2fits.Hips2FitsService
import nebulosa.io.resource
import nebulosa.query.horizons.HorizonsService
import nebulosa.query.sbd.SmallBodyDatabaseLookupService
import nebulosa.query.simbad.SimbadService
import okhttp3.ConnectionPool
import okhttp3.OkHttpClient
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.config.AutowireCapableBeanFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import javax.swing.filechooser.FileSystemView
import kotlin.io.path.createDirectories

@SpringBootApplication
class App : CommandLineRunner {

    enum class OSType {
        WINDOWS,
        LINUX,
        MAC,
        OTHER,
    }

    @Autowired private lateinit var beanFactory: AutowireCapableBeanFactory

    @Bean
    fun operatingSystemType(): OSType {
        val osName = System.getProperty("os.name", "generic").lowercase()

        return when {
            "mac" in osName || "darwin" in osName -> OSType.MAC
            "win" in osName -> OSType.WINDOWS
            "nux" in osName -> OSType.LINUX
            else -> OSType.OTHER
        }
    }

    @Bean
    fun appDirectory(operatingSystemType: OSType): Path? {
        val appDirectory = when (operatingSystemType) {
            OSType.LINUX -> {
                val userHomeDir = Paths.get(System.getProperty("user.home"))
                Paths.get("$userHomeDir", ".nebulosa")
            }
            OSType.WINDOWS -> {
                val documentsDir = FileSystemView.getFileSystemView().defaultDirectory.path
                Paths.get(documentsDir, "Nebulosa")
            }
            else -> {
                null
            }
        }

        appDirectory?.createDirectories()

        return appDirectory
    }

    @Bean
    fun objectMapper() = ObjectMapper()
        .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        .enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)!!

    @Bean
    fun connectionPool() = ConnectionPool(32, 5L, TimeUnit.MINUTES)

    @Bean
    fun okHttpClient(connectionPool: ConnectionPool) = OkHttpClient.Builder()
        .connectionPool(connectionPool)
        .readTimeout(30L, TimeUnit.SECONDS)
        .writeTimeout(30L, TimeUnit.SECONDS)
        .connectTimeout(30L, TimeUnit.SECONDS)
        .callTimeout(30L, TimeUnit.SECONDS)
        .build()

    @Bean
    fun preferences(appDirectory: Path, objectMapper: ObjectMapper) = Preferences(Paths.get("$appDirectory", "preferences.properties"), objectMapper)

    @Bean
    fun cameraExecutorService(): ExecutorService = Executors.newSingleThreadExecutor()

    @Bean
    fun mountExecutorService(): ExecutorService = Executors.newSingleThreadExecutor()

    @Bean
    fun systemExecutorService(): ExecutorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())

    @Bean
    fun horizonsService() = HorizonsService()

    @Bean
    fun simbadService() = SimbadService()

    @Bean
    fun smallBodyDatabaseLookupService() = SmallBodyDatabaseLookupService()

    @Bean
    fun hips2FitsService() = Hips2FitsService()

    @Bean
    fun connectionEventBus(): ConnectionEventBus = newEventBus()

    @Bean
    fun deviceEventBus(): DeviceEventBus = newEventBus()

    @Bean
    fun taskEventBus(): TaskEventBus = newEventBus()

    override fun run(vararg args: String) {
        // Sets default locale to en_US.
        Locale.setDefault(Locale.ENGLISH)

        // Fonts.
        Font.loadFont(resource("fonts/Material-Design-Icons.ttf"), 24.0)
        Font.loadFont(resource("fonts/Roboto-Regular.ttf"), 12.0)
        Font.loadFont(resource("fonts/Roboto-Bold.ttf"), 12.0)

        System.setProperty("prism.lcdtext", "false")

        beanFactory.createBean(IERSLoader::class.java).start()

        // Log level.
        with(LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME) as ch.qos.logback.classic.Logger) {
            level = if ("-v" in args) Level.DEBUG else Level.INFO
        }
    }
}
