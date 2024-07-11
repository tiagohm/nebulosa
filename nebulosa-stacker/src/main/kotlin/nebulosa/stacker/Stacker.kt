package nebulosa.stacker

import java.nio.file.Path

interface Stacker {

    fun calibrate(
        targetPath: Path, outputPath: Path,
        darkPath: Path? = null, flatPath: Path? = null, biasPath: Path? = null,
    ): Boolean

    fun align(referencePath: Path, targetPath: Path, outputPath: Path): Boolean

    fun integrate(stackCount: Int, stackedPath: Path, targetPath: Path, outputPath: Path): Boolean
}
