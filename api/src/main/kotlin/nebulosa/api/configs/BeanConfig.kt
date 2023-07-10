package nebulosa.api.configs

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import nebulosa.api.data.entities.MyObjectBox
import nebulosa.hips2fits.Hips2FitsService
import nebulosa.horizons.HorizonsService
import nebulosa.sbd.SmallBodyDatabaseLookupService
import nebulosa.simbad.SimbadService
import okhttp3.Cache
import okhttp3.ConnectionPool
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.greenrobot.eventbus.EventBus
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import java.nio.file.Path
import java.util.concurrent.TimeUnit
import kotlin.io.path.createDirectories

@Configuration
class BeanConfig {

    @Bean
    fun appDirectory(): Path = Path.of(System.getProperty("app.dir"))

    @Bean
    fun dataDirectory(appDirectory: Path): Path = Path.of("$appDirectory", "data").createDirectories()

    @Bean
    fun logsDirectory(appDirectory: Path): Path = Path.of("$appDirectory", "logs").createDirectories()

    @Bean
    fun capturesDirectory(appDirectory: Path): Path = Path.of("$appDirectory", "captures").createDirectories()

    @Bean
    fun cacheDirectory(appDirectory: Path): Path = Path.of("$appDirectory", "cache").createDirectories()

    @Bean
    @Primary
    fun objectMapper() = ObjectMapper()
        .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        .enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)!!

    @Bean
    fun connectionPool() = ConnectionPool(32, 5L, TimeUnit.MINUTES)

    @Bean
    fun cache(cacheDirectory: Path) = Cache(cacheDirectory.toFile(), MAX_CACHE_SIZE)

    @Bean
    fun okHttpClient(connectionPool: ConnectionPool, cache: Cache) = OkHttpClient.Builder()
        .connectionPool(connectionPool)
        .cache(cache)
        .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BASIC))
        .readTimeout(30L, TimeUnit.SECONDS)
        .writeTimeout(30L, TimeUnit.SECONDS)
        .connectTimeout(30L, TimeUnit.SECONDS)
        .callTimeout(30L, TimeUnit.SECONDS)
        .build()

    @Bean
    fun boxStore(dataDirectory: Path) = MyObjectBox.builder()
        .directory(dataDirectory.toFile())
        .build()!!

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
        .installDefaultEventBus()!!

    @Bean
    fun corsConfigurer() = object : WebMvcConfigurer {
        override fun addCorsMappings(registry: CorsRegistry) {
            registry
                .addMapping("/**")
                .allowedOrigins("*")
                .allowedMethods("*")
                .allowedHeaders("*")
                .exposedHeaders("X-Image-Info")
        }
    }

    companion object {

        const val MAX_CACHE_SIZE = 1024L * 1024L * 32L // 32MB
    }
}
