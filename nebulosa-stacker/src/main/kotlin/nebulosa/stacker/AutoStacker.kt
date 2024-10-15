package nebulosa.stacker

import java.nio.file.Path

interface AutoStacker : Stacker {

    fun registerAutoStackerListener(listener: AutoStackerListener)

    fun unregisterAutoStackerListener(listener: AutoStackerListener)

    fun stack(targetPaths: Collection<Path>, outputPath: Path, referencePath: Path = targetPaths.first()): Boolean

    fun stop()
}
