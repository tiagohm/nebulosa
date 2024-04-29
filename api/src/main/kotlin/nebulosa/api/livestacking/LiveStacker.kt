package nebulosa.api.livestacking

import java.nio.file.Path

interface LiveStacker {

    fun start()

    fun add(path: Path): Path

    fun stop()
}
