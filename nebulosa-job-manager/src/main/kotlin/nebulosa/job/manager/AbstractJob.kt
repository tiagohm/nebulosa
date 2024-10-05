package nebulosa.job.manager

import nebulosa.log.loggerFor
import nebulosa.util.concurrency.cancellation.CancellationListener
import nebulosa.util.concurrency.cancellation.CancellationSource
import nebulosa.util.concurrency.latch.CountUpDownLatch
import nebulosa.util.concurrency.latch.PauseListener
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executor
import java.util.concurrent.ForkJoinPool
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference

abstract class AbstractJob : JobTask, CancellationListener, PauseListener {

    @Volatile private var head: TaskNode? = null
    @Volatile private var tail: TaskNode? = null

    private val running = AtomicBoolean()
    private val cancelled = AtomicBoolean()
    private val pauseLatch = CountUpDownLatch()
    private val current = AtomicReference<TaskNode>()

    @Volatile final override var loopCount = 0
        private set

    @Volatile final override var taskCount = 0
        private set

    final override var size = 0
        private set

    override val isRunning
        get() = running.get()

    override val isCancelled
        get() = cancelled.get()

    override val isPaused
        get() = !pauseLatch.get()

    override val currentTask
        get() = current.get()?.item

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
        checkIfPaused(task)

        return if (isCancelled) {
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
    }

    final override fun run() {
        if (current.compareAndSet(null, requireNotNull(head))) {
            running.set(true)

            beforeStart()

            var prev: TaskNode? = null

            while (current.get() != null && isRunning && !isCancelled) {
                val (task, _, next) = current.get()
                val state = runTask(task, prev?.item)

                if (state == TaskExecutionState.OK) {
                    prev = current.get()
                } else if (state == TaskExecutionState.BREAK) {
                    break
                }

                if (next != null) {
                    current.set(next)
                } else if (isLoop()) {
                    loopCount++
                    current.set(head)
                } else {
                    current.set(null)
                }
            }

            afterFinish()
            running.set(false)

            current.set(null)
        }
    }

    fun runAsync(executor: Executor = EXECUTOR): CompletableFuture<Void> {
        return CompletableFuture.runAsync(this, executor)
    }

    final override fun waitForPause() {
        pauseLatch.await()
    }

    private fun checkIfPaused(task: Task) {
        if (isPaused && canPause(task)) {
            beforePause(task)
            waitForPause()
            afterPause(task)
        }
    }

    final override fun iterator(): MutableIterator<Task> {
        return NextIterator()
    }

    final override fun addFirst(e: Task) {
        val h = head
        val node = TaskNode(e, null, h)

        if (h == null) {
            tail = node
        } else {
            h.prev = node
        }

        head = node
        size++
    }

    final override fun addLast(e: Task) {
        val t = tail
        val node = TaskNode(e, t, null)

        if (t == null) {
            head = node
        } else {
            t.next = node
        }

        tail = node
        size++
    }

    final override fun offerFirst(e: Task): Boolean {
        addFirst(e)
        return true
    }

    final override fun offerLast(e: Task): Boolean {
        return add(e)
    }

    final override fun removeFirst(): Task {
        return removeFirst(head ?: throw NoSuchElementException())
    }

    final override fun removeLast(): Task {
        return removeLast(tail ?: throw NoSuchElementException())
    }

    final override fun pollFirst(): Task? {
        return removeFirst(head ?: return null)
    }

    final override fun pollLast(): Task? {
        return removeLast(tail ?: return null)
    }

    final override fun getFirst(): Task {
        return peekFirst() ?: throw NoSuchElementException()
    }

    final override fun getLast(): Task {
        return peekLast() ?: throw NoSuchElementException()
    }

    final override fun peekFirst(): Task? {
        return head?.item
    }

    final override fun peekLast(): Task? {
        return tail?.item
    }

    final override fun removeFirstOccurrence(o: Any?): Boolean {
        return o is Task && remove(o)
    }

    final override fun removeLastOccurrence(o: Any?): Boolean {
        if (o == null || o !is Task) return false

        var node = tail

        while (node != null) {
            if (node.item == o) {
                remove(node)
                return true
            }

            node = node.prev
        }

        return false
    }

    final override fun add(element: Task): Boolean {
        addLast(element)
        return true
    }

    final override fun offer(e: Task): Boolean {
        return add(e)
    }

    final override fun remove(): Task {
        return removeFirst()
    }

    final override fun remove(element: Task?): Boolean {
        if (element == null) return false

        var node = head

        while (node != null) {
            if (node.item == element) {
                remove(node)
                return true
            }

            node = node.next
        }

        return false
    }

    final override fun poll(): Task? {
        return head?.let(::removeFirst)
    }

    final override fun element(): Task {
        return first
    }

    final override fun peek(): Task? {
        return peekFirst()
    }

    final override fun addAll(elements: Collection<Task?>): Boolean {
        var added = false

        for (item in elements) {
            added = added || (item != null && add(item))
        }

        return added
    }

    final override fun push(e: Task) {
        addFirst(e)
    }

    final override fun pop(): Task {
        return removeFirst()
    }

    final override fun contains(element: Task?): Boolean {
        if (element == null) return false

        var node = head

        while (node != null) {
            if (node.item == element) {
                return true
            }

            node = node.next
        }

        return false
    }

    final override fun descendingIterator(): Iterator<Task?> {
        return PrevIterator()
    }

    final override fun clear() {
        var node = head

        while (node != null) {
            val next = node.next
            node.prev = null
            node.next = null
            node = next
        }

        head = null
        tail = null
        size = 0
    }

    final override fun removeAll(elements: Collection<Task?>): Boolean {
        var node = head
        var removed = false

        while (node != null) {
            if (node.item in elements) {
                remove(node)
                removed = true
            }

            node = node.next
        }

        return removed
    }

    final override fun retainAll(elements: Collection<Task?>): Boolean {
        var node = head
        var removed = false

        while (node != null) {
            if (node.item !in elements) {
                remove(node)
                removed = true
            }

            node = node.next
        }

        return removed
    }

    final override fun isEmpty(): Boolean {
        return size == 0
    }

    final override fun containsAll(elements: Collection<Task?>): Boolean {
        return elements.all { it in this }
    }

    private fun removeFirst(node: TaskNode): Task {
        val item = node.item
        val next = node.next

        node.next = null
        head = next

        if (next == null) {
            tail = null
        } else {
            next.prev = null
        }

        size--

        return item
    }

    private fun removeLast(node: TaskNode): Task {
        val item = node.item
        val prev = node.prev

        node.prev = null
        tail = prev

        if (prev == null) {
            head = null
        } else {
            prev.next = null
        }

        size--

        return item
    }

    private fun remove(node: TaskNode): Task {
        val item = node.item
        val prev = node.prev
        val next = node.next

        if (prev == null) {
            head = next
        } else {
            prev.next = next
            node.prev = null
        }

        if (next == null) {
            tail = prev
        } else {
            next.prev = prev
            node.next = null
        }

        size--

        return item
    }

    private inner class NextIterator : MutableIterator<Task> {

        @Volatile private var next: TaskNode? = null

        override fun remove() {
            TODO("Not yet implemented")
        }

        override fun hasNext(): Boolean {
            return next != null || head != null
        }

        override fun next(): Task {
            val n = next

            if (n == null) {
                next = head ?: throw NoSuchElementException()
                return next!!.item
            } else {
                next = n.next
                return n.item
            }
        }
    }

    private inner class PrevIterator : MutableIterator<Task> {

        @Volatile private var next: TaskNode? = null

        override fun remove() {
            TODO("Not yet implemented")
        }

        override fun hasNext(): Boolean {
            return next != null || tail != null
        }

        override fun next(): Task {
            val n = next

            if (n == null) {
                next = tail ?: throw NoSuchElementException()
                return next!!.item
            } else {
                next = n.prev
                return n.item
            }
        }
    }

    companion object {

        @JvmStatic private val EXECUTOR = ForkJoinPool.commonPool()
        @JvmStatic private val LOG = loggerFor<AbstractJob>()
    }
}
