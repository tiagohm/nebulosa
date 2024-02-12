package nebulosa.api.beans.configurations

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import com.fasterxml.jackson.module.kotlin.kotlinModule
import io.objectbox.BoxStore
import nebulosa.api.atlas.SatelliteEntity
import nebulosa.api.atlas.SimbadEntity
import nebulosa.api.calibration.CalibrationFrameEntity
import nebulosa.api.entities.MyObjectBox
import nebulosa.api.locations.LocationEntity
import nebulosa.api.preferences.PreferenceEntity
import nebulosa.batch.processing.AsyncJobLauncher
import nebulosa.common.json.PathDeserializer
import nebulosa.common.json.PathSerializer
import nebulosa.guiding.Guider
import nebulosa.guiding.phd2.PHD2Guider
import nebulosa.hips2fits.Hips2FitsService
import nebulosa.horizons.HorizonsService
import nebulosa.imaging.Image
import nebulosa.phd2.client.PHD2Client
import nebulosa.sbd.SmallBodyDatabaseService
import nebulosa.simbad.SimbadService
import nebulosa.star.detection.StarDetector
import nebulosa.watney.star.detection.WatneyStarDetector
import okhttp3.Cache
import okhttp3.ConnectionPool
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.greenrobot.eventbus.EventBus
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.http.converter.StringHttpMessageConverter
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import java.nio.file.Path
import java.util.concurrent.TimeUnit
import kotlin.io.path.createDirectories

@Configuration
class BeanConfiguration {

    @Bean
    fun appPath(): Path = Path.of(System.getProperty("app.dir"))

    @Bean
    fun dataPath(appPath: Path): Path = Path.of("$appPath", "data").createDirectories()

    @Bean
    fun logsPath(appPath: Path): Path = Path.of("$appPath", "logs").createDirectories()

    @Bean
    fun capturesPath(appPath: Path): Path = Path.of("$appPath", "captures").createDirectories()

    @Bean
    fun sequencesPath(appPath: Path): Path = Path.of("$appPath", "sequences").createDirectories()

    @Bean
    fun cachePath(appPath: Path): Path = Path.of("$appPath", "cache").createDirectories()

    @Bean
    fun libsPath(appPath: Path): Path = Path.of("$appPath", "libs").createDirectories()

    @Bean
    @Suppress("UNCHECKED_CAST")
    fun kotlinModule(
        serializers: List<StdSerializer<*>>,
        deserializers: List<StdDeserializer<*>>,
    ): SimpleModule = kotlinModule()
        .apply { serializers.forEach { addSerializer(it) } }
        .apply { deserializers.forEach { addDeserializer(it.handledType() as Class<Any>, it) } }
        .addSerializer(PathSerializer)
        .addDeserializer(Path::class.java, PathDeserializer)

    @Bean
    fun jackson2ObjectMapperBuilderCustomizer() = Jackson2ObjectMapperBuilderCustomizer {
        it.featuresToDisable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        it.featuresToEnable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
    }

    @Bean
    fun connectionPool() = ConnectionPool(32, 5L, TimeUnit.MINUTES)

    @Bean
    fun cache(cachePath: Path) = Cache(cachePath.toFile(), MAX_CACHE_SIZE)

    @Bean
    fun httpClient(connectionPool: ConnectionPool, cache: Cache) = OkHttpClient.Builder()
        .connectionPool(connectionPool)
        .cache(cache)
        .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BASIC))
        .readTimeout(60L, TimeUnit.SECONDS)
        .writeTimeout(60L, TimeUnit.SECONDS)
        .connectTimeout(60L, TimeUnit.SECONDS)
        .callTimeout(60L, TimeUnit.SECONDS)
        .build()

    @Bean
    fun horizonsService(httpClient: OkHttpClient) = HorizonsService(httpClient = httpClient)

    @Bean
    fun simbadService(httpClient: OkHttpClient) = SimbadService(httpClient = httpClient)

    @Bean
    fun smallBodyDatabaseService(httpClient: OkHttpClient) = SmallBodyDatabaseService(httpClient = httpClient)

    @Bean
    fun hips2FitsService(httpClient: OkHttpClient) = Hips2FitsService(httpClient = httpClient)

    @Bean
    fun threadPoolTaskExecutor(): ThreadPoolTaskExecutor {
        val taskExecutor = ThreadPoolTaskExecutor()
        taskExecutor.corePoolSize = 32
        taskExecutor.initialize()
        return taskExecutor
    }

    @Bean
    fun eventBus(threadPoolTaskExecutor: ThreadPoolTaskExecutor) = EventBus.builder()
        .sendNoSubscriberEvent(false)
        .sendSubscriberExceptionEvent(false)
        .throwSubscriberException(false)
        .logNoSubscriberMessages(false)
        .logSubscriberExceptions(false)
        .executorService(threadPoolTaskExecutor.threadPoolExecutor)
        .installDefaultEventBus()!!

    @Bean
    fun phd2Client() = PHD2Client()

    @Bean
    fun phd2Guider(phd2Client: PHD2Client): Guider = PHD2Guider(phd2Client)

    @Bean
    fun asyncJobLauncher(threadPoolTaskExecutor: ThreadPoolTaskExecutor) = AsyncJobLauncher(threadPoolTaskExecutor)

    @Bean
    @Primary
    fun watneyStarDetector(): StarDetector<Image> = WatneyStarDetector(computeHFD = true)

    @Bean
    @Primary
    fun boxStore(dataPath: Path) = MyObjectBox.builder()
        .baseDirectory(dataPath.toFile())
        .name("main")
        .build()!!

    @Bean
    fun simbadBoxStore(dataPath: Path) = MyObjectBox.builder()
        .baseDirectory(dataPath.toFile())
        .name("simbad")
        .build()!!

    @Bean
    fun locationBox(boxStore: BoxStore) = boxStore.boxFor(LocationEntity::class.java)!!

    @Bean
    fun calibrationFrameBox(boxStore: BoxStore) = boxStore.boxFor(CalibrationFrameEntity::class.java)!!

    @Bean
    fun preferenceBox(boxStore: BoxStore) = boxStore.boxFor(PreferenceEntity::class.java)!!

    @Bean
    fun satelliteBox(boxStore: BoxStore) = boxStore.boxFor(SatelliteEntity::class.java)!!

    @Bean
    fun simbadEntityBox(@Qualifier("simbadBoxStore") boxStore: BoxStore) = boxStore.boxFor(SimbadEntity::class.java)!!

    @Bean
    fun webMvcConfigurer(
        deviceOrEntityParamMethodArgumentResolver: HandlerMethodArgumentResolver,
        dateAndTimeParamMethodArgumentResolver: HandlerMethodArgumentResolver,
        angleParamMethodArgumentResolver: HandlerMethodArgumentResolver,
        durationUnitMethodArgumentResolver: HandlerMethodArgumentResolver,
    ) = object : WebMvcConfigurer {

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

        override fun addArgumentResolvers(resolvers: MutableList<HandlerMethodArgumentResolver>) {
            resolvers.add(deviceOrEntityParamMethodArgumentResolver)
            resolvers.add(dateAndTimeParamMethodArgumentResolver)
            resolvers.add(angleParamMethodArgumentResolver)
            resolvers.add(durationUnitMethodArgumentResolver)
        }
    }

    companion object {

        const val MAX_CACHE_SIZE = 1024L * 1024L * 32L // 32MB
    }
}
