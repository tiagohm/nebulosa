package nebulosa.desktop.logic.task

import java.util.concurrent.Callable
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.min

interface Task<T> : Callable<T> {

    companion object {

        const val DELAY_INTERVAL = 100L

        @JvmStatic
        fun sleep(delay: Long, abort: AtomicBoolean) {
            var remainingTime = delay

            while (!abort.get() && remainingTime > 0L) {
                Thread.sleep(min(remainingTime, DELAY_INTERVAL))
                remainingTime -= DELAY_INTERVAL
            }
        }
    }
}
