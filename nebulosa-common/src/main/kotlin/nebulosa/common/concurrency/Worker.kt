package nebulosa.common.concurrency

import java.util.concurrent.atomic.AtomicBoolean

abstract class Worker : Runnable, Executable, Pauseable {

    private val running = AtomicBoolean()
    private val pauser = CountUpDownLatch()

    override val paused
        get() = !pauser.get()

    override val stopped
        get() = !running.get()

    final override fun run() {
        if (running.compareAndSet(false, true)) {
            try {
                while (running.get()) {
                    pauser.await()

                    if (running.get()) {
                        execute()
                    }
                }
            } finally {
                running.set(false)
            }
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
        running.set(false)
        pauser.reset()
    }
}
