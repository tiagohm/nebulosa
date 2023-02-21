package nebulosa.desktop

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import javafx.application.Application
import javafx.scene.text.Font
import nebulosa.desktop.logic.Preferences
import nebulosa.desktop.logic.loader.IERSLoader
import nebulosa.io.resource
import nebulosa.query.horizons.HorizonsService
import nebulosa.query.sbd.SmallBodyDatabaseLookupService
import nebulosa.query.simbad.SimbadService
import okhttp3.ConnectionPool
import okhttp3.OkHttpClient
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
    fun appDirectory(operatingSystemType: OSType): Path {
        val appDirectory = when (operatingSystemType) {
            OSType.LINUX -> {
                val userHomeDir = Paths.get(System.getProperty("user.home"))
                Paths.get("$userHomeDir", ".nebulosa")
            }
            OSType.WINDOWS -> {
                val documentsDir = FileSystemView.getFileSystemView().defaultDirectory.path
                Paths.get(documentsDir, "Nebulosa")
            }
            else -> throw IllegalStateException("invalid os: $operatingSystemType")
        }

        appDirectory.createDirectories()

        return appDirectory
    }

    @Bean
    fun objectMapper(): ObjectMapper {
        val mapper = ObjectMapper()
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        return mapper
    }

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
    fun preferences(appDirectory: Path, objectMapper: ObjectMapper) = Preferences(Paths.get("$appDirectory", "preferences.json"), objectMapper)

    @Bean
    fun cameraExecutorService(): ExecutorService = Executors.newSingleThreadExecutor()

    @Bean
    fun mountExecutorService(): ExecutorService = Executors.newSingleThreadExecutor()

    @Bean
    fun horizonsService() = HorizonsService()

    @Bean
    fun simbadService() = SimbadService()

    @Bean
    fun smallBodyDatabaseLookupService() = SmallBodyDatabaseLookupService()

    override fun run(vararg args: String) {
        App.beanFactory = beanFactory

        // Sets default locale to en_US.
        Locale.setDefault(Locale.ENGLISH)

        // Fonts.
        Font.loadFont(resource("fonts/Material-Design-Icons.ttf"), 24.0)
        Font.loadFont(resource("fonts/Roboto-Regular.ttf"), 12.0)
        Font.loadFont(resource("fonts/Roboto-Bold.ttf"), 12.0)

        System.setProperty("prism.lcdtext", "false")

        IERSLoader().start()

        // Run the JavaFX application.
        Application.launch(Nebulosa::class.java, *args)
    }

    companion object {

        @JvmStatic private lateinit var beanFactory: AutowireCapableBeanFactory

        @JvmStatic
        fun autowireBean(o: Any) = beanFactory.autowireBean(o)

        @JvmStatic
        inline fun <reified T : Any> beanFor() = beanFor(T::class.java)

        @JvmStatic
        fun <T : Any> beanFor(type: Class<out T>) = beanFactory.getBean(type)
    }
}
