package nebulosa.desktop

import ch.qos.logback.classic.Level
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import nebulosa.desktop.logic.Preferences
import nebulosa.desktop.logic.atlas.provider.ephemeris.TimeBucket
import nebulosa.hips2fits.Hips2FitsService
import nebulosa.horizons.HorizonsService
import nebulosa.sbd.SmallBodyDatabaseLookupService
import nebulosa.simbad.SimbadService
import okhttp3.Cache
import okhttp3.ConnectionPool
import okhttp3.OkHttpClient
import org.greenrobot.eventbus.EventBus
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean
import org.springframework.scheduling.annotation.EnableAsync
import java.nio.file.Path
import java.nio.file.Paths
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.io.path.createDirectories
import ch.qos.logback.classic.Logger as LogbackLogger

@EnableAsync
@SpringBootApplication
class App : CommandLineRunner {

    @Bean
    fun appDirectory(): Path = Paths.get(System.getProperty("app.dir"))

    @Bean
    fun objectMapper() = ObjectMapper()
        .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        .enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)!!

    @Bean
    fun connectionPool() = ConnectionPool(32, 5L, TimeUnit.MINUTES)

    @Bean
    fun cache(appDirectory: Path) = Cache(Paths.get("$appDirectory", "cache").createDirectories().toFile(), MAX_CACHE_SIZE)

    @Bean
    fun okHttpClient(connectionPool: ConnectionPool, cache: Cache) = OkHttpClient.Builder()
        .connectionPool(connectionPool)
        .cache(cache)
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
    fun focuserExecutorService(): ExecutorService = Executors.newSingleThreadExecutor()

    @Bean
    fun filterWheelExecutorService(): ExecutorService = Executors.newSingleThreadExecutor()

    @Bean
    fun guiderExecutorService(): ExecutorService = Executors.newSingleThreadExecutor()

    @Bean
    fun systemExecutorService(): ExecutorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())

    @Bean
    fun horizonsService(okHttpClient: OkHttpClient) = HorizonsService(okHttpClient = okHttpClient)

    @Bean
    fun simbadService(okHttpClient: OkHttpClient) = SimbadService(okHttpClient = okHttpClient)

    @Bean
    fun smallBodyDatabaseLookupService(okHttpClient: OkHttpClient) = SmallBodyDatabaseLookupService(okHttpClient = okHttpClient)

    @Bean
    fun hips2FitsService(okHttpClient: OkHttpClient) = Hips2FitsService(okHttpClient = okHttpClient)

    @Bean
    fun eventBus() = EventBus.builder()
        .sendNoSubscriberEvent(false)
        .sendSubscriberExceptionEvent(false)
        .throwSubscriberException(false)
        .logNoSubscriberMessages(false)
        .logSubscriberExceptions(false)
        .build()!!

    @Bean
    fun timeBucket() = TimeBucket()

    override fun run(vararg args: String) {
        with(if ("-v" in args) Level.DEBUG else Level.INFO) {
            logger(Logger.ROOT_LOGGER_NAME).level = this
            logger("javafx").level = Level.WARN
        }
    }

    companion object {

        const val MAX_CACHE_SIZE = 1024L * 1024L * 512L // 512MB

        @Suppress("NOTHING_TO_INLINE")
        private inline fun logger(name: String) = LoggerFactory.getLogger(name) as LogbackLogger
    }
}
