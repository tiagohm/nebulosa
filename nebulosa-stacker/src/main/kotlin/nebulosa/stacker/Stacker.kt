package nebulosa.stacker

import java.nio.file.Path

interface Stacker {

    fun calibrate(targetPath: Path, outputPath: Path, darkPath: Path? = null, flatPath: Path? = null, biasPath: Path? = null): Boolean

    fun align(referencePath: Path, targetPath: Path, outputPath: Path): Boolean

    fun integrate(stackCount: Int, stackedPath: Path, targetPath: Path, outputPath: Path): Boolean

    fun combineLRGB(outputPath: Path, luminancePath: Path? = null, redPath: Path? = null, greenPath: Path? = null, bluePath: Path? = null): Boolean

    fun combineLuminance(outputPath: Path, luminancePath: Path, targetPath: Path, mono: Boolean): Boolean

    fun saveAs(inputPath: Path, outputPath: Path): Boolean
}
