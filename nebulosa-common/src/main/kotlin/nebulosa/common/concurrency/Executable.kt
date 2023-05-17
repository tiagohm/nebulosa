package nebulosa.common.concurrency

import java.io.Closeable

interface Executable : Closeable {

    val stopped: Boolean

    fun execute()
}
