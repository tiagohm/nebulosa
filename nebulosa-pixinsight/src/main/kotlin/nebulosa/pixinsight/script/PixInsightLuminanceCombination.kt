package nebulosa.pixinsight.script

import nebulosa.io.resource
import nebulosa.io.transferAndClose
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.deleteIfExists
import kotlin.io.path.outputStream

data class PixInsightLuminanceCombination(
    private val slot: Int,
    private val outputPath: Path,
    private val luminancePath: Path,
    private val targetPath: Path,
) : AbstractPixInsightScript<PixInsightLuminanceCombination.Output>() {

    private data class Input(
        @JvmField val outputPath: Path,
        @JvmField val statusPath: Path,
        @JvmField val luminancePath: Path,
        @JvmField val targetPath: Path,
        @JvmField val wWeight: Double,
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
        resource("pixinsight/LuminanceCombination.js")!!.transferAndClose(scriptPath.outputStream())
    }

    private val input = Input(outputPath, statusPath, luminancePath, targetPath, 1.0)
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
