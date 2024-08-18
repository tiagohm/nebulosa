package nebulosa.livestacker

import nebulosa.stacker.Stacker
import java.nio.file.Path

interface LiveStacker : AutoCloseable {

    val stacker: Stacker?

    val isRunning: Boolean

    val isStacking: Boolean

    val stackedPath: Path?

    fun start()

    fun add(path: Path, referencePath: Path? = null): Path?

    fun stop()
}
