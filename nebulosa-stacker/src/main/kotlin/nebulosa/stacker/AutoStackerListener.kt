package nebulosa.stacker

import java.nio.file.Path

interface AutoStackerListener {

    fun onCalibrationStarted(stackCount: Int, path: Path) = Unit

    fun onAlignStarted(stackCount: Int, path: Path) = Unit

    fun onIntegrationStarted(stackCount: Int, path: Path) = Unit

    fun onCalibrationFinished(stackCount: Int, path: Path, calibratedPath: Path) = Unit

    fun onAlignFinished(stackCount: Int, path: Path, alignedPath: Path) = Unit

    fun onIntegrationFinished(stackCount: Int, path: Path, alignedPath: Path) = Unit
}
