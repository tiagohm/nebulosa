package nebulosa.util.time

import nebulosa.util.Resettable
import java.time.Duration

/**
 * A stopwatch which measures time while it's running.
 *
 * A stopwatch is either running or stopped.
 * It measures the elapsed time that passes while the stopwatch is running.
 *
 * When a stopwatch is initially created, it is stopped and has measured no elapsed time.
 *
 * The elapsed time can be accessed in various formats using [elapsed],
 * [elapsedMilliseconds], [elapsedMicroseconds] or [elapsedTicks].
 *
 * The stopwatch is started by calling [start].
 */
class Stopwatch : Resettable {

    @Volatile private var start = 0L
    @Volatile private var stop = 0L

    /**
     * The elapsed number of clock ticks since calling [start] while the [Stopwatch] is running.
     */
    val elapsedTicks
        get() = if (isStopped) stop - start else System.nanoTime() - start

    /**
     * The [elapsedTicks] counter converted to [Duration].
     */
    inline val elapsed: Duration
        get() = Duration.ofNanos(elapsedTicks)

    /**
     * The [elapsedTicks] counter converted to microseconds.
     */
    inline val elapsedMicroseconds
        get() = elapsedTicks / 1000L

    /**
     * The [elapsedTicks] counter converted to milliseconds.
     */
    inline val elapsedMilliseconds
        get() = elapsedMicroseconds / 1000L

    /**
     * The [elapsedTicks] counter converted to seconds.
     */
    inline val elapsedSeconds
        get() = elapsedMilliseconds / 1000L

    /**
     * Determines whether the [Stopwatch] is currently stopped.
     */
    val isStopped
        get() = stop >= 0L

    /**
     * Determines whether the [Stopwatch] is currently running.
     */
    val isRunning
        get() = stop == -1L

    /**
     * Starts the [Stopwatch].
     *
     * The [elapsed] count increases monotonically. If the [Stopwatch] has
     * been stopped, then calling start again restarts it without resetting the
     * [elapsed] count.
     *
     * If the [Stopwatch] is currently running, then calling start does nothing.
     */
    @Synchronized
    fun start() {
        // Don't count the time while the stopwatch has been stopped.
        if (isStopped) {
            start += System.nanoTime() - stop
            stop = -1L
        }
    }

    /**
     * Stops the [Stopwatch].
     *
     * The [elapsedTicks] count stops increasing after this call. If the
     * [Stopwatch] is currently not running, then calling this method has no
     * effect.
     */
    @Synchronized
    fun stop() {
        if (isRunning) {
            stop = System.nanoTime()
        }
    }

    /**
     * Resets the [elapsed] count to zero.
     *
     * This method does not stop or start the [Stopwatch].
     */
    @Synchronized
    override fun reset() {
        start = if (isStopped) stop else System.nanoTime()
    }
}
