package nebulosa.stacker

import java.nio.file.Path

interface AutoStackerListener {

    fun onCalibrated(stackCount: Int, path: Path, calibratedPath: Path)

    fun onAligned(stackCount: Int, path: Path, alignedPath: Path)

    fun onIntegrated(stackCount: Int, path: Path, alignedPath: Path)
}
