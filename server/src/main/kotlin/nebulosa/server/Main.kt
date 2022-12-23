package nebulosa.server

import nebulosa.server.connection.ConnectionService
import nebulosa.server.equipments.EquipmentService
import nebulosa.server.equipments.cameras.CameraService
import org.greenrobot.eventbus.EventBus
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.withOptions
import org.koin.core.qualifier.named
import org.koin.dsl.module
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.createDirectories

private fun appDirectory(): Path {
    val userHomeDir = Paths.get(System.getProperty("user.home"))
    // TODO: Use different directory name based on current OS.
    val appDirectory = Paths.get("$userHomeDir", ".nebulosa")
    appDirectory.createDirectories()
    return appDirectory
}

private fun injection() = module {
    single { appDirectory() } withOptions { named("app") }

    single {
        EventBus.builder()
            .sendNoSubscriberEvent(false)
            .throwSubscriberException(false)
            .sendSubscriberExceptionEvent(false)
            .logNoSubscriberMessages(false)
            .logSubscriberExceptions(false)
            .build()
    }

    single(createdAtStart = true) { ConnectionService() }
    single(createdAtStart = true) { CameraService() }
    single(createdAtStart = true) { EquipmentService() }
}

fun main(args: Array<String>) {
    startKoin {
        modules(injection())
    }

    val server = NebulosaServer()
    server.start()
    Thread.currentThread().join()
}
