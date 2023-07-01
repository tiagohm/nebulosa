package nebulosa.desktop.logic.concurrency

import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.disposables.Disposable
import javafx.animation.KeyFrame
import javafx.animation.Timeline
import javafx.application.Platform
import javafx.util.Duration
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference
import kotlin.math.max

object JavaFxScheduler : Scheduler() {

    override fun createWorker(): Worker = JavaFxWorker()

    private class JavaFxWorker : Worker(), Runnable {

        @Volatile private var head = QueuedRunnable(null)
        private val tail = AtomicReference(head)

        override fun dispose() {
            tail.set(null)

            var qr: QueuedRunnable? = head

            while (qr != null) {
                qr.dispose()
                qr = qr.getAndSet(null)
            }
        }

        override fun isDisposed(): Boolean {
            return tail.get() == null
        }

        override fun schedule(action: Runnable, delay: Long, unit: TimeUnit): Disposable {
            val ms = max(0, unit.toMillis(delay))

            val queuedRunnable = QueuedRunnable(action)

            if (ms == 0L) {
                // Delay is too small for the java fx timer, schedule it without delay.
                return schedule(queuedRunnable)
            }

            val timer = Timeline(KeyFrame(Duration.millis(ms.toDouble()), {
                schedule(queuedRunnable)
            }))

            timer.play()

            return Disposable.fromRunnable {
                queuedRunnable.dispose()
                timer.stop()
            }
        }

        override fun schedule(action: Runnable): Disposable {
            if (isDisposed) {
                return Disposable.disposed()
            }

            val queuedRunnable = if (action is QueuedRunnable) action else QueuedRunnable(action)

            var tailPivot: QueuedRunnable?

            do {
                tailPivot = tail.get()
            } while (tailPivot != null && !tailPivot.compareAndSet(null, queuedRunnable))

            if (tailPivot == null) {
                queuedRunnable.dispose()
            } else {
                // Can only fail with a concurrent dispose and we don't want
                // to override the disposed value.
                tail.compareAndSet(tailPivot, queuedRunnable)

                if (tailPivot === head) {
                    if (Platform.isFxApplicationThread()) {
                        run()
                    } else {
                        Platform.runLater(this)
                    }
                }
            }

            return queuedRunnable

        }

        override fun run() {
            var qr = head.get()

            while (qr != null) {
                qr.run()
                head = qr
                qr = qr.get()
            }
        }
    }

    internal class QueuedRunnable(@Volatile private var action: Runnable?) : AtomicReference<QueuedRunnable>(), Disposable, Runnable {

        override fun dispose() {
            action = null
        }

        override fun isDisposed(): Boolean {
            return action == null
        }

        override fun run() {
            action?.run()
            action = null
        }
    }
}
