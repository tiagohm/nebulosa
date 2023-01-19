package nebulosa.desktop.core.util.concurrent

import java.util.concurrent.TimeUnit

class Ticker(
    val action: Runnable,
    val delay: Long,
    val unit: TimeUnit = TimeUnit.MILLISECONDS,
) : Thread() {

    init {
        isDaemon = true
    }

    override fun run() {
        while (true) {
            try {
                action.run()
                sleep(unit.toMillis(delay))
            } catch (e: Throwable) {
                break
            }
        }
    }
}
