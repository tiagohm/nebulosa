package nebulosa.desktop

import ch.qos.logback.classic.Level
import com.fasterxml.jackson.databind.ObjectMapper
import javafx.application.Application
import javafx.scene.text.Font
import nebulosa.desktop.connections.ConnectionManager
import nebulosa.desktop.core.EventBus
import nebulosa.desktop.core.ScreenManager
import nebulosa.desktop.equipments.EquipmentManager
import nebulosa.desktop.preferences.Preferences
import nebulosa.desktop.telescopecontrol.TelescopeControlServerManager
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

    single { EventBus() }
    single { ObjectMapper() }
    single(createdAtStart = true) { Preferences(Paths.get("$appDirectory", "preferences.json")) }

    single(createdAtStart = true) { ConnectionManager() }
    single(createdAtStart = true) { EquipmentManager() }
    single(createdAtStart = true) { TelescopeControlServerManager() }
    single(createdAtStart = true) { ScreenManager() }
}

fun main(args: Array<String>) {
    startKoin {
        modules(inject())
    }

    Locale.setDefault(Locale.ENGLISH)

    resource("fonts/Material-Design-Icons.ttf")!!.use { Font.loadFont(it, 24.0) }
    resource("fonts/Roboto-Regular.ttf")!!.use { Font.loadFont(it, 12.0) }
    resource("fonts/Roboto-Bold.ttf")!!.use { Font.loadFont(it, 12.0) }

    System.setProperty("prism.lcdtext", "false")

    with(LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME) as ch.qos.logback.classic.Logger) {
        level = if ("-v" in args) Level.DEBUG else Level.INFO
    }

    Application.launch(Nebulosa::class.java, *args)
}
