package nebulosa.api.configs

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import nebulosa.api.data.entities.MyObjectBox
import nebulosa.common.concurrency.DaemonThreadFactory
import nebulosa.hips2fits.Hips2FitsService
import nebulosa.horizons.HorizonsService
import nebulosa.json.HasJsonModule
import nebulosa.json.JsonModule
import nebulosa.json.ToJson
import nebulosa.sbd.SmallBodyDatabaseService
import nebulosa.simbad.SimbadService
import okhttp3.Cache
import okhttp3.ConnectionPool
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.greenrobot.eventbus.EventBus
import org.springframework.batch.core.launch.JobLauncher
import org.springframework.batch.core.launch.support.TaskExecutorJobLauncher
import org.springframework.batch.core.repository.JobRepository
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.core.task.SimpleAsyncTaskExecutor
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.http.converter.StringHttpMessageConverter
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import java.nio.file.Path
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.io.path.createDirectories

@Configuration
class BeanConfig {

    @Bean
    fun appPath(): Path = Path.of(System.getProperty("app.dir"))

    @Bean
    fun dataPath(appPath: Path): Path = Path.of("$appPath", "data").createDirectories()

    @Bean
    fun logsPath(appPath: Path): Path = Path.of("$appPath", "logs").createDirectories()

    @Bean
    fun capturesPath(appPath: Path): Path = Path.of("$appPath", "captures").createDirectories()

    @Bean
    fun cachePath(appPath: Path): Path = Path.of("$appPath", "cache").createDirectories()

    @Bean
    @Primary
    @Suppress("UNCHECKED_CAST")
    fun objectMapper(
        @Qualifier("serializer") serializers: List<StdSerializer<*>>,
        @Qualifier("deserializer") deserializers: List<StdDeserializer<*>>,
        serializers2: List<ToJson<*>>,
    ) = ObjectMapper()
        .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        .enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
        .registerModule(HasJsonModule())
        .registerModule(JsonModule(serializers2, emptyList()))
        .registerModule(SimpleModule().apply {
            serializers.forEach(::addSerializer)
            deserializers.forEach { addDeserializer(it.handledType() as Class<Any>, it) }
        })!!

    @Bean
    fun connectionPool() = ConnectionPool(32, 5L, TimeUnit.MINUTES)

    @Bean
    fun cache(cachePath: Path) = Cache(cachePath.toFile(), MAX_CACHE_SIZE)

    @Bean
    fun okHttpClient(connectionPool: ConnectionPool, cache: Cache) = OkHttpClient.Builder()
        .connectionPool(connectionPool)
        .cache(cache)
        .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BASIC))
        .readTimeout(60L, TimeUnit.SECONDS)
        .writeTimeout(60L, TimeUnit.SECONDS)
        .connectTimeout(60L, TimeUnit.SECONDS)
        .callTimeout(60L, TimeUnit.SECONDS)
        .build()

    @Bean
    fun boxStore(dataPath: Path) = MyObjectBox.builder()
        .directory(dataPath.toFile())
        .build()!!

    @Bean
    fun horizonsService(okHttpClient: OkHttpClient) = HorizonsService(okHttpClient = okHttpClient)

    @Bean
    fun simbadService(okHttpClient: OkHttpClient) = SimbadService(okHttpClient = okHttpClient)

    @Bean
    fun smallBodyDatabaseService(okHttpClient: OkHttpClient) = SmallBodyDatabaseService(okHttpClient = okHttpClient)

    @Bean
    fun hips2FitsService(okHttpClient: OkHttpClient) = Hips2FitsService(okHttpClient = okHttpClient)

    @Bean
    fun systemExecutorService(): ExecutorService =
        Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors(), DaemonThreadFactory)

    @Bean
    fun eventBus(systemExecutorService: ExecutorService) = EventBus.builder()
        .sendNoSubscriberEvent(false)
        .sendSubscriberExceptionEvent(false)
        .throwSubscriberException(false)
        .logNoSubscriberMessages(false)
        .logSubscriberExceptions(false)
        .executorService(systemExecutorService)
        .installDefaultEventBus()!!

    @Bean
    @Primary
    fun asyncJobLauncher(jobRepository: JobRepository): JobLauncher {
        val jobLauncher = TaskExecutorJobLauncher()
        jobLauncher.setJobRepository(jobRepository)
        jobLauncher.setTaskExecutor(SimpleAsyncTaskExecutor(DaemonThreadFactory))
        jobLauncher.afterPropertiesSet()
        return jobLauncher
    }

    @Bean
    fun webMvcConfigurer() = object : WebMvcConfigurer {

        override fun extendMessageConverters(converters: MutableList<HttpMessageConverter<*>>) {
            converters.removeIf { it is StringHttpMessageConverter }
        }

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
