package nebulosa.desktop

import javafx.application.Application
import nebulosa.desktop.connections.ConnectionManager
import nebulosa.desktop.core.eventbus.EventBus
import nebulosa.desktop.equipments.EquipmentManager
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.named
import org.koin.core.module.dsl.withOptions
import org.koin.dsl.module
import java.nio.file.Path
import java.nio.file.Paths
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
    single { appDirectory() } withOptions { named("app") }

    single { EventBus() }

    single { Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()) }

    single(createdAtStart = true) { ConnectionManager() }
    single(createdAtStart = true) { EquipmentManager() }
}

fun main(args: Array<String>) {
    startKoin {
        modules(inject())
    }

    Application.launch(Nebulosa::class.java, *args)
}
