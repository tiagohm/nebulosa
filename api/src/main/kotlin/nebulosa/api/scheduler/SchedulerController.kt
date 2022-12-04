package nebulosa.api.scheduler

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationEventPublisher
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.concurrent.Executors
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.Phaser
import java.util.concurrent.atomic.AtomicReference

@RestController
@RequestMapping("scheduler")
class SchedulerController {

    @Autowired
    private lateinit var applicationEventPublisher: ApplicationEventPublisher

    private val workerExecutor = Executors.newFixedThreadPool(1)
    private val scheduledTaskExecutor = Executors.newFixedThreadPool(1)
    private val runningTask = AtomicReference<ScheduledTask<*>>(null)
    private val scheduledTasks = LinkedBlockingQueue<ScheduledTask<*>>()
    private val finishedTasks = ArrayList<ScheduledTask<*>>(256)
    private val pauser = Phaser(1)

    init {
        workerExecutor.submit(SchduledTaskWorker())
    }

    @Synchronized
    internal fun <T> add(task: ScheduledTask<T>) {
        scheduledTasks.add(task)
    }

    @Synchronized
    internal fun stop(task: ScheduledTask<*>) {
        pause()

        if (runningTask.get() === task) {
            task.cancel()
        } else if (!task.isDone()) {
            scheduledTasks.remove(task)
        }

        unpause()
    }

    val isPaused get() = pauser.registeredParties > 0

    @Synchronized
    @PostMapping("pause")
    fun pause() {
        pauser.register()
    }

    @Synchronized
    @PostMapping("unpause")
    fun unpause() {
        pauser.arriveAndDeregister()
    }

    @GetMapping
    fun tasks(): List<ScheduledTask<*>> {
        val tasks = ArrayList<ScheduledTask<*>>(finishedTasks.size + scheduledTasks.size + 1)
        tasks.addAll(finishedTasks)
        if (runningTask.get() != null) tasks.add(runningTask.get())
        tasks.addAll(scheduledTasks)
        return tasks
    }

    private inner class SchduledTaskWorker : Thread() {

        override fun run() {
            while (true) {
                try {
                    println("Waiting for new task")
                    pauser.arriveAndAwaitAdvance()
                    val task = scheduledTasks.take()
                    println("Running task ${task.name}")
                    runningTask.set(task)
                    applicationEventPublisher.publishEvent(ScheduledTaskStartedEvent(task))
                    val future = scheduledTaskExecutor.submit(task)
                    task.attach(future)
                    println("Waiting for task finish")
                    future.get()
                    println("Task finished")
                } catch (e: Throwable) {
                    e.printStackTrace()
                } finally {
                    val task = runningTask.getAndSet(null)
                    finishedTasks.add(task)
                    applicationEventPublisher.publishEvent(ScheduledTaskFinishedEvent(task))
                }
            }
        }
    }
}
