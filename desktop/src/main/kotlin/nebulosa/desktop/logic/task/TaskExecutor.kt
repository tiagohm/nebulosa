package nebulosa.desktop.logic.task

import nebulosa.desktop.logic.camera.CameraTask
import nebulosa.desktop.logic.filterwheel.FilterWheelTask
import nebulosa.desktop.logic.focuser.FocuserTask
import nebulosa.desktop.logic.mount.MountTask
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.config.AutowireCapableBeanFactory
import org.springframework.stereotype.Service
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutorService

@Service
class TaskExecutor {

    @Autowired private lateinit var beanFactory: AutowireCapableBeanFactory
    @Autowired private lateinit var cameraExecutorService: ExecutorService
    @Autowired private lateinit var mountExecutorService: ExecutorService

    @Synchronized
    fun <T> execute(task: Task<T>): CompletableFuture<T> {
        val executorService = when (task) {
            is CameraTask,
            is FocuserTask,
            is FilterWheelTask -> cameraExecutorService
            is MountTask -> mountExecutorService
            else -> throw IllegalArgumentException("unable to execute the task: $task")
        }

        beanFactory.autowireBean(task)

        return CompletableFuture
            .supplyAsync(task, executorService)
    }
}
