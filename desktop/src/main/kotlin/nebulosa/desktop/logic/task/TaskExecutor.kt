package nebulosa.desktop.logic.task

import nebulosa.desktop.core.EventBus
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.slf4j.LoggerFactory
import java.io.Closeable
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Future
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.atomic.AtomicReference

abstract class TaskExecutor<T : Task> : Thread(), KoinComponent {

    private val eventBus by inject<EventBus>()
    private val tasks = LinkedBlockingQueue<CompletableTask>()
    private val runningTask = AtomicReference<T>()

    @Volatile private var running = false

    init {
        isDaemon = true
        start()
    }

    val currentTask: T? get() = runningTask.get()

    fun add(task: T): Future<Any> {
        val future = CompletableTask(task)
        tasks.offer(future)
        return future
    }

    final override fun start() = super.start()

    @Suppress("UNCHECKED_CAST")
    final override fun run() {
        running = true

        while (running) {
            val task = try {
                tasks.take()
            } catch (e: InterruptedException) {
                break
            }

            runningTask.set(task.task as T)
            eventBus.post(TaskStarted(task.task))

            try {
                task.run()
            } catch (e: InterruptedException) {
                task.close()
                break
            } catch (e: Throwable) {
                LOG.error("task exception", e)
            } finally {
                runningTask.set(null)
                eventBus.post(TaskFinished(task.task))
            }
        }
    }

    final override fun interrupt() {
        running = false
        super.interrupt()
    }

    private class CompletableTask(@JvmField val task: Task) : CompletableFuture<Any>(), Runnable, Closeable {

        override fun run() {
            try {
                complete(task.call())
            } catch (e: InterruptedException) {
                throw e
            } catch (e: Throwable) {
                completeExceptionally(e)
            }
        }

        override fun close() = task.closeGracefully()
    }

    companion object {

        @JvmStatic private val LOG = LoggerFactory.getLogger(TaskExecutor::class.java)
    }
}
