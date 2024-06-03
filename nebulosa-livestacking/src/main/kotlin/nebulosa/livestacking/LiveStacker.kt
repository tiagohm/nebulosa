package nebulosa.livestacking

import java.io.Closeable
import java.nio.file.Path

interface LiveStacker : Closeable {

    val isRunning: Boolean

    val isStacking: Boolean

    fun start()

    fun add(path: Path): Path?

    fun stop()
}
