package nebulosa.pixinsight.script

import nebulosa.io.resource
import nebulosa.io.transferAndClose
import nebulosa.pixinsight.script.PixInsightImageSolver.Output
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.deleteIfExists
import kotlin.io.path.outputStream

data class PixInsightPixelMath(
    private val slot: Int,
    private val inputPaths: List<Path>,
    private val outputPath: Path,
    private val expressionRK: String? = null,
    private val expressionG: String? = null,
    private val expressionB: String? = null,
) : AbstractPixInsightScript<PixInsightPixelMath.Output>() {

    private data class Input(
        @JvmField val statusPath: Path,
        @JvmField val inputPaths: List<Path>,
        @JvmField val outputPath: Path,
        @JvmField val expressionRK: String? = null,
        @JvmField val expressionG: String? = null,
        @JvmField val expressionB: String? = null,
    )

    data class Output(
        override val success: Boolean = false,
        override val errorMessage: String? = null,
        @JvmField val outputImage: Path? = null,
    ) : PixInsightScript.Output {

        companion object {

            @JvmStatic val FAILED = Output()
        }
    }

    private val scriptPath = Files.createTempFile("pi-", ".js")
    private val statusPath = Files.createTempFile("pi-", ".txt")

    init {
        resource("pixinsight/PixelMath.js")!!.transferAndClose(scriptPath.outputStream())
    }

    private val input = Input(statusPath, inputPaths, outputPath, expressionRK, expressionG, expressionB)
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
