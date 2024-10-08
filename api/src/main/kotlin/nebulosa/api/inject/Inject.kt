package nebulosa.api.inject

import com.fasterxml.jackson.databind.ObjectMapper
import io.javalin.Javalin
import io.objectbox.BoxStore
import io.objectbox.kotlin.boxFor
import nebulosa.api.APP_DIR_KEY
import nebulosa.api.atlas.SatelliteEntity
import nebulosa.api.atlas.SatelliteRepository
import nebulosa.api.atlas.SimbadEntity
import nebulosa.api.atlas.SimbadEntityRepository
import nebulosa.api.calibration.CalibrationFrameEntity
import nebulosa.api.calibration.CalibrationFrameRepository
import nebulosa.api.cameras.CameraEventHub
import nebulosa.api.confirmation.ConfirmationController
import nebulosa.api.confirmation.ConfirmationService
import nebulosa.api.connection.ConnectionController
import nebulosa.api.connection.ConnectionEventHub
import nebulosa.api.connection.ConnectionService
import nebulosa.api.database.MyObjectBox
import nebulosa.api.dustcap.DustCapController
import nebulosa.api.dustcap.DustCapEventHub
import nebulosa.api.dustcap.DustCapService
import nebulosa.api.focusers.FocuserController
import nebulosa.api.focusers.FocuserEventHub
import nebulosa.api.focusers.FocuserService
import nebulosa.api.guiding.GuideOutputController
import nebulosa.api.guiding.GuideOutputEventHub
import nebulosa.api.guiding.GuideOutputService
import nebulosa.api.lightboxes.LightBoxController
import nebulosa.api.lightboxes.LightBoxEventHub
import nebulosa.api.lightboxes.LightBoxService
import nebulosa.api.message.MessageService
import nebulosa.api.mounts.MountEventHub
import nebulosa.api.preference.PreferenceEntity
import nebulosa.api.preference.PreferenceRepository
import nebulosa.api.rotators.RotatorController
import nebulosa.api.rotators.RotatorEventHub
import nebulosa.api.rotators.RotatorService
import nebulosa.api.wheels.WheelController
import nebulosa.api.wheels.WheelEventHub
import nebulosa.api.wheels.WheelService
import nebulosa.guiding.phd2.PHD2Guider
import nebulosa.hips2fits.Hips2FitsService
import nebulosa.horizons.HorizonsService
import nebulosa.log.loggerFor
import nebulosa.phd2.client.PHD2Client
import nebulosa.sbd.SmallBodyDatabaseService
import nebulosa.simbad.SimbadService
import nebulosa.time.SystemClock
import nebulosa.util.concurrency.DaemonThreadFactory
import okhttp3.Cache
import okhttp3.ConnectionPool
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.greenrobot.eventbus.EventBus
import org.koin.core.qualifier.named
import org.koin.dsl.koinApplication
import org.koin.dsl.module
import java.nio.file.Path
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.concurrent.ExecutorService
import java.util.concurrent.SynchronousQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import kotlin.io.path.*

val koinApp = koinApplication {
    modules(pathModule())
    modules(coreModule())
    modules(httpModule())
    modules(eventBusModule())
    modules(boxStoreModule())
    modules(repositoriesModule())
    modules(phd2Module())
}

object Named {

    val appDir = named("appDir")
    val logsDir = named("logsDir")
    val dataDir = named("dataDir")
    val capturesDir = named("capturesDir")
    val sequencesDir = named("sequencesDir")
    val cacheDir = named("cacheDir")
    val libsDir = named("libsDir")
    val defaultHttpClient = named("defaultHttpClient")
    val alpacaHttpClient = named("alpacaHttpClient")
    val mainBoxStore = named("mainBoxStore")
    val simbadBoxStore = named("simbadBoxStore")
    val calibrationFrameBox = named("calibrationFrameBox")
    val preferenceBox = named("preferenceBox")
    val satelliteBox = named("satelliteBox")
    val simbadBox = named("simbadBox")
}

// PATH

private fun Path.clearLogIfPastDays(days: Long = 7L) {
    if (exists()) {
        val pastDays = LocalDate.now(SystemClock).minusDays(days)

        for (entry in listDirectoryEntries("nebulosa-*.log")) {
            val logDate = entry.fileName.toString()
                .replace("nebulosa-", "")
                .replace(".log", "")
                .let { runCatching { LocalDate.parse(it, DateTimeFormatter.ISO_LOCAL_DATE) }.getOrNull() }
                ?: continue

            if (pastDays.isAfter(logDate)) {
                entry.deleteIfExists()
            }
        }
    }
}

fun pathModule(root: Path = Path(requireNotNull(System.getProperty(APP_DIR_KEY)))) = module(true) {
    single(Named.appDir) { root }
    single(Named.logsDir) { Path("$root", "logs").createDirectories().clearLogIfPastDays() }
    single(Named.dataDir) { Path("$root", "data").createDirectories() }
    single(Named.capturesDir) { Path("$root", "captures").createDirectories() }
    single(Named.sequencesDir) { Path("$root", "sequences").createDirectories() }
    single(Named.cacheDir) { Path("$root", "cache").createDirectories() }
    single(Named.libsDir) { Path("$root", "libs").createDirectories() }
}

// CORE

fun coreModule() = module {
    val numberOfCores = Runtime.getRuntime().availableProcessors()

    single<ExecutorService> { ThreadPoolExecutor(numberOfCores, 32, 60L, TimeUnit.SECONDS, SynchronousQueue(), DaemonThreadFactory) }
}

// HTTP

private const val MAX_CACHE_SIZE = 1024L * 1024L * 32L // 32MB
private val OKHTTP_LOG = loggerFor<OkHttpClient>()

fun httpModule() = module {
    single { ConnectionPool(32, 5L, TimeUnit.MINUTES) }
    single { Cache(get<Path>(Named.cacheDir).toFile(), MAX_CACHE_SIZE) }
    single { HttpLoggingInterceptor.Logger { OKHTTP_LOG.info(it) } }
    single(Named.defaultHttpClient) {
        OkHttpClient.Builder()
            .connectionPool(get())
            .cache(get())
            .addInterceptor(HttpLoggingInterceptor(get()).setLevel(HttpLoggingInterceptor.Level.BASIC))
            .readTimeout(60L, TimeUnit.SECONDS)
            .writeTimeout(60L, TimeUnit.SECONDS)
            .connectTimeout(60L, TimeUnit.SECONDS)
            .callTimeout(60L, TimeUnit.SECONDS)
            .build()
    }
    single(Named.alpacaHttpClient) {
        OkHttpClient.Builder()
            .connectionPool(get())
            .readTimeout(90L, TimeUnit.SECONDS)
            .writeTimeout(90L, TimeUnit.SECONDS)
            .connectTimeout(90L, TimeUnit.SECONDS)
            .callTimeout(90L, TimeUnit.SECONDS)
            .build()
    }
}

// EVENT BUS

fun eventBusModule() = module {
    single {
        EventBus.builder()
            .sendNoSubscriberEvent(false)
            .sendSubscriberExceptionEvent(false)
            .throwSubscriberException(false)
            .logNoSubscriberMessages(false)
            .logSubscriberExceptions(false)
            .executorService(get())
            .build()
    }
}

// BOX STORE

fun boxStoreModule() = module {
    single(Named.mainBoxStore) {
        MyObjectBox.builder()
            .baseDirectory(get<Path>(Named.dataDir).toFile())
            .name("main")
            .build()!!
    }
    single(Named.simbadBoxStore) {
        MyObjectBox.builder()
            .baseDirectory(get<Path>(Named.dataDir).toFile())
            .name("simbad")
            .build()!!
    }
    single(Named.calibrationFrameBox) { get<BoxStore>(Named.mainBoxStore).boxFor<CalibrationFrameEntity>() }
    single(Named.preferenceBox) { get<BoxStore>(Named.mainBoxStore).boxFor<PreferenceEntity>() }
    single(Named.satelliteBox) { get<BoxStore>(Named.mainBoxStore).boxFor<SatelliteEntity>() }
    single(Named.simbadBox) { get<BoxStore>(Named.simbadBoxStore).boxFor<SimbadEntity>() }
}

// OBJECT MAPPER

fun objectMapperModule(mapper: ObjectMapper) = module {
    single<ObjectMapper> { mapper }
}

// PHD2

fun phd2Module() = module {
    single { PHD2Client() }
    single { PHD2Guider(get()) }
}

// REPOSITORIES

fun repositoriesModule() = module {
    single { CalibrationFrameRepository(get(Named.calibrationFrameBox)) }
    single { PreferenceRepository(get(Named.preferenceBox)) }
    single { SatelliteRepository(get(Named.satelliteBox)) }
    single { SimbadEntityRepository(get(Named.simbadBox)) }
}

// SERVICES

fun deviceEventHubModule() = module(true) {
    single { CameraEventHub(get(), get()) }
    single { MountEventHub(get(), get()) }
    single { FocuserEventHub(get(), get()) }
    single { WheelEventHub(get(), get()) }
    single { GuideOutputEventHub(get(), get()) }
    single { RotatorEventHub(get(), get()) }
    single { LightBoxEventHub(get(), get()) }
    single { DustCapEventHub(get(), get()) }
    single { ConnectionEventHub(get(), get(), get(), get(), get(), get(), get(), get()) }
}

fun servicesModule() = module {
    single { HorizonsService(httpClient = get(Named.defaultHttpClient)) }
    single { SimbadService(httpClient = get(Named.defaultHttpClient)) }
    single { SmallBodyDatabaseService(httpClient = get(Named.defaultHttpClient)) }
    single { Hips2FitsService(httpClient = get(Named.defaultHttpClient)) }
    single(createdAtStart = true) { MessageService(get()) }
    includes(deviceEventHubModule())
    single(createdAtStart = true) { ConnectionService(get(), get(Named.alpacaHttpClient), get(), get()) }
    single { ConfirmationService(get()) }
    single { RotatorService(get()) }
    single { FocuserService(get()) }
    single { WheelService(get()) }
    single { GuideOutputService(get()) }
    single { LightBoxService(get()) }
    single { DustCapService(get()) }
}

// CONTROLLERS

fun controllersModule() = module(true) {
    single { ConnectionController(get(), get()) }
    single { ConfirmationController(get(), get()) }
    single { RotatorController(get(), get(), get()) }
    single { FocuserController(get(), get(), get()) }
    single { WheelController(get(), get(), get()) }
    single { GuideOutputController(get(), get(), get()) }
    single { LightBoxController(get(), get(), get()) }
    single { DustCapController(get(), get(), get()) }
}

// APP

fun appModule(app: Javalin) = module(true) {
    single { app }
}
