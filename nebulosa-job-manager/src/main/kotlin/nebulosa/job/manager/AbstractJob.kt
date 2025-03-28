package nebulosa.job.manager

import nebulosa.log.loggerFor
import nebulosa.util.concurrency.cancellation.CancellationListener
import nebulosa.util.concurrency.cancellation.CancellationSource
import nebulosa.util.concurrency.latch.CountUpDownLatch
import nebulosa.util.concurrency.latch.PauseListener
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executor
import java.util.concurrent.ForkJoinPool
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

abstract class AbstractJob : JobTask, CancellationListener, PauseListener {

    private val tasks = Collections.synchronizedList(ArrayList<Task>(128))

    private val running = AtomicBoolean()
    private val cancelled = AtomicBoolean()
    private val pauseLatch = CountUpDownLatch()
    private val current = AtomicInteger(-1)

    @Volatile final override var loopCount = 0
        private set

    @Volatile final override var taskCount = 0
        private set

    override val size
        get() = tasks.size

    override val isRunning
        get() = running.get()

    override val isCancelled
        get() = cancelled.get()

    override val isPaused
        get() = !pauseLatch.get()

    override val currentTask
        get() = tasks.getOrNull(current.get())

    protected open fun beforeStart() = Unit

    protected open fun beforeTask(task: Task) = Unit

    protected open fun afterTask(task: Task, exception: Throwable? = null) = exception == null

    protected open fun afterFinish() = Unit

    protected open fun beforePause(task: Task) = Unit

    protected open fun afterPause(task: Task) = Unit

    protected open fun isLoop() = false

    protected open fun canRun(prev: Task?, current: Task) = true

    protected open fun canPause(task: Task) = true

    override fun onCancel(source: CancellationSource) {
        cancelled.set(true)
        currentTask?.onCancel(source)
    }

    override fun onPause(paused: Boolean) {
        if (paused) {
            pauseLatch.countUp()
        } else {
            pauseLatch.reset()
        }

        currentTask?.onPause(paused)
    }

    override fun runTask(task: Task, prev: Task?): TaskExecutionState {
        return try {
            checkIfPaused(task)

            if (isCancelled) {
                TaskExecutionState.BREAK
            } else if (canRun(prev, task)) {
                beforeTask(task)

                var exception: Throwable? = null

                try {
                    taskCount++
                    task.run()
                } catch (e: Throwable) {
                    LOG.error("task execution failed", e)
                    exception = e
                }

                if (!afterTask(task, exception) || isCancelled) {
                    TaskExecutionState.BREAK
                } else {
                    checkIfPaused(task)
                    TaskExecutionState.OK
                }
            } else {
                TaskExecutionState.CONTINUE
            }
        } catch (e: Throwable) {
            LOG.error("task execution failed", e)
            TaskExecutionState.CONTINUE
        }
    }

    final override fun run() {
        if (current.compareAndSet(-1, 0)) {
            running.set(true)

            beforeStart()

            var prev: Task? = null

            while (isRunning && !isCancelled) {
                val index = current.get()
                val task = tasks.getOrNull(index) ?: break
                val state = runTask(task, prev)

                if (state == TaskExecutionState.OK) {
                    prev = task
                } else if (state == TaskExecutionState.BREAK) {
                    break
                }

                val next = tasks.getOrNull(index + 1)

                if (next != null) {
                    current.set(index + 1)
                } else if (isLoop()) {
                    loopCount++
                    current.set(0)
                } else {
                    break
                }
            }

            afterFinish()
            running.set(false)

            current.set(-1)
        }
    }

    fun runAsync(executor: Executor = EXECUTOR): CompletableFuture<Void> {
        return CompletableFuture.runAsync(this, executor)
    }

    private fun checkIfPaused(task: Task) {
        if (isPaused && canPause(task)) {
            beforePause(task)
            pauseLatch.await()
            afterPause(task)
        }
    }

    final override fun iterator() = object : MutableIterator<Task> {

        @Volatile private var index = 0

        override fun hasNext(): Boolean {
            return index < size
        }

        override fun next(): Task {
            return tasks.getOrNull(index++) ?: throw NoSuchElementException()
        }

        override fun remove() {
            TODO("Not yet implemented")
        }
    }

    fun addFirst(task: Task) {
        tasks.add(0, task)
    }

    fun addLast(task: Task) {
        tasks.add(task)
    }

    fun removeFirst(): Task? {
        return tasks.removeFirstOrNull()
    }

    fun removeLast(): Task? {
        return tasks.removeLastOrNull()
    }

    final override fun add(element: Task): Boolean {
        addLast(element)
        return true
    }

    final override fun contains(element: Task): Boolean {
        return element in tasks
    }

    final override fun clear() {
        tasks.clear()
    }

    final override fun removeAll(elements: Collection<Task>): Boolean {
        return tasks.removeAll(elements)
    }

    final override fun retainAll(elements: Collection<Task>): Boolean {
        return tasks.retainAll(elements)
    }

    final override fun isEmpty(): Boolean {
        return size == 0
    }

    final override fun containsAll(elements: Collection<Task>): Boolean {
        return tasks.containsAll(elements)
    }

    override fun addAll(elements: Collection<Task>): Boolean {
        return tasks.addAll(elements)
    }

    override fun remove(element: Task): Boolean {
        return tasks.remove(element)
    }

    companion object {

        private val EXECUTOR = ForkJoinPool.commonPool()
        private val LOG = loggerFor<AbstractJob>()
    }
}
