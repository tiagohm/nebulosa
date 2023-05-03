package nebulosa.common.concurrency

import java.io.Closeable

interface Executable : Runnable, Closeable {

    val stopped: Boolean

    fun execute()
}
