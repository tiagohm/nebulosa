package nebulosa.livestacker

import java.io.Closeable
import java.nio.file.Path

interface LiveStacker : Closeable {

    val isRunning: Boolean

    val isStacking: Boolean

    fun start()

    // TODO: add CancellationToken parameter?
    fun add(path: Path): Path?

    fun stop()
}