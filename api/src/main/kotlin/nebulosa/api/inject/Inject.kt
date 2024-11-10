package nebulosa.api.inject

import com.fasterxml.jackson.databind.ObjectMapper
import io.ktor.server.engine.EmbeddedServer
import io.ktor.server.netty.NettyApplicationEngine
import nebulosa.api.APP_DIR_KEY
import nebulosa.api.Nebulosa
import nebulosa.api.alignment.polar.PolarAlignmentController
import nebulosa.api.alignment.polar.PolarAlignmentService
import nebulosa.api.alignment.polar.darv.DARVExecutor
import nebulosa.api.alignment.polar.tppa.TPPAExecutor
import nebulosa.api.atlas.*
import nebulosa.api.atlas.ephemeris.BodyEphemerisProvider
import nebulosa.api.atlas.ephemeris.HorizonsEphemerisProvider
import nebulosa.api.autofocus.AutoFocusController
import nebulosa.api.autofocus.AutoFocusExecutor
import nebulosa.api.autofocus.AutoFocusService
import nebulosa.api.calibration.CalibrationFrameController
import nebulosa.api.calibration.CalibrationFrameRepository
import nebulosa.api.calibration.CalibrationFrameService
import nebulosa.api.cameras.CameraCaptureExecutor
import nebulosa.api.cameras.CameraController
import nebulosa.api.cameras.CameraEventHub
import nebulosa.api.cameras.CameraService
import nebulosa.api.confirmation.ConfirmationController
import nebulosa.api.confirmation.ConfirmationService
import nebulosa.api.connection.ConnectionController
import nebulosa.api.connection.ConnectionEventHub
import nebulosa.api.connection.ConnectionService
import nebulosa.api.database.migration.MainDatabaseMigrator
import nebulosa.api.database.migration.SkyDatabaseMigrator
import nebulosa.api.dustcap.DustCapController
import nebulosa.api.dustcap.DustCapEventHub
import nebulosa.api.dustcap.DustCapService
import nebulosa.api.focusers.FocuserController
import nebulosa.api.focusers.FocuserEventHub
import nebulosa.api.focusers.FocuserService
import nebulosa.api.framing.FramingController
import nebulosa.api.framing.FramingService
import nebulosa.api.guiding.*
import nebulosa.api.image.ImageBucket
import nebulosa.api.image.ImageController
import nebulosa.api.image.ImageService
import nebulosa.api.indi.INDIController
import nebulosa.api.indi.INDIEventHandler
import nebulosa.api.indi.INDIService
import nebulosa.api.lightboxes.LightBoxController
import nebulosa.api.lightboxes.LightBoxEventHub
import nebulosa.api.lightboxes.LightBoxService
import nebulosa.api.livestacker.LiveStackingController
import nebulosa.api.livestacker.LiveStackingService
import nebulosa.api.message.MessageService
import nebulosa.api.mounts.MountController
import nebulosa.api.mounts.MountEventHub
import nebulosa.api.mounts.MountService
import nebulosa.api.platesolver.PlateSolverController
import nebulosa.api.platesolver.PlateSolverService
import nebulosa.api.preference.PreferenceRepository
import nebulosa.api.preference.PreferenceService
import nebulosa.api.rotators.RotatorController
import nebulosa.api.rotators.RotatorEventHub
import nebulosa.api.rotators.RotatorService
import nebulosa.api.sequencer.SequencerController
import nebulosa.api.sequencer.SequencerExecutor
import nebulosa.api.sequencer.SequencerService
import nebulosa.api.stardetector.StarDetectionController
import nebulosa.api.stardetector.StarDetectionService
import nebulosa.api.wheels.WheelController
import nebulosa.api.wheels.WheelEventHub
import nebulosa.api.wheels.WheelService
import nebulosa.api.wizard.flat.FlatWizardController
import nebulosa.api.wizard.flat.FlatWizardExecutor
import nebulosa.api.wizard.flat.FlatWizardService
import nebulosa.guiding.Guider
import nebulosa.guiding.phd2.PHD2Guider
import nebulosa.hips2fits.Hips2FitsService
import nebulosa.horizons.HorizonsService
import nebulosa.log.di
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
import org.jetbrains.exposed.sql.Database
import org.koin.core.qualifier.named
import org.koin.dsl.koinApplication
import org.koin.dsl.module
import java.nio.file.Path
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.concurrent.*
import kotlin.io.path.*

val koinApp = koinApplication {
    modules(pathModule())
    modules(coreModule())
    modules(httpModule())
    modules(databaseModule())
    modules(eventBusModule())
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
    val liveStackingDir = named("liveStackingDir")
    val defaultHttpClient = named("defaultHttpClient")
    val alpacaHttpClient = named("alpacaHttpClient")
    val mainConnection = named("mainConnection")
    val mainDatasourceUrl = named("mainDatasource")
    val skyConnection = named("skyConnection")
    val skyDatasourceUrl = named("skyDatasource")
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
    single(Named.liveStackingDir) { Path("$root", "live-stacking").createDirectories() }
}

// CORE

fun coreModule() = module {
    val numberOfCores = Runtime.getRuntime().availableProcessors()

    single<ExecutorService> { ThreadPoolExecutor(numberOfCores, 32, 60L, TimeUnit.SECONDS, SynchronousQueue(), DaemonThreadFactory) }
    single<ScheduledExecutorService> { Executors.newScheduledThreadPool(numberOfCores, DaemonThreadFactory) }
}

// HTTP

private const val MAX_CACHE_SIZE = 1024L * 1024L * 32L // 32MB

fun httpModule() = module {
    single { ConnectionPool(32, 5L, TimeUnit.MINUTES) }
    single { Cache(get<Path>(Named.cacheDir).toFile(), MAX_CACHE_SIZE) }
    single { HttpLoggingInterceptor.Logger { Nebulosa.LOG.di(it) } }
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

fun databaseModule() = module {
    single(Named.mainDatasourceUrl) { "jdbc:h2:${get<Path>(Named.dataDir)}/main;DB_CLOSE_DELAY=-1" }
    single(Named.skyDatasourceUrl) { "jdbc:h2:${get<Path>(Named.dataDir)}/sky;DB_CLOSE_DELAY=-1" }
    single(Named.mainConnection) { Database.connect(get(Named.mainDatasourceUrl), user = "root", password = "") }
    single(Named.skyConnection) { Database.connect(get(Named.skyDatasourceUrl), user = "root", password = "") }
    single { MainDatabaseMigrator(get(Named.mainDatasourceUrl)) }
    single { SkyDatabaseMigrator(get(Named.skyDatasourceUrl)) }
}

// OBJECT MAPPER

fun objectMapperModule(mapper: ObjectMapper) = module {
    single<ObjectMapper> { mapper }
}

// PHD2

fun phd2Module() = module {
    single { PHD2Client() }
    single<Guider> { PHD2Guider(get()) }
}

// REPOSITORIES

fun repositoriesModule() = module {
    single { CalibrationFrameRepository(get(Named.mainConnection)) }
    single { PreferenceRepository(get(Named.mainConnection)) }
    single { SatelliteRepository(get(Named.skyConnection)) }
    single { SkyObjectEntityRepository(get(Named.skyConnection)) }
}

// SERVICES

fun eventHandlerModule() = module(true) {
    single { CameraEventHub(get(), get()) }
    single { MountEventHub(get(), get()) }
    single { FocuserEventHub(get(), get()) }
    single { WheelEventHub(get(), get()) }
    single { GuideOutputEventHub(get(), get()) }
    single { RotatorEventHub(get(), get()) }
    single { LightBoxEventHub(get(), get()) }
    single { DustCapEventHub(get(), get()) }
    single { ConnectionEventHub(get(), get(), get(), get(), get(), get(), get(), get()) }
    single { INDIEventHandler(get(), get()) }
}

fun tasksModule() = module(true) {
    single { IERSUpdateTask(get(Named.dataDir), get(Named.defaultHttpClient), get(), get()) }
    single { SkyAtlasUpdateTask(get(Named.defaultHttpClient), get(), get(), get(), get()) }
    single { SatelliteUpdateTask(get(Named.defaultHttpClient), get(), get(), get(), get()) }
    single { LibWCSDownloadTask(get(Named.libsDir), get(Named.defaultHttpClient), get(), get()) }
}

fun servicesModule() = module {
    single { HorizonsService(httpClient = get(Named.defaultHttpClient)) }
    single { SimbadService(httpClient = get(Named.defaultHttpClient)) }
    single { SmallBodyDatabaseService(httpClient = get(Named.defaultHttpClient)) }
    single { Hips2FitsService(httpClient = get(Named.defaultHttpClient)) }
    single(createdAtStart = true) { MessageService(get(), get()) }
    includes(eventHandlerModule())
    single(createdAtStart = true) { ConnectionService(get(), get(Named.alpacaHttpClient), get(), get()) }
    single { ConfirmationService(get()) }
    single { RotatorService(get()) }
    single { FocuserService(get()) }
    single { WheelService(get()) }
    single { GuideOutputService(get()) }
    single { LightBoxService(get()) }
    single { DustCapService(get()) }
    single { ImageBucket(get()) }
    single { CalibrationFrameService(get()) }
    single { FramingService(get(), get()) }
    single { ImageService(get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    single { PlateSolverService(get(), get()) }
    single { FlatWizardExecutor(get(), get(), get()) }
    single { FlatWizardService(get(Named.capturesDir), get()) }
    single { StarDetectionService() }
    single { AutoFocusExecutor(get(), get(), get()) }
    single { AutoFocusService(get()) }
    single { LiveStackingService() }
    single { FramingService(get(), get()) }
    single { INDIService(get()) }
    single { DARVExecutor(get(), get(), get()) }
    single { TPPAExecutor(get(), get(), get()) }
    single { PolarAlignmentService(get(), get()) }
    single { PreferenceService(get(), get()) }
    single { GuidingService(get(), get(), get(), get()) }
    single { SequencerExecutor(get(), get(), get(), get(), get()) }
    single { SequencerService(get(Named.sequencesDir), get()) }
    single { MoonPhaseFinder(get()) }
    single { HorizonsEphemerisProvider(get()) }
    single { BodyEphemerisProvider(get()) }
    single { SkyAtlasService(get(), get(), get(), get(), get(), get(Named.defaultHttpClient), get(), get(), get()) }
    single { MountService(get(), get(), get(), get(), get()) }
    single { CameraCaptureExecutor(get(), get(), get(), get(), get()) }
    single { CameraService(get(Named.capturesDir), get(), get()) }
    includes(tasksModule())
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
    single { CalibrationFrameController(get(), get()) }
    single { ImageController(get(), get(), get(), get()) }
    single { PlateSolverController(get(), get()) }
    single { FlatWizardController(get(), get(), get()) }
    single { StarDetectionController(get(), get()) }
    single { AutoFocusController(get(), get(), get()) }
    single { LiveStackingController(get(), get(), get()) }
    single { FramingController(get(), get(), get()) }
    single { INDIController(get(), get(), get()) }
    single { PolarAlignmentController(get(), get(), get()) }
    single { GuidingController(get(), get()) }
    single { SequencerController(get(), get(), get()) }
    single { SkyAtlasController(get(), get(), get(), get()) }
    single { MountController(get(), get(), get()) }
    single { CameraController(get(), get(), get()) }
}

// SERVER

fun serverModule(server: EmbeddedServer<NettyApplicationEngine, NettyApplicationEngine.Configuration>) = module(true) {
    single { server }
    single { server.application }
}
