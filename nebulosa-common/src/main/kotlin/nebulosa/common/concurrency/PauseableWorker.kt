package nebulosa.common.concurrency

abstract class PauseableWorker(private val name: String) : Worker, Pauseable {

    @Volatile private var thread: Thread? = null
    @Volatile private var running = false
    private val pauser = CountUpDownLatch()

    override val paused
        get() = pauser.get()

    override val stopped
        get() = thread == null

    fun start() {
        if (thread == null) {
            running = true

            thread = Thread {
                while (running) {
                    pauser.await()
                    run()
                }
            }

            thread!!.isDaemon = true
            thread!!.start()
        }
    }

    override fun pause() {
        if (!paused) {
            pauser.countUp()
        }
    }

    override fun unpause() {
        if (paused) {
            pauser.countDown()
        }
    }

    override fun close() {
        running = false
        pauser.reset()

        thread?.interrupt()
        thread = null
    }
}
