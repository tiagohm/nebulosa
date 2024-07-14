package nebulosa.pixinsight.script

import nebulosa.io.resource
import nebulosa.io.transferAndClose
import nebulosa.math.Angle
import nebulosa.math.toDegrees
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.deleteIfExists
import kotlin.io.path.outputStream

data class PixInsightImageSolver(
    private val slot: Int,
    private val targetPath: Path,
    private val centerRA: Angle,
    private val centerDEC: Angle,
    private val pixelSize: Double = 0.0, // µm
    private val resolution: Double = 0.0, // arcsec/px
    private val focalDistance: Double = 0.0, // mm
) : AbstractPixInsightScript<PixInsightImageSolver.Output>() {

    private data class Input(
        @JvmField val targetPath: Path,
        @JvmField val statusPath: Path,
        @JvmField val centerRA: Double, // deg
        @JvmField val centerDEC: Double, // deg
        @JvmField val pixelSize: Double = 0.0,
        @JvmField val resolution: Double = 0.0,
        @JvmField val focalDistance: Double = 0.0,
    )

    data class Output(
        override val success: Boolean = false,
        override val errorMessage: String? = null,
        @JvmField val rightAscension: Angle = 0.0,
        @JvmField val declination: Angle = 0.0,
        @JvmField val resolution: Angle = 0.0,
        @JvmField val pixelSize: Double = 0.0,
        @JvmField val focalDistance: Double = 0.0,
        @JvmField val width: Angle = 0.0,
        @JvmField val height: Angle = 0.0,
        // @JvmField val rotation: Angle = 0.0,
        @JvmField val imageWidth: Double = 0.0,
        @JvmField val imageHeight: Double = 0.0,
        @JvmField val astrometricSolutionSummary: String = "",
    ) : PixInsightScript.Output {

        companion object {

            @JvmStatic val FAILED = Output()
        }
    }

    private val scriptPath = Files.createTempFile("pi-", ".js")
    private val statusPath = Files.createTempFile("pi-", ".txt")

    init {
        resource("pixinsight/ImageSolver.js")!!.transferAndClose(scriptPath.outputStream())
    }

    private val input = Input(targetPath, statusPath, centerRA.toDegrees, centerDEC.toDegrees, pixelSize, resolution, focalDistance)
    override val arguments = listOf("-x=${execute(slot, scriptPath, input)}")

    override fun processOnComplete(exitCode: Int): Output {
        if (exitCode == 0) {
            repeat(30) {
                statusPath.parseStatus<Output>()?.also { return it } ?: Thread.sleep(1000)
            }
        }

        return Output.FAILED
    }

    override fun close() {
        scriptPath.deleteIfExists()
        statusPath.deleteIfExists()
    }
}
