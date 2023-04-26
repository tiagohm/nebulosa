package nebulosa.desktop

import ch.qos.logback.classic.Level
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import nebulosa.desktop.data.DeepSkyObjectEntity
import nebulosa.desktop.data.PreferenceEntity
import nebulosa.desktop.data.StarEntity
import nebulosa.desktop.logic.atlas.provider.ephemeris.TimeBucket
import nebulosa.desktop.repository.app.PreferenceRepository
import nebulosa.desktop.repository.sky.DeepSkyObjectRepository
import nebulosa.hips2fits.Hips2FitsService
import nebulosa.horizons.HorizonsService
import nebulosa.io.resource
import nebulosa.io.transferAndClose
import nebulosa.sbd.SmallBodyDatabaseLookupService
import nebulosa.simbad.SimbadService
import okhttp3.Cache
import okhttp3.ConnectionPool
import okhttp3.OkHttpClient
import org.greenrobot.eventbus.EventBus
import org.hibernate.community.dialect.SQLiteDialect
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.jdbc.datasource.DriverManagerDataSource
import org.springframework.orm.jpa.JpaTransactionManager
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.transaction.annotation.EnableTransactionManagement
import org.sqlite.JDBC
import java.nio.file.Path
import java.nio.file.Paths
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.zip.GZIPInputStream
import javax.sql.DataSource
import kotlin.io.path.createDirectories
import kotlin.io.path.outputStream
import ch.qos.logback.classic.Logger as LogbackLogger

@EnableAsync
@SpringBootApplication
class App : CommandLineRunner {

    @Configuration
    @EnableTransactionManagement
    @EnableJpaRepositories(
        entityManagerFactoryRef = "appEntityManagerFactory",
        transactionManagerRef = "appTransactionManager",
        basePackageClasses = [PreferenceRepository::class],
    )
    class AppDatabaseConfig {

        @Bean
        @Primary
        fun appEntityManagerFactory(builder: EntityManagerFactoryBuilder, appDataSource: DataSource) = builder
            .dataSource(appDataSource)
            .properties(mapOf("hibernate.dialect" to SQLiteDialect::class.java.name))
            .packages(PreferenceEntity::class.java)
            .build()!!

        @Bean
        fun appTransactionManager(appEntityManagerFactory: LocalContainerEntityManagerFactoryBean) =
            JpaTransactionManager(appEntityManagerFactory.`object`!!)
    }

    @Configuration
    @EnableTransactionManagement
    @EnableJpaRepositories(
        entityManagerFactoryRef = "skyEntityManagerFactory",
        transactionManagerRef = "skyTransactionManager",
        basePackageClasses = [DeepSkyObjectRepository::class],
    )
    class SkyDatabaseConfig {

        @Bean
        fun skyEntityManagerFactory(builder: EntityManagerFactoryBuilder, @Qualifier("skyDataSource") skyDataSource: DataSource) = builder
            .dataSource(skyDataSource)
            .properties(mapOf("hibernate.dialect" to SQLiteDialect::class.java.name, "open_mode" to "1"))
            .packages(DeepSkyObjectEntity::class.java, StarEntity::class.java)
            .build()!!

        @Bean
        fun skyTransactionManager(@Qualifier("skyEntityManagerFactory") skyEntityManagerFactory: LocalContainerEntityManagerFactoryBean) =
            JpaTransactionManager(skyEntityManagerFactory.`object`!!)
    }

    @Bean
    fun appDirectory(): Path = Paths.get(System.getProperty("app.dir"))

    @Bean
    @Primary
    fun appDataSource(appDirectory: Path) = DriverManagerDataSource().apply {
        val path = Paths.get("$appDirectory", "data", "database", "app.db")
        path.parent.createDirectories()
        initialize(path)
    }

    @Bean
    fun skyDataSource(appDirectory: Path) = DriverManagerDataSource().apply {
        val path = Paths.get("$appDirectory", "data", "database", "sky.db")
        path.parent.createDirectories()

        GZIPInputStream(resource("data/StarCatalog.db.gz")!!).transferAndClose(path.outputStream())

        initialize(path)
    }

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
            logger("org.hibernate").level = Level.WARN
            logger("org.hibernate.SQL").level = this
        }
    }

    companion object {

        const val MAX_CACHE_SIZE = 1024L * 1024L * 512L // 512MB

        @Suppress("NOTHING_TO_INLINE")
        private inline fun logger(name: String) = LoggerFactory.getLogger(name) as LogbackLogger

        @JvmStatic
        private fun DriverManagerDataSource.initialize(path: Path) {
            setDriverClassName(JDBC::class.java.name)
            url = "jdbc:sqlite:$path"
        }
    }
}
