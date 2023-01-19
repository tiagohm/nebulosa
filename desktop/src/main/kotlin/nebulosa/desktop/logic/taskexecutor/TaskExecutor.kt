package nebulosa.desktop.logic.taskexecutor

import nebulosa.desktop.core.EventBus
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.slf4j.LoggerFactory
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.atomic.AtomicReference

abstract class TaskExecutor<T : Task> : Thread(), KoinComponent {

    private val eventBus by inject<EventBus>()
    private val tasks = LinkedBlockingQueue<T>()
    private val runningTask = AtomicReference<T>()

    @Volatile private var running = false

    init {
        isDaemon = true
        start()
    }

    val currentTask: T? get() = runningTask.get()

    fun add(task: T) = tasks.offer(task)

    fun remove(task: T) = tasks.remove(task)

    final override fun start() = super.start()

    final override fun run() {
        running = true

        while (running) {
            val task = try {
                tasks.take()
            } catch (e: InterruptedException) {
                break
            }

            runningTask.set(task)
            eventBus.post(TaskStarted(task))

            try {
                task.run()
            } catch (e: InterruptedException) {
                task.closeGracefully()
                break
            } catch (e: Throwable) {
                LOG.error("task exception", e)
            } finally {
                runningTask.set(null)
                eventBus.post(TaskFinished(task))
            }
        }
    }

    final override fun interrupt() {
        running = false
        super.interrupt()
    }

    companion object {

        @JvmStatic private val LOG = LoggerFactory.getLogger(TaskExecutor::class.java)
    }
}
