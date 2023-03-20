package nebulosa.desktop

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import nebulosa.desktop.logic.Preferences
import nebulosa.desktop.logic.concurrency.JavaFXExecutor
import nebulosa.desktop.logic.concurrency.JavaFXExecutorService
import nebulosa.hips2fits.Hips2FitsService
import nebulosa.horizons.HorizonsService
import nebulosa.sbd.SmallBodyDatabaseLookupService
import nebulosa.simbad.SimbadService
import nebulosa.stellarium.skycatalog.Nebula
import okhttp3.ConnectionPool
import okhttp3.OkHttpClient
import org.greenrobot.eventbus.EventBus
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.config.AutowireCapableBeanFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean
import java.nio.file.Path
import java.nio.file.Paths
import java.util.concurrent.Executor
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

@SpringBootApplication
class App {

    @Autowired private lateinit var beanFactory: AutowireCapableBeanFactory

    @Bean
    fun operatingSystemType() = getOperatingSystemType()

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
    fun systemExecutorService(): ExecutorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())

    @Bean
    fun javaFXExecutorService(javaFXExecutor: Executor): ExecutorService = JavaFXExecutorService(javaFXExecutor)

    @Bean
    fun horizonsService() = HorizonsService()

    @Bean
    fun simbadService() = SimbadService()

    @Bean
    fun smallBodyDatabaseLookupService() = SmallBodyDatabaseLookupService()

    @Bean
    fun hips2FitsService() = Hips2FitsService()

    @Bean
    fun javaFXExecutor() = JavaFXExecutor()

    @Bean
    fun eventBus(javaFXExecutorService: ExecutorService) = EventBus.builder()
        .sendNoSubscriberEvent(false)
        .sendSubscriberExceptionEvent(false)
        .throwSubscriberException(false)
        .logNoSubscriberMessages(false)
        .logSubscriberExceptions(false)
        .executorService(javaFXExecutorService)
        .build()!!

    @Bean
    fun nebula() = Nebula()
}
