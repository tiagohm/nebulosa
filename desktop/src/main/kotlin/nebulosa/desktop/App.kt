package nebulosa.desktop

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import nebulosa.desktop.logic.Preferences
import nebulosa.hips2fits.Hips2FitsService
import nebulosa.horizons.HorizonsService
import nebulosa.sbd.SmallBodyDatabaseLookupService
import nebulosa.simbad.SimbadService
import nebulosa.skycatalog.hyg.HygDatabase
import nebulosa.skycatalog.stellarium.Nebula
import okhttp3.ConnectionPool
import okhttp3.OkHttpClient
import org.greenrobot.eventbus.EventBus
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean
import org.springframework.scheduling.annotation.EnableAsync
import java.nio.file.Path
import java.nio.file.Paths
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

@EnableAsync
@SpringBootApplication
class App {

    @Bean
    fun appDirectory(): Path = Paths.get(System.getProperty("app.dir"))

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
    fun focuserExecutorService(): ExecutorService = Executors.newSingleThreadExecutor()

    @Bean
    fun filterWheelExecutorService(): ExecutorService = Executors.newSingleThreadExecutor()

    @Bean
    fun guiderExecutorService(): ExecutorService = Executors.newFixedThreadPool(3)

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
    fun eventBus() = EventBus.builder()
        .sendNoSubscriberEvent(false)
        .sendSubscriberExceptionEvent(false)
        .throwSubscriberException(false)
        .logNoSubscriberMessages(false)
        .logSubscriberExceptions(false)
        .build()!!

    @Bean
    fun nebula() = Nebula()

    @Bean
    fun hygDatabase() = HygDatabase()
}
