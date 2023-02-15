package nebulosa.desktop

import ch.qos.logback.classic.Level
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import javafx.application.Application
import javafx.scene.text.Font
import nebulosa.desktop.logic.EquipmentManager
import nebulosa.desktop.logic.Preferences
import nebulosa.desktop.logic.connection.ConnectionManager
import nebulosa.desktop.logic.loader.IERSLoader
import nebulosa.io.resource
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.named
import org.koin.core.module.dsl.withOptions
import org.koin.dsl.module
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*
import java.util.concurrent.Executors
import kotlin.io.path.createDirectories

private fun appDirectory(): Path {
    val userHomeDir = Paths.get(System.getProperty("user.home"))
    // TODO: Use different directory name based on current OS.
    val appDirectory = Paths.get("$userHomeDir", ".nebulosa")
    appDirectory.createDirectories()
    return appDirectory
}

private fun inject() = module {
    val appDirectory = appDirectory()

    single { appDirectory } withOptions { named("app") }

    val mapper = ObjectMapper()
    mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)

    single(createdAtStart = true) { mapper }
    single(createdAtStart = true) { Preferences(Paths.get("$appDirectory", "preferences.json")) }

    single(createdAtStart = true) { ConnectionManager() }
    single(createdAtStart = true) { EquipmentManager() }

    single { Executors.newSingleThreadExecutor() } withOptions { named("camera") }
    single { Executors.newSingleThreadExecutor() } withOptions { named("mount") }
}

fun main(args: Array<String>) {
    // Dependency injection.
    startKoin {
        modules(inject())
    }

    // Sets default locale to en_US.
    Locale.setDefault(Locale.ENGLISH)

    // Fonts.
    resource("fonts/Material-Design-Icons.ttf")!!.use { Font.loadFont(it, 24.0) }
    resource("fonts/Roboto-Regular.ttf")!!.use { Font.loadFont(it, 12.0) }
    resource("fonts/Roboto-Bold.ttf")!!.use { Font.loadFont(it, 12.0) }

    System.setProperty("prism.lcdtext", "false")

    // Log level.
    with(LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME) as ch.qos.logback.classic.Logger) {
        level = if ("-v" in args) Level.DEBUG else Level.INFO
    }

    // Loaders.
    IERSLoader().start()

    // Run the application.
    Application.launch(App::class.java, *args)
}
