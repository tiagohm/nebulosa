package nebulosa.stacker

import java.nio.file.Path

interface AutoStacker : Stacker {

    fun stack(paths: Collection<Path>, outputPath: Path, referencePath: Path = paths.first()): Boolean
}
