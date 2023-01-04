package nebulosa.desktop

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import javafx.application.Application
import nebulosa.desktop.cameras.CameraManagerScreen
import nebulosa.desktop.connections.ConnectionManager
import nebulosa.desktop.core.EventBus
import nebulosa.desktop.equipments.EquipmentManager
import nebulosa.desktop.filterwheels.FilterWheelManagerScreen
import nebulosa.desktop.focusers.FocuserManagerScreen
import nebulosa.desktop.mounts.MountManagerScreen
import nebulosa.desktop.preferences.Preferences
import nebulosa.desktop.telescopecontrol.TelescopeControlManager
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.named
import org.koin.core.module.dsl.withOptions
import org.koin.dsl.module
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
    single { jacksonObjectMapper() }
    single(createdAtStart = true) { Preferences(Paths.get("$appDirectory", "preferences.json")) }

    single { CameraManagerScreen() }
    single { MountManagerScreen() }
    single { FocuserManagerScreen() }
    single { FilterWheelManagerScreen() }

    single(createdAtStart = true) { ConnectionManager() }
    single(createdAtStart = true) { EquipmentManager() }
    single(createdAtStart = true) { TelescopeControlManager() }
}

fun main(args: Array<String>) {
    startKoin {
        modules(inject())
    }

    Locale.setDefault(Locale.ENGLISH)

    Application.launch(Nebulosa::class.java, *args)
}
