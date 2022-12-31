package nebulosa.desktop

import javafx.application.Application
import nebulosa.desktop.cameras.CameraManager
import nebulosa.desktop.connections.ConnectionService
import nebulosa.desktop.eventbus.EventBus
import nebulosa.desktop.home.Home
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

    single { ConnectionService() }

    single { Home() }
    single { CameraManager() }
}

fun main(args: Array<String>) {
    startKoin {
        modules(inject())
    }

    Application.launch(Nebulosa::class.java, *args)
}
